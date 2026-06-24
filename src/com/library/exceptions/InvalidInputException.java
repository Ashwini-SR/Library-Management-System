package com.library.exceptions;

/** Thrown when user-supplied input fails a validation rule (empty, wrong format, out of range, etc.). */
public class InvalidInputException extends LibraryException {
    public InvalidInputException(String message) {
        super(message);
    }
}
