package com.ozanaktas.insurance.ui;

import com.ozanaktas.insurance.model.InsuranceType;
import com.ozanaktas.insurance.model.Policy;
import com.ozanaktas.insurance.model.Role;
import com.ozanaktas.insurance.model.User;
import com.ozanaktas.insurance.service.PolicyService;
import com.ozanaktas.insurance.service.UndoService;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class CreatePolicyView {

    private final User user;
    private final PolicyService policyService;
    private final UndoService undoService; // şimdilik ekranda kullanmayacağız (bir sonraki adımda policy-undo ekleyeceğiz)
    private final Runnable onBack;

    public CreatePolicyView(User user, PolicyService policyService, UndoService undoService, Runnable onBack) {
        this.user = user;
        this.policyService = policyService;
        this.undoService = undoService;
        this.onBack = onBack;
    }

    public Parent getView() {
        Label title = new Label("Create Policy (Agent/Admin)");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label who = new Label("Logged in: " + user.getFullName() + " (" + user.getRole() + ")");
        who.setStyle("-fx-opacity: 0.85;");

        // Yetki kontrolü (Customer bu ekrana gelirse)
        boolean allowed = user.getRole() == Role.AGENT || user.getRole() == Role.ADMIN;

        TextField customerUsernameField = new TextField();
        customerUsernameField.setPromptText("Customer username (e.g. customer)");

        ComboBox<InsuranceType> typeBox = new ComboBox<>();
        typeBox.getItems().setAll(InsuranceType.values());
        typeBox.getSelectionModel().selectFirst();

        TextField premiumField = new TextField();
        premiumField.setPromptText("Premium (e.g. 1200)");

        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        DatePicker endDatePicker = new DatePicker(LocalDate.now().plusMonths(12));

        Label message = new Label();

        Button create = new Button("Create Policy");
        Button back = new Button("Back");
        Button undoLast = new Button("Undo Last");
        undoLast.setDisable(!allowed);

        create.setMaxWidth(Double.MAX_VALUE);
        back.setMaxWidth(Double.MAX_VALUE);
        undoLast.setMaxWidth(Double.MAX_VALUE);

        create.setDisable(!allowed);

        create.setOnAction(e -> {
            String customerUsername = customerUsernameField.getText() == null ? "" : customerUsernameField.getText().trim();
            InsuranceType type = typeBox.getValue();
            String premiumStr = premiumField.getText() == null ? "" : premiumField.getText().trim();
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();

            if (customerUsername.isEmpty()) {
                message.setText("Customer username cannot be empty.");
                return;
            }
            if (type == null) {
                message.setText("Insurance type must be selected.");
                return;
            }

            double premium;
            try {
                premium = Double.parseDouble(premiumStr);
                if (premium <= 0) {
                    message.setText("Premium must be > 0.");
                    return;
                }
            } catch (NumberFormatException ex) {
                message.setText("Premium must be a number (e.g. 1200).");
                return;
            }

            if (start == null || end == null) {
                message.setText("Start and end date cannot be empty.");
                return;
            }
            if (!end.isAfter(start)) {
                message.setText("End date must be after start date.");
                return;
            }

            Policy created = policyService.createPolicyWithUndo(customerUsername, type, premium, start, end, undoService);
            message.setText("Policy created.  PolicyNo: " + created.getPolicyNo());

            
            if (undoService != null && undoService.canUndo()) {
                undoLast.setDisable(false);
            }

            customerUsernameField.clear();
            premiumField.clear();
            typeBox.getSelectionModel().selectFirst();
        });

        undoLast.setOnAction(e -> {
            var msg = (undoService == null) ? "Undo service not available" : undoService.undoLast().orElse("Nothing to undo");
            message.setText(msg);
        });

        back.setOnAction(e -> onBack.run());

        VBox form = new VBox(
                8,
                new Label("Customer Username"), customerUsernameField,
                new Label("Insurance Type"), typeBox,
                new Label("Premium"), premiumField,
                new Label("Start Date"), startDatePicker,
                new Label("End Date"), endDatePicker
        );

        HBox buttons = new HBox(10, create, undoLast, back);

        VBox root = new VBox(12, title, who);

        if (!allowed) {
            Label warn = new Label("You are not allowed to create policies. (Agent/Admin only)");
            warn.setStyle("-fx-text-fill: #b00020;");
            root.getChildren().addAll(warn, back);
        } else {
            root.getChildren().addAll(form, buttons, new Label("Tip: Create a policy, then press Undo Last to delete the most recently created policy (Stack/LIFO)."), message);
        }

        root.setPadding(new Insets(20));
        root.setPrefWidth(760);
        root.setPrefHeight(540);

        return root;
    }
}