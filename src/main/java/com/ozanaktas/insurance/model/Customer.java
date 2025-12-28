package com.ozanaktas.insurance.model;

public class Customer extends User {
    public Customer(String username, String password, String fullName) {
        super(username, password, fullName, Role.CUSTOMER);
    }
}