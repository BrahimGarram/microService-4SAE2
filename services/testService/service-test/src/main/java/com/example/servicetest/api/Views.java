package com.example.servicetest.api;

/**
 * Vues JSON pour ne pas exposer testCasesJson au candidat (affectation).
 * Admin (getQuestionById) utilise Admin pour avoir les cas de test.
 */
public final class Views {
    public interface Public {}
    public interface Admin extends Public {}
}
