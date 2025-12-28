package com.ozanaktas.insurance.ui;

import com.ozanaktas.insurance.model.Policy;
import com.ozanaktas.insurance.model.PolicyStatus;
import com.ozanaktas.insurance.model.Role;
import com.ozanaktas.insurance.model.User;
import com.ozanaktas.insurance.repository.UserRepository;
import com.ozanaktas.insurance.service.ClaimQueueService;
import com.ozanaktas.insurance.service.PolicyService;
import com.ozanaktas.insurance.service.UndoService;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class ReportsView {

    private final User currentUser;
    private final PolicyService policyService;
    private final ClaimQueueService claimQueueService;
    private final UndoService undoService;
    private final UserRepository userRepository;
    private final Runnable onBack;

    // UI labels (updated by refresh)
    private final Label policiesTotal = new Label();
    private final Label policiesActive = new Label();
    private final Label policiesCancelled = new Label();
    private final Label premiumSum = new Label();

    private final Label claimsQueued = new Label();
    private final Label claimsProcessed = new Label();

    private final Label usersTotal = new Label();
    private final Label usersAdmins = new Label();
    private final Label usersAgents = new Label();
    private final Label usersCustomers = new Label();

    private final Label undoSize = new Label();
    private final Label undoNext = new Label();

    public ReportsView(User currentUser,
                       PolicyService policyService,
                       ClaimQueueService claimQueueService,
                       UndoService undoService,
                       UserRepository userRepository,
                       Runnable onBack) {
        this.currentUser = currentUser;
        this.policyService = policyService;
        this.claimQueueService = claimQueueService;
        this.undoService = undoService;
        this.userRepository = userRepository;
        this.onBack = onBack;
    }

    public Parent getView() {
        Label title = new Label("Reports");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label who = new Label("Logged in: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        who.setStyle("-fx-opacity: 0.85;");

        boolean allowed = currentUser.getRole() == Role.ADMIN;
        if (!allowed) {
            Label warn = new Label("You are not allowed to view reports. (Admin only)");
            warn.setStyle("-fx-text-fill: #b00020;");

            Button back = new Button("Back");
            back.setOnAction(e -> onBack.run());

            VBox root = new VBox(12, title, who, warn, back);
            root.setPadding(new Insets(20));
            root.setPrefWidth(760);
            root.setPrefHeight(520);
            return root;
        }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        int r = 0;
        r = row(grid, r, "Policies (total)", policiesTotal);
        r = row(grid, r, "Policies (active)", policiesActive);
        r = row(grid, r, "Policies (cancelled)", policiesCancelled);
        r = row(grid, r, "Total premium sum", premiumSum);

        r = spacer(grid, r);

        r = row(grid, r, "Claims queued (FIFO)", claimsQueued);
        r = row(grid, r, "Claims processed", claimsProcessed);

        r = spacer(grid, r);

        r = row(grid, r, "Users (total)", usersTotal);
        r = row(grid, r, "Users (admins)", usersAdmins);
        r = row(grid, r, "Users (agents)", usersAgents);
        r = row(grid, r, "Users (customers)", usersCustomers);

        r = spacer(grid, r);

        r = row(grid, r, "Undo stack size", undoSize);
        r = row(grid, r, "Next undo description", undoNext);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);

        Button refreshBtn = new Button("Refresh");
        Button backBtn = new Button("Back");
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setMaxWidth(Double.MAX_VALUE);

        refreshBtn.setOnAction(e -> refresh());
        backBtn.setOnAction(e -> onBack.run());

        HBox buttons = new HBox(10, refreshBtn, backBtn);
        HBox.setHgrow(refreshBtn, Priority.ALWAYS);
        HBox.setHgrow(backBtn, Priority.ALWAYS);

        VBox top = new VBox(8, title, who, new Separator());
        VBox bottom = new VBox(10, new Separator(), buttons);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(scroll);
        root.setBottom(bottom);

        BorderPane.setMargin(top, new Insets(20, 20, 0, 20));
        BorderPane.setMargin(scroll, new Insets(0, 20, 0, 20));
        BorderPane.setMargin(bottom, new Insets(0, 20, 20, 20));

        root.setPrefWidth(760);
        root.setPrefHeight(520);

        refresh();
        return root;
    }

    private void refresh() {
        List<Policy> policies = policyService.getAllPolicies();

        int total = policies.size();
        long active = policies.stream().filter(p -> p.getStatus() == PolicyStatus.ACTIVE).count();
        long cancelled = policies.stream().filter(p -> p.getStatus() == PolicyStatus.CANCELLED).count();
        double sumPremium = policies.stream().mapToDouble(Policy::getPremium).sum();

        policiesTotal.setText(String.valueOf(total));
        policiesActive.setText(String.valueOf(active));
        policiesCancelled.setText(String.valueOf(cancelled));
        premiumSum.setText("$" + String.format("%.2f", sumPremium));

        claimsQueued.setText(String.valueOf(claimQueueService.queuedCount()));
        claimsProcessed.setText(String.valueOf(claimQueueService.processedCount()));

        var users = userRepository.findAll();
        usersTotal.setText(String.valueOf(users.size()));
        usersAdmins.setText(String.valueOf(users.stream().filter(u -> u.getRole() == Role.ADMIN).count()));
        usersAgents.setText(String.valueOf(users.stream().filter(u -> u.getRole() == Role.AGENT).count()));
        usersCustomers.setText(String.valueOf(users.stream().filter(u -> u.getRole() == Role.CUSTOMER).count()));

        undoSize.setText(String.valueOf(undoService.size()));
        undoNext.setText(undoService.peekNextDescription().orElse("(none)"));
    }

    private int row(GridPane grid, int rowIndex, String left, Label rightLabel) {
        Label l = new Label(left);
        l.setStyle("-fx-opacity: 0.85;");

        rightLabel.setStyle("-fx-font-weight: bold;");

        grid.add(l, 0, rowIndex);
        grid.add(rightLabel, 1, rowIndex);
        return rowIndex + 1;
    }

    private int spacer(GridPane grid, int rowIndex) {
        grid.add(new Label(""), 0, rowIndex);
        return rowIndex + 1;
    }
}
