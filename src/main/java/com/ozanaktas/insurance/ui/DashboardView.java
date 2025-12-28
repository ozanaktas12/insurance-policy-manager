package com.ozanaktas.insurance.ui;

import com.ozanaktas.insurance.model.Role;
import com.ozanaktas.insurance.model.User;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

public class DashboardView {

    private final User user;

    private final Runnable onManageEmployees;
    private final Runnable onReports;
    private final Runnable onCustomerManagement;
    private final Runnable onCreatePolicy;
    private final Runnable onClaimsQueue;
    private final Runnable onMyPolicies;
    private final Runnable onCreateClaim;
    private final Runnable onLogout;

    public DashboardView(
            User user,
            Runnable onManageEmployees,
            Runnable onReports,
            Runnable onCustomerManagement,
            Runnable onCreatePolicy,
            Runnable onClaimsQueue,
            Runnable onMyPolicies,
            Runnable onCreateClaim,
            Runnable onLogout
    ) {
        this.user = user;
        this.onManageEmployees = onManageEmployees;
        this.onReports = onReports;
        this.onCustomerManagement = onCustomerManagement;
        this.onCreatePolicy = onCreatePolicy;
        this.onClaimsQueue = onClaimsQueue;
        this.onMyPolicies = onMyPolicies;
        this.onCreateClaim = onCreateClaim;
        this.onLogout = onLogout;
    }

    public Parent getView() {
        Label title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label who = new Label("Logged in as: " + user.getFullName() + " (" + user.getRole() + ")");
        who.setStyle("-fx-opacity: 0.85;");

        VBox actions = new VBox(8);
        actions.setPadding(new Insets(10, 0, 0, 0));
        actions.setFillWidth(true);

        if (user.getRole() == Role.ADMIN) {
            actions.getChildren().addAll(
                    new Label("Admin Actions:"),
                    makeButton("Manage Employees", onManageEmployees),
                    makeButton("View Reports", onReports),
                    makeButton("Claims Queue (FIFO)", onClaimsQueue),
                    new Label("Agent Actions (Admin can access):"),
                    makeButton("Customer Management", onCustomerManagement),
                    makeButton("Create Quote / Policy", onCreatePolicy)
            );
        } else if (user.getRole() == Role.AGENT) {
            actions.getChildren().addAll(
                    new Label("Agent Actions:"),
                    makeButton("Customer Management", onCustomerManagement),
                    makeButton("Create Quote / Policy", onCreatePolicy),
                    makeButton("Claims Queue (FIFO)", onClaimsQueue)
            );
        } else {
            actions.getChildren().addAll(
                    new Label("Customer Actions:"),
                    makeButton("My Policies", onMyPolicies),
                    makeButton("Create Claim", onCreateClaim)
            );
        }

        Button logout = new Button("Logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setOnAction(e -> onLogout.run());

        VBox root = new VBox(10, title, who, actions, logout);
        root.setPadding(new Insets(20));
        root.setPrefWidth(560);

        return root;
    }

    private Button makeButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setTooltip(new Tooltip(text));
        if (action == null) {
            btn.setDisable(true);
            btn.setTooltip(new Tooltip("Not implemented / not available"));
        } else {
            btn.setOnAction(e -> action.run());
        }
        if (btn.isDisabled()) {
            btn.setStyle("-fx-opacity: 0.6;");
        }
        return btn;
    }
}