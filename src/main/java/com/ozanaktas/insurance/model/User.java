package com.ozanaktas.insurance.model;

public abstract class User {
    private final String username;
    private final String password; 
    private final String fullName;
    private final Role role;

    protected User(String username, String password, String fullName, Role role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
}