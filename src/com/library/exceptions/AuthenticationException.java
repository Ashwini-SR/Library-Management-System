package com.library.exceptions;

/** Thrown when login credentials are invalid. */
public class AuthenticationException extends LibraryException {
    public AuthenticationException(String message) {
        super(message);
    }
}
