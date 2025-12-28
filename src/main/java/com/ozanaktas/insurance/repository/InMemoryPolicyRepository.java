package com.ozanaktas.insurance.repository;

import com.ozanaktas.insurance.model.Policy;

import java.util.*;

public class InMemoryPolicyRepository implements PolicyRepository {

    private final Map<String, Policy> map = new HashMap<>();

    @Override
    public void save(Policy policy) {
        if (policy == null || policy.getPolicyNo() == null) return;
        map.put(policy.getPolicyNo().trim(), policy);
    }

    @Override
    public Optional<Policy> findByPolicyNo(String policyNo) {
        if (policyNo == null) return Optional.empty();
        return Optional.ofNullable(map.get(policyNo.trim()));
    }

    @Override
    public boolean existsByPolicyNo(String policyNo) {
        if (policyNo == null) return false;
        return map.containsKey(policyNo.trim());
    }

    @Override
    public boolean deleteByPolicyNo(String policyNo) {
        if (policyNo == null) return false;
        return map.remove(policyNo.trim()) != null;
    }

    @Override
    public List<Policy> findByCustomerUsername(String customerUsername) {
        List<Policy> res = new ArrayList<>();
        for (Policy p : map.values()) {
            if (p.getCustomerUsername().equals(customerUsername)) {
                res.add(p);
            }
        }
        
        res.sort(Comparator.comparing(Policy::getPolicyNo));
        return res;
    }

    @Override
    public List<Policy> findAll() {
        return new ArrayList<>(map.values());
    }
}