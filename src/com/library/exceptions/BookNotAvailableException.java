package com.library.exceptions;

/** Thrown when a book exists but has zero available copies left to issue. */
public class BookNotAvailableException extends LibraryException {
    public BookNotAvailableException(String title) {
        super("No copies of \"" + title + "\" are currently available.");
    }
}
