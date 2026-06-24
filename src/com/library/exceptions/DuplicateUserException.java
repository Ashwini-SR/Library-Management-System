package com.library.exceptions;

/** Thrown during registration when the chosen username is already in use. */
public class DuplicateUserException extends LibraryException {
    public DuplicateUserException(String username) {
        super("Username \"" + username + "\" is already taken. Please choose another.");
    }
}
