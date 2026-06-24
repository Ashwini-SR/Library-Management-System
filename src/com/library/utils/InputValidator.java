package com.library.utils;

import com.library.exceptions.InvalidInputException;

import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Static helpers that validate raw console input and convert it into a
 * usable typed value, throwing {@link InvalidInputException} on bad input.
 * Centralizing validation here keeps the menu classes free of repeated
 * parsing/try-catch boilerplate.
 */
public final class InputValidator {

    private static final Pattern ISBN_PATTERN = Pattern.compile("^[0-9Xx-]{10,17}$");

    private InputValidator() { /* utility class */ }

    public static String validateNotEmpty(String value, String fieldName) throws InvalidInputException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }

    public static String validateIsbn(String value) throws InvalidInputException {
        String trimmed = validateNotEmpty(value, "ISBN");
        if (!ISBN_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidInputException("ISBN must be 10-17 characters (digits, optional 'X' or '-').");
        }
        return trimmed;
    }

    public static int validatePositiveInt(String value, String fieldName) throws InvalidInputException {
        try {
            int n = Integer.parseInt(value.trim());
            if (n <= 0) {
                throw new InvalidInputException(fieldName + " must be a positive whole number.");
            }
            return n;
        } catch (NumberFormatException e) {
            throw new InvalidInputException(fieldName + " must be a whole number.");
        }
    }

    public static int validateNonNegativeInt(String value, String fieldName) throws InvalidInputException {
        try {
            int n = Integer.parseInt(value.trim());
            if (n < 0) {
                throw new InvalidInputException(fieldName + " cannot be negative.");
            }
            return n;
        } catch (NumberFormatException e) {
            throw new InvalidInputException(fieldName + " must be a whole number.");
        }
    }

    /**
     * Reads an integer menu choice within [min, max] from the scanner, looping
     * and re-prompting on bad input instead of throwing — appropriate for the
     * top-level menu loop where we never want to crash on a stray keystroke.
     */
    public static int readMenuChoice(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(raw);
                if (choice < min || choice > max) {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return choice;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input - please enter a number.");
            }
        }
    }

    /** Reads a line and re-prompts until it is non-empty. */
    public static String readNonEmptyLine(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("This field cannot be empty. Please try again.");
        }
    }
}
