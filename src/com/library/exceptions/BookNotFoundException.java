package com.library.exceptions;

/** Thrown when a requested book id does not exist in the catalog. */
public class BookNotFoundException extends LibraryException {
    public BookNotFoundException(String bookId) {
        super("Book not found with ID: " + bookId);
    }
}
