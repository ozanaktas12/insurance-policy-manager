package com.ozanaktas.insurance.repository;

import com.ozanaktas.insurance.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findAll();

    void save(User user);

    
    boolean deleteByUsername(String username);
}