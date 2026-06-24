package com.library.utils;

/**
 * Centralized configuration constants. Declared {@code final} with a
 * private constructor so it can never be instantiated — it exists purely
 * as a namespace of static values (a common, simple form of Encapsulation
 * for cross-cutting configuration).
 */
public final class Constants {

    private Constants() { /* utility class — no instances */ }

    public static final String DATA_DIR = "data";
    public static final String BOOKS_FILE = DATA_DIR + "/books.txt";
    public static final String USERS_FILE = DATA_DIR + "/users.txt";
    public static final String TRANSACTIONS_FILE = DATA_DIR + "/transactions.txt";

    /** Regex used with String.split(); a literal pipe must be escaped. */
    public static final String FIELD_DELIMITER = "\\|";
    /** Literal delimiter used with String.join() when writing lines back out. */
    public static final String FIELD_JOIN_DELIMITER = "|";

    public static final int MAX_BORROW_LIMIT = 3;
    public static final int BORROW_DURATION_DAYS = 14;
    public static final double FINE_PER_DAY = 5.0;
}
