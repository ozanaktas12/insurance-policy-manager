package com.ozanaktas.insurance.service;

import com.ozanaktas.insurance.model.User;
import com.ozanaktas.insurance.repository.UserRepository;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    
    public Optional<User> login(String username, String password) {
        return loginWithMessage(username, password).userOpt();
    }

    
    public LoginResult loginWithMessage(String username, String password) {
        String u = (username == null) ? "" : username.trim();
        String p = (password == null) ? "" : password;

        if (u.isEmpty()) {
            return LoginResult.failure("Username cannot be empty.");
        }
        if (p.isEmpty()) {
            return LoginResult.failure("Password cannot be empty.");
        }

        Optional<User> userOpt = userRepository.findByUsername(u);
        if (userOpt.isEmpty()) {
            return LoginResult.failure("User not found.");
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(p)) {
            return LoginResult.failure("Wrong password.");
        }

        return LoginResult.success(user);
    }

    
    public static record LoginResult(boolean success, User user, String message) {
        public static LoginResult success(User user) {
            return new LoginResult(true, user, "Login successful.");
        }

        public static LoginResult failure(String message) {
            return new LoginResult(false, null, message);
        }

        public Optional<User> userOpt() {
            return Optional.ofNullable(user);
        }
    }
}