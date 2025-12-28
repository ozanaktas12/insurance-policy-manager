package com.ozanaktas.insurance.repository;

import com.ozanaktas.insurance.model.Policy;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository {
    void save(Policy policy);

    Optional<Policy> findByPolicyNo(String policyNo);

    boolean existsByPolicyNo(String policyNo);

    
    boolean deleteByPolicyNo(String policyNo);

    List<Policy> findByCustomerUsername(String customerUsername);

    List<Policy> findAll();
}