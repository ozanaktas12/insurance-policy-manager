package com.ozanaktas.insurance.ui;

import com.ozanaktas.insurance.model.Customer;
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

public class CustomerManagementView {

    private final User currentUser;
    private final UserRepository userRepository;
    private final Runnable onBack;

    private final ListView<User> customersList = new ListView<>();
    private final Label message = new Label();

    public CustomerManagementView(User currentUser, UserRepository userRepository, Runnable onBack) {
        this.currentUser = currentUser;
        this.userRepository = userRepository;
        this.onBack = onBack;
    }

    public Parent getView() {
        Label title = new Label("Customer Management");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label who = new Label("Logged in: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        who.setStyle("-fx-opacity: 0.85;");

        // Message label: we'll color it green for success, red for errors
        message.setStyle("-fx-text-fill: #b00020;");

        boolean allowed = currentUser.getRole() == Role.AGENT || currentUser.getRole() == Role.ADMIN;

        if (!allowed) {
            Label warn = new Label("You are not allowed to manage customers. (Agent/Admin only)");
            warn.setStyle("-fx-text-fill: #b00020;");

            Button back = new Button("Back");
            back.setOnAction(e -> onBack.run());

            VBox root = new VBox(12, title, who, warn, back);
            root.setPadding(new Insets(20));
            return root;
        }

        // --- Add customer form ---
        TextField usernameField = new TextField();
        usernameField.setPromptText("username (e.g. ali)");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("password");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("full name (e.g. Ali Veli)");

        Button add = new Button("Add Customer");
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

            userRepository.save(new Customer(username, password, fullName));
            setSuccess("Customer added. (" + username + ") — login: " + username + "/" + password);

            usernameField.clear();
            passwordField.clear();
            fullNameField.clear();

            refresh();
            selectCustomer(username);
        });

        VBox form = new VBox(8,
                new Label("Add New Customer"),
                usernameField,
                passwordField,
                fullNameField,
                add
        );
        form.setPadding(new Insets(12));
        form.setStyle("-fx-border-color: rgba(0,0,0,0.15); -fx-border-radius: 6; -fx-background-radius: 6;");

        // --- List + delete ---
        customersList.setPlaceholder(new Label("No customers yet"));

        // Make list items readable even if User.toString() isn't overridden
        customersList.setCellFactory(lv -> new ListCell<>() {
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

        Button delete = new Button("Delete Selected Customer");
        delete.setMaxWidth(Double.MAX_VALUE);

        delete.setOnAction(e -> {
            User selected = customersList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                setError("Select a customer first.");
                return;
            }
            if (selected.getRole() != Role.CUSTOMER) {
                setError("Only CUSTOMER users can be deleted from this screen.");
                return;
            }

            boolean removed = userRepository.deleteByUsername(selected.getUsername());
            if (removed) {
                setSuccess("Customer deleted.  (" + selected.getUsername() + ")");
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
                new Label("Customers"),
                customersList,
                actions
        );

        customersList.setPrefHeight(320);

        VBox root = new VBox(12, title, who, form, listBox, message);
        root.setPadding(new Insets(20));
        root.setPrefWidth(760);
        root.setPrefHeight(600);

        refresh();
        return root;
    }

    private void refresh() {
        List<User> customers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.CUSTOMER)
                .sorted(Comparator.comparing(User::getUsername))
                .collect(Collectors.toList());

        customersList.getItems().setAll(customers);
    }

    private void setError(String text) {
        message.setStyle("-fx-text-fill: #b00020;");
        message.setText(text);
    }

    private void setSuccess(String text) {
        message.setStyle("-fx-text-fill: #1b5e20;");
        message.setText(text);
    }

    private void selectCustomer(String username) {
        if (username == null) return;
        String u = username.trim();

        for (User item : customersList.getItems()) {
            if (item != null && u.equals(item.getUsername())) {
                customersList.getSelectionModel().select(item);
                customersList.scrollTo(item);
                return;
            }
        }
    }
}
