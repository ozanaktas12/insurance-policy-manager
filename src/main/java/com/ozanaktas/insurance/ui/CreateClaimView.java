package com.ozanaktas.insurance.ui;

import com.ozanaktas.insurance.model.Claim;
import com.ozanaktas.insurance.model.Policy;
import com.ozanaktas.insurance.model.User;
import com.ozanaktas.insurance.service.ClaimQueueService;
import com.ozanaktas.insurance.service.PolicyService;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;

public class CreateClaimView {

    private final User user;
    private final ClaimQueueService claimQueueService;
    private final PolicyService policyService;
    private final Runnable onBack;

    public CreateClaimView(User user, ClaimQueueService claimQueueService, PolicyService policyService, Runnable onBack) {
        this.user = user;
        this.claimQueueService = claimQueueService;
        this.policyService = policyService;
        this.onBack = onBack;
    }

    public Parent getView() {
        Label title = new Label("Create Claim (Customer)");

        
        ComboBox<String> policyNoBox = new ComboBox<>();
        policyNoBox.setEditable(true);
        policyNoBox.setPromptText("Policy No (select or type)");

        var myPolicies = policyService.getPoliciesForCustomer(user.getUsername());
        policyNoBox.getItems().setAll(
                myPolicies.stream().map(Policy::getPolicyNo).collect(Collectors.toList())
        );
        if (!policyNoBox.getItems().isEmpty()) {
            policyNoBox.getSelectionModel().selectFirst();
        }

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Describe the incident...");
        descriptionArea.setPrefRowCount(4);

        TextField amountField = new TextField();
        amountField.setPromptText("Claim Amount (e.g. 2500)");

        Label message = new Label();

        Button submit = new Button("Submit Claim");
        Button back = new Button("Back");

        submit.setOnAction(e -> {
            String policyNo = policyNoBox.getEditor().getText() == null ? "" : policyNoBox.getEditor().getText().trim();
            String desc = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
            String amtStr = amountField.getText() == null ? "" : amountField.getText().trim();

            if (policyNo.isEmpty()) {
                message.setText("Policy No cannot be empty.");
                return;
            }
            if (desc.isEmpty()) {
                message.setText("Description cannot be empty.");
                return;
            }

            if (!policyService.customerOwnsPolicy(user.getUsername(), policyNo)) {
                String valid = policyService.getPoliciesForCustomer(user.getUsername())
                        .stream()
                        .map(Policy::getPolicyNo)
                        .collect(Collectors.joining(", "));

                if (valid.isBlank()) {
                    message.setText("Invalid policy. You currently have no policies.");
                } else {
                    message.setText("Invalid policy. Valid policies: " + valid);
                }
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amtStr);
                if (amount <= 0) {
                    message.setText("Amount must be > 0.");
                    return;
                }
            } catch (NumberFormatException ex) {
                message.setText("Amount must be a number (e.g. 2500).");
                return;
            }

            Claim claim = new Claim(user.getUsername(), policyNo, desc, amount);
            claimQueueService.submitClaim(claim);

            message.setText("Claim submitted. It is now in the queue.");
     
            policyNoBox.getEditor().clear();
            if (!policyNoBox.getItems().isEmpty()) {
                policyNoBox.getSelectionModel().selectFirst();
            }
            descriptionArea.clear();
            amountField.clear();
        });

        back.setOnAction(e -> onBack.run());

        HBox buttons = new HBox(10, submit, back);

        VBox root = new VBox(
                10,
                title,
                new Label("Customer: " + user.getFullName() + " (" + user.getUsername() + ")"),
                new Label("Policy No"),
                policyNoBox,
                new Label("Description"),
                descriptionArea,
                new Label("Amount"),
                amountField,
                buttons,
                message
        );

        root.setPadding(new Insets(20));
        root.setPrefWidth(520);

        return root;
    }
}