package com.library.services;

import com.library.exceptions.AuthenticationException;
import com.library.exceptions.DuplicateUserException;
import com.library.exceptions.InvalidInputException;
import com.library.models.Admin;
import com.library.models.Role;
import com.library.models.Student;
import com.library.models.User;
import com.library.utils.Constants;
import com.library.utils.IDGenerator;
import com.library.utils.InputValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Handles everything related to accounts: loading/saving them to disk,
 * authenticating a login attempt, and registering new students.
 *
 * Uses the Collections Framework (ArrayList, Comparator, Collections.sort)
 * to keep the in-memory user list manageable.
 */
public class AuthService {

    private final List<User> users;

    public AuthService() {
        FileService.ensureDataDirectoryExists();
        this.users = new ArrayList<>();
        load();
        if (users.isEmpty()) {
            seedDefaultAdmin();
        }
    }

    private void load() {
        for (String line : FileService.readLines(Constants.USERS_FILE)) {
            try {
                users.add(User.fromFileLine(line));
            } catch (Exception e) {
                System.err.println("Skipping malformed user record: " + line);
            }
        }
    }

    /** Guarantees the application is always usable even if users.txt is missing or empty. */
    private void seedDefaultAdmin() {
        users.add(new Admin("U001", "System Administrator", "admin", "admin123"));
        save();
    }

    public void save() {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(u.toFileLine());
        }
        FileService.writeLines(Constants.USERS_FILE, lines);
    }

    /**
     * Verifies a login attempt.
     * @throws AuthenticationException if the username does not exist or the password is wrong
     */
    public User authenticate(String username, String password) throws AuthenticationException {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                if (u.checkPassword(password)) {
                    return u;
                }
                throw new AuthenticationException("Incorrect password.");
            }
        }
        throw new AuthenticationException("No account found with username \"" + username + "\".");
    }

    public boolean isUsernameTaken(String username) {
        return users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }

    /**
     * Self-service registration for new students.
     * @throws DuplicateUserException if the username is already taken
     * @throws InvalidInputException  if any field fails basic validation
     */
    public Student registerStudent(String name, String username, String password)
            throws DuplicateUserException, InvalidInputException {
        InputValidator.validateNotEmpty(name, "Name");
        InputValidator.validateNotEmpty(username, "Username");
        InputValidator.validateNotEmpty(password, "Password");
        if (isUsernameTaken(username)) {
            throw new DuplicateUserException(username);
        }
        String id = IDGenerator.generate("U", users, User::getUserId);
        Student student = new Student(id, name, username, password);
        users.add(student);
        save();
        return student;
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        for (User u : users) {
            if (u.getRole() == Role.STUDENT) {
                students.add((Student) u);
            }
        }
        students.sort(Comparator.comparing(User::getName));
        return students;
    }

    public List<User> getAllUsers() {
        List<User> copy = new ArrayList<>(users);
        copy.sort(Comparator.comparing((User u) -> u.getRole().name()).thenComparing(User::getName));
        return Collections.unmodifiableList(copy);
    }
}
