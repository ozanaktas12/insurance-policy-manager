package com.ozanaktas.insurance.repository;

import com.ozanaktas.insurance.model.User;

import java.util.*;

public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> users = new HashMap<>();

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable(users.get(username.trim()));
    }

    @Override
    public void save(User user) {
        if (user == null || user.getUsername() == null) return;
        users.put(user.getUsername().trim(), user);
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null) return false;
        return users.containsKey(username.trim());
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public List<String> findAllUsernames() {
        List<String> list = new ArrayList<>(users.keySet());
        Collections.sort(list);
        return list;
    }

    @Override
    public boolean deleteByUsername(String username) {
        if (username == null) return false;
        return users.remove(username.trim()) != null;
    }
}