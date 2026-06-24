package com.library.exceptions;

/** Thrown when a return is attempted but no matching active "ISSUED" transaction exists. */
public class TransactionNotFoundException extends LibraryException {
    public TransactionNotFoundException(String bookId, String userId) {
        super("No active issue record found for book " + bookId + " against user " + userId + ".");
    }
}
