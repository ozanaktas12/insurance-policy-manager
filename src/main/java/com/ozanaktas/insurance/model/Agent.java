package com.ozanaktas.insurance.model;

public class Agent extends User {
    public Agent(String username, String password, String fullName) {
        super(username, password, fullName, Role.AGENT);
    }
}