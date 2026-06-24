package com.library.exceptions;

/** Thrown when trying to add a book whose ID already exists in the catalog. */
public class DuplicateBookException extends LibraryException {
    public DuplicateBookException(String bookId) {
        super("A book with ID " + bookId + " already exists.");
    }
}
