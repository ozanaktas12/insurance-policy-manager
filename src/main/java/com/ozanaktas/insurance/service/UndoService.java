package com.ozanaktas.insurance.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public class UndoService {

    private final Deque<UndoableAction> stack = new ArrayDeque<>();

    public void push(UndoableAction action) {
        if (action != null) {
            stack.push(action);
        }
    }

    public boolean canUndo() {
        return !stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }

    public void clear() {
        stack.clear();
    }

    public Optional<String> peekNextDescription() {
        if (stack.isEmpty()) return Optional.empty();
        return Optional.ofNullable(stack.peek()).map(UndoableAction::description);
    }

    public Optional<String> undoLast() {
        if (stack.isEmpty()) return Optional.empty();

        UndoableAction action = stack.pop();
        try {
            action.undo();
            return Optional.ofNullable(action.description());
        } catch (RuntimeException ex) {          
            stack.push(action);
            return Optional.of("Undo failed: " + ex.getClass().getSimpleName());
        }
    }
}