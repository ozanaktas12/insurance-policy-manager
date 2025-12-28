package com.ozanaktas.insurance.service;

import com.ozanaktas.insurance.model.InsuranceType;
import com.ozanaktas.insurance.model.PolicyStatus;
import com.ozanaktas.insurance.model.Policy;
import com.ozanaktas.insurance.repository.PolicyRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PolicyService {

    private final PolicyRepository policyRepository;

    public PolicyService(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    public List<Policy> getPoliciesForCustomer(String customerUsername) {
        return policyRepository.findByCustomerUsername(customerUsername);
    }

    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    public Optional<Policy> getByPolicyNo(String policyNo) {
        if (policyNo == null) return Optional.empty();
        return policyRepository.findByPolicyNo(policyNo.trim());
    }

    public boolean customerOwnsPolicy(String customerUsername, String policyNo) {
        if (customerUsername == null || policyNo == null) return false;

        return policyRepository.findByPolicyNo(policyNo.trim())
                .map(p -> p.getCustomerUsername().equals(customerUsername))
                .orElse(false);
    }

    public void addPolicy(Policy policy) {
        policyRepository.save(policy);
    }

    /**
     * Generates a new policy number like POL-3001.
     * Scans existing policies and returns the next available number.
     */
    public String generateNextPolicyNo() {
        int max = 0;
        for (Policy p : policyRepository.findAll()) {
            String no = p.getPolicyNo();
            if (no == null) continue;
            no = no.trim();

            
            if (no.startsWith("POL-")) {
                String tail = no.substring(4);
                try {
                    int n = Integer.parseInt(tail);
                    if (n > max) max = n;
                } catch (NumberFormatException ignored) {
                    
                }
            }
        }
        return "POL-" + (max + 1);
    }

    /**
     * Convenience method to create and store a policy in one call.
     */
    public Policy createPolicy(String customerUsername, InsuranceType type, double premium, LocalDate startDate, LocalDate endDate) {
        String policyNo = generateNextPolicyNo();
        Policy policy = new Policy(policyNo, customerUsername, type, premium, startDate, endDate);
        addPolicy(policy);
        return policy;
    }

    /**
     * Creates and stores a policy, and registers an undo action that will delete the created policy.
     * This is used to demonstrate Stack (LIFO) undo behavior in the UI.
     */
    public Policy createPolicyWithUndo(String customerUsername,
                                      InsuranceType type,
                                      double premium,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      UndoService undoService) {
        String policyNo = generateNextPolicyNo();
        Policy policy = new Policy(policyNo, customerUsername, type, premium, startDate, endDate);
        addPolicy(policy);

        if (undoService != null) {
            undoService.push(new UndoableAction() {
                @Override
                public String description() {
                    return "Undid create policy " + policyNo + " (deleted)";
                }

                @Override
                public void undo() {
                    boolean deleted = policyRepository.deleteByPolicyNo(policyNo);
                    if (!deleted) {
                        throw new IllegalStateException("Policy not found for undo: " + policyNo);
                    }
                }
            });
        }

        return policy;
    }
    /**
     * Cancels a policy (sets status to CANCELLED) and registers an undo action that restores
     * the previous status. Returns a user-friendly message.
     */
    public String cancelPolicyWithUndo(String policyNo, UndoService undoService) {
        if (policyNo == null || policyNo.trim().isEmpty()) {
            return "Policy No cannot be empty.";
        }

        String no = policyNo.trim();
        Optional<Policy> opt = policyRepository.findByPolicyNo(no);
        if (opt.isEmpty()) {
            return "Policy not found: " + no;
        }

        Policy policy = opt.get();
        PolicyStatus prev = policy.getStatus();

        if (prev == PolicyStatus.CANCELLED) {
            return "Policy is already cancelled: " + no;
        }

        policy.setStatus(PolicyStatus.CANCELLED);

        if (undoService != null) {
            undoService.push(new UndoableAction() {
                @Override
                public String description() {
                    return "Undid cancel policy " + no + " (restored to " + prev + ")";
                }

                @Override
                public void undo() {
                    
                    policy.setStatus(prev);
                }
            });
        }

        return "Policy cancelled ✅ (" + no + ")";
    }
}