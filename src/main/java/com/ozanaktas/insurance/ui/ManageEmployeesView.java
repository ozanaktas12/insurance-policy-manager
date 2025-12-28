package com.ozanaktas.insurance.ui;

import com.ozanaktas.insurance.model.Agent;
import com.ozanaktas.insurance.model.Role;
import com.ozanaktas.insurance.model.User;
import com.ozanaktas.insurance.repository.UserRepository;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ManageEmployeesView {

    private final User currentUser;
    private final UserRepository userRepository;
    private final Runnable onBack;

    private final ListView<User> agentsList = new ListView<>();
    private final Label message = new Label();

    public ManageEmployeesView(User currentUser, UserRepository userRepository, Runnable onBack) {
        this.currentUser = currentUser;
        this.userRepository = userRepository;
        this.onBack = onBack;
    }

    public Parent getView() {
        Label title = new Label("Manage Employees");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label who = new Label("Logged in: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        who.setStyle("-fx-opacity: 0.85;");

        // Message label default style
        message.setStyle("-fx-text-fill: #b00020;");

        boolean allowed = currentUser.getRole() == Role.ADMIN;

        if (!allowed) {
            Label warn = new Label("You are not allowed to manage employees. (Admin only)");
            warn.setStyle("-fx-text-fill: #b00020;");

            Button back = new Button("Back");
            back.setOnAction(e -> onBack.run());

            VBox root = new VBox(12, title, who, warn, back);
            root.setPadding(new Insets(20));
            root.setPrefWidth(760);
            root.setPrefHeight(520);
            return root;
        }

        // --- Add agent form ---
        TextField usernameField = new TextField();
        usernameField.setPromptText("agent username (e.g. agent2)");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("password");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("full name (e.g. Ozan Agent 2)");

        Button add = new Button("Add Agent");
        add.setMaxWidth(Double.MAX_VALUE);

        add.setOnAction(e -> {
            String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
            String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
            String fullName = fullNameField.getText() == null ? "" : fullNameField.getText().trim();

            if (username.isEmpty()) {
                setError("Username cannot be empty.");
                return;
            }
            if (password.isEmpty()) {
                setError("Password cannot be empty.");
                return;
            }
            if (fullName.isEmpty()) {
                setError("Full name cannot be empty.");
                return;
            }

            if (userRepository.existsByUsername(username)) {
                setError("This username already exists: " + username);
                return;
            }

            userRepository.save(new Agent(username, password, fullName));
            setSuccess("Agent added.  (" + username + ") — login: " + username + "/" + password);

            usernameField.clear();
            passwordField.clear();
            fullNameField.clear();

            refresh();
            selectUser(username);
        });

        VBox form = new VBox(8,
                new Label("Add New Agent"),
                usernameField,
                passwordField,
                fullNameField,
                add
        );
        form.setPadding(new Insets(12));
        form.setStyle("-fx-border-color: rgba(0,0,0,0.15); -fx-border-radius: 6; -fx-background-radius: 6;");

        agentsList.setPlaceholder(new Label("No agents yet"));

        agentsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getUsername() + " — " + item.getFullName());
                }
            }
        });

        Button delete = new Button("Delete Selected Agent");
        delete.setMaxWidth(Double.MAX_VALUE);

        delete.setOnAction(e -> {
            User selected = agentsList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                setError("Select an agent first.");
                return;
            }
            if (selected.getRole() != Role.AGENT) {
                setError("Only AGENT users can be deleted from this screen.");
                return;
            }

            boolean removed = userRepository.deleteByUsername(selected.getUsername());
            if (removed) {
                setSuccess("Agent deleted.  (" + selected.getUsername() + ")");
            } else {
                setError("Delete failed: user not found.");
            }
            refresh();
        });

        Button back = new Button("Back");
        back.setMaxWidth(Double.MAX_VALUE);
        back.setOnAction(e -> onBack.run());

        HBox actions = new HBox(10, delete, back);
        HBox.setHgrow(delete, Priority.ALWAYS);
        HBox.setHgrow(back, Priority.ALWAYS);

        VBox listBox = new VBox(8,
                new Label("Agents"),
                agentsList,
                actions
        );

        agentsList.setPrefHeight(300);

        Label tip = new Label("Tip: Admin can add/remove AGENT users here. Agents can add/remove CUSTOMER users from Customer Management.");
        tip.setStyle("-fx-opacity: 0.85;");

        VBox root = new VBox(12, title, who, tip, form, listBox, message);
        root.setPadding(new Insets(20));
        root.setPrefWidth(760);
        root.setPrefHeight(620);

        refresh();
        return root;
    }

    private void refresh() {
        List<User> agents = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.AGENT)
                .sorted(Comparator.comparing(User::getUsername))
                .collect(Collectors.toList());

        agentsList.getItems().setAll(agents);
    }

    private void setError(String text) {
        message.setStyle("-fx-text-fill: #b00020;");
        message.setText(text);
    }

    private void setSuccess(String text) {
        message.setStyle("-fx-text-fill: #1b5e20;");
        message.setText(text);
    }

    private void selectUser(String username) {
        if (username == null) return;
        String u = username.trim();

        for (User item : agentsList.getItems()) {
            if (item != null && u.equals(item.getUsername())) {
                agentsList.getSelectionModel().select(item);
                agentsList.scrollTo(item);
                return;
            }
        }
    }
}
