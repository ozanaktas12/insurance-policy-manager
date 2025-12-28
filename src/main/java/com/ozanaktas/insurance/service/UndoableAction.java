package com.ozanaktas.insurance.service;

public interface UndoableAction {

    String description();

    void undo();
}