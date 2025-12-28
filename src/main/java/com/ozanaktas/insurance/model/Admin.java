package com.ozanaktas.insurance.model;

public class Admin extends User {
    public Admin(String username, String password, String fullName) {
        super(username, password, fullName, Role.ADMIN);
    }
}