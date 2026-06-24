package com.library.exceptions;

/** Thrown when a student attempts to borrow more books than the allowed limit. */
public class BorrowLimitExceededException extends LibraryException {
    public BorrowLimitExceededException(int limit) {
        super("Borrow limit exceeded. A student may hold at most " + limit + " book(s) at a time.");
    }
}
