package com.library.models;

/**
 * Identifies what kind of account a {@link User} represents.
 * Used polymorphically: each {@code User} subclass reports its own
 * role via the abstract {@code getRole()} method.
 */
public enum Role {
    ADMIN,
    STUDENT
}
