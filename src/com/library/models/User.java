package com.library.models;

import com.library.utils.Constants;

/**
 * Abstract base class for every account in the system.
 *
 * <p><b>Abstraction:</b> {@code User} defines the data and behaviour common
 * to all accounts (id, name, credentials) but deliberately leaves
 * {@link #getRole()} and the role-specific {@link #toString()} detail
 * unimplemented — each concrete subclass must decide what it means to
 * "be" that kind of user.</p>
 *
 * <p><b>Inheritance:</b> {@link Admin} and {@link Student} both extend this
 * class and reuse all of its fields/getters instead of duplicating them.</p>
 */
public abstract class User {

    private final String userId;
    private String name;
    private String username;
    private String password;

    protected User(String userId, String name, String username, String password) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }

    public boolean checkPassword(String candidate) {
        return this.password != null && this.password.equals(candidate);
    }

    /**
     * Polymorphic hook: every subclass reports which role it plays.
     * Callers can branch on this (or, better, just rely on overridden
     * behaviour elsewhere) without using {@code instanceof} everywhere.
     */
    public abstract Role getRole();

    /** Serializes this user to a single pipe-delimited line for file storage. */
    public String toFileLine() {
        return String.join(Constants.FIELD_JOIN_DELIMITER, userId, name, username, password, getRole().name());
    }

    /**
     * Factory method: parses a stored line and returns the correct concrete
     * subclass (Admin or Student) based on the role field — the caller never
     * needs to know which subclass it will get back.
     */
    public static User fromFileLine(String line) {
        String[] p = line.split(Constants.FIELD_DELIMITER);
        String id = p[0], name = p[1], username = p[2], password = p[3];
        Role role = Role.valueOf(p[4]);
        if (role == Role.ADMIN) {
            return new Admin(id, name, username, password);
        }
        return new Student(id, name, username, password);
    }

    @Override
    public String toString() {
        return String.format("%-6s | %-20s | %-12s | %s", userId, name, username, getRole());
    }
}
