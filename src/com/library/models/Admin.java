package com.library.models;

/**
 * Concrete account type for library administrators.
 * Adds no extra state of its own — an Admin is fully described by the
 * common {@link User} fields — but provides its own {@link #getRole()}
 * and {@link #toString()}, which is Method Overriding (runtime polymorphism).
 */
public class Admin extends User {

    public Admin(String userId, String name, String username, String password) {
        super(userId, name, username, password);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    @Override
    public String toString() {
        return "[ADMIN] " + super.toString();
    }
}
