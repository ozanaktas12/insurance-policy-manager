package com.ozanaktas.insurance.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Claim {
    private final UUID id;
    private final String customerUsername;
    private final String policyNo;
    private final String description;
    private final double amount;
    private final LocalDateTime createdAt;

    private ClaimStatus status;

    public Claim(String customerUsername, String policyNo, String description, double amount) {
        this.id = UUID.randomUUID();
        this.customerUsername = customerUsername;
        this.policyNo = policyNo;
        this.description = description;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
        this.status = ClaimStatus.IN_QUEUE;
    }

    public UUID getId() { return id; }
    public String getCustomerUsername() { return customerUsername; }
    public String getPolicyNo() { return policyNo; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public ClaimStatus getStatus() { return status; }

    public void setStatus(ClaimStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "#" + id.toString().substring(0, 8)
                + " | " + customerUsername
                + " | " + policyNo
                + " | " + status
                + " | $" + amount;
    }
}