package com.ozanaktas.insurance.ui;

import com.ozanaktas.insurance.model.User;
import com.ozanaktas.insurance.service.AuthService;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class LoginView {

    private final AuthService authService;
    private final Consumer<User> onLoginSuccess;

    public LoginView(AuthService authService, Consumer<User> onLoginSuccess) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;
    }

    public Parent getView() {
        Label title = new Label("Insurance Policy Manager - Login");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username (e.g. admin)");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (e.g. admin123)");

        Button loginBtn = new Button("Login");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Label message = new Label();
        message.setStyle("-fx-text-fill: #b00020;");

        // Demo hint
        Label demo = new Label("Demo users:\nadmin/admin123\nagent/agent123\ncustomer/customer123\n\nTip: Agent/Admin can add new customers from Customer Management.");
        demo.setStyle("-fx-opacity: 0.8;");

        loginBtn.setOnAction(e -> {
            String u = usernameField.getText();
            String p = passwordField.getText();

            var result = authService.loginWithMessage(u, p);
            if (result.success()) {
                message.setText("");
                onLoginSuccess.accept(result.user());
            } else {
                message.setText(result.message());
            }
        });

        VBox root = new VBox(10, title, usernameField, passwordField, loginBtn, message, demo);
        root.setPadding(new Insets(20));
        root.setPrefWidth(520);

        return root;
    }
}