package com.ozanaktas.insurance.model;

import java.time.LocalDate;
import java.util.Objects;

public class Policy {

    private final String policyNo;
    private final String customerUsername;
    private final InsuranceType type;
    private final double premium;
    private final LocalDate startDate;
    private final LocalDate endDate;

    private PolicyStatus status;

    public Policy(String policyNo, String customerUsername, InsuranceType type, double premium,
                  LocalDate startDate, LocalDate endDate) {
        
        this.policyNo = policyNo == null ? null : policyNo.trim();
        this.customerUsername = customerUsername == null ? null : customerUsername.trim();

        this.type = type;
        this.premium = premium;
        this.startDate = startDate;
        this.endDate = endDate;

        this.status = PolicyStatus.ACTIVE;
    }

    public String getPolicyNo() { return policyNo; }

    public String getCustomerUsername() { return customerUsername; }

    public InsuranceType getType() { return type; }

    public double getPremium() { return premium; }

    public LocalDate getStartDate() { return startDate; }

    public LocalDate getEndDate() { return endDate; }

    public PolicyStatus getStatus() { return status; }

    public void setStatus(PolicyStatus status) {
        // Keep it safe (avoid null status)
        this.status = (status == null) ? PolicyStatus.ACTIVE : status;
    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Policy policy = (Policy) o;
        return Objects.equals(policyNo, policy.policyNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(policyNo);
    }

    @Override
    public String toString() {
        String premiumText = "$" + String.format("%.2f", premium);
        return policyNo + " | " + type + " | " + premiumText + " | " + status
                + " | " + startDate + " → " + endDate;
    }
}