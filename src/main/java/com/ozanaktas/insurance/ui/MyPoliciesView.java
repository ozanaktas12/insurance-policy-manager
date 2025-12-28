package com.ozanaktas.insurance.ui;

import com.ozanaktas.insurance.model.Policy;
import com.ozanaktas.insurance.model.User;
import com.ozanaktas.insurance.service.PolicyService;
import com.ozanaktas.insurance.service.UndoService;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class MyPoliciesView {

    private final User user;
    private final PolicyService policyService;
    private final UndoService undoService;
    private final Runnable onBack;

    private final ListView<Policy> list = new ListView<>();
    private final Label counts = new Label();

    public MyPoliciesView(User user, PolicyService policyService, UndoService undoService, Runnable onBack) {
        this.user = user;
        this.policyService = policyService;
        this.undoService = undoService;
        this.onBack = onBack;
    }

    public Parent getView() {
        Label title = new Label("My Policies");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label who = new Label("Customer: " + user.getFullName() + " (" + user.getUsername() + ")");
        who.setStyle("-fx-opacity: 0.85;");

        counts.setStyle("-fx-opacity: 0.85;");

        Label message = new Label();
        message.setStyle("-fx-opacity: 0.9;");

        list.setPlaceholder(new Label("No policies found"));

        DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Policy item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String premium = "$" + String.format("%.2f", item.getPremium());
                    String dates = item.getStartDate().format(df) + " → " + item.getEndDate().format(df);
                    setText(item.getPolicyNo() + " — " + item.getType() + " — " + item.getStatus() + " — " + premium + " — " + dates);
                }
            }
        });

        Button cancelSelected = new Button("Cancel Selected");
        Button undoLast = new Button("Undo Last");
        Button refresh = new Button("Refresh");
        Button back = new Button("Back");

        cancelSelected.setMaxWidth(Double.MAX_VALUE);
        undoLast.setMaxWidth(Double.MAX_VALUE);
        refresh.setMaxWidth(Double.MAX_VALUE);
        back.setMaxWidth(Double.MAX_VALUE);

        cancelSelected.setOnAction(e -> {
            Policy selected = list.getSelectionModel().getSelectedItem();
            if (selected == null) {
                message.setText("Select a policy first.");
                return;
            }
            String msg = policyService.cancelPolicyWithUndo(selected.getPolicyNo(), undoService);
            message.setText(msg);
            refresh();
        });

        undoLast.setOnAction(e -> {
            String msg = (undoService == null) ? "Undo service not available" : undoService.undoLast().orElse("Nothing to undo");
            message.setText(msg);
            refresh();
        });

        refresh.setOnAction(e -> {
            message.setText("");
            refresh();
        });

        back.setOnAction(e -> onBack.run());

        HBox actions = new HBox(10, cancelSelected, undoLast, refresh, back);
        HBox.setHgrow(cancelSelected, Priority.ALWAYS);
        HBox.setHgrow(undoLast, Priority.ALWAYS);
        HBox.setHgrow(refresh, Priority.ALWAYS);
        HBox.setHgrow(back, Priority.ALWAYS);

        VBox root = new VBox(10, title, who, counts, list, actions, message);
        root.setPadding(new Insets(20));
        root.setPrefWidth(720);
        root.setPrefHeight(520);

        refresh();
        return root;
    }

    private void refresh() {
        var policies = policyService.getPoliciesForCustomer(user.getUsername());
        list.getItems().setAll(policies);
        counts.setText("Total policies: " + policies.size());
    }
}