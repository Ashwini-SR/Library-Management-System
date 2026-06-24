package com.library.models;

import com.library.utils.Constants;

/**
 * Represents a single book title in the library catalog.
 *
 * <p><b>Encapsulation:</b> every field is private; all access goes through
 * getters/setters, and the class protects its own invariant (availableCopies
 * can never be negative or exceed totalCopies) inside {@link #issueCopy()},
 * {@link #returnCopy()} and {@link #setTotalCopies(int)} rather than trusting
 * outside callers to do the right thing.</p>
 */
public class Book {

    private String bookId;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private String publisher;
    private int totalCopies;
    private int availableCopies;

    public Book(String bookId, String title, String author, String isbn,
                String category, String publisher, int totalCopies, int availableCopies) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.publisher = publisher;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    /** Convenience constructor used when a brand-new book is added (all copies start available). */
    public Book(String bookId, String title, String author, String isbn,
                String category, String publisher, int totalCopies) {
        this(bookId, title, author, isbn, category, publisher, totalCopies, totalCopies);
    }

    // ---------------------------------------------------------------- getters
    public String getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public String getCategory() { return category; }
    public String getPublisher() { return publisher; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }

    // ---------------------------------------------------------------- setters
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setCategory(String category) { this.category = category; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    /**
     * Updates the total copy count while keeping the available count consistent.
     * If the catalog grows, the new copies become immediately available.
     * If it shrinks, available copies shrink too but never below zero.
     */
    public void setTotalCopies(int newTotal) {
        int delta = newTotal - this.totalCopies;
        this.totalCopies = newTotal;
        this.availableCopies = Math.max(0, Math.min(newTotal, this.availableCopies + delta));
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    /** Number of copies currently on loan. */
    public int getIssuedCopies() {
        return totalCopies - availableCopies;
    }

    /** Decrements available copies when a copy is handed out. Caller must check {@link #isAvailable()} first. */
    public void issueCopy() {
        if (availableCopies > 0) {
            availableCopies--;
        }
    }

    /** Increments available copies when a copy comes back, never exceeding totalCopies. */
    public void returnCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    /** Serializes this book to a single pipe-delimited line for file storage. */
    public String toFileLine() {
        return String.join(Constants.FIELD_JOIN_DELIMITER,
                bookId, title, author, isbn, category, publisher,
                String.valueOf(totalCopies), String.valueOf(availableCopies));
    }

    /** Parses a pipe-delimited line (as produced by {@link #toFileLine()}) back into a Book. */
    public static Book fromFileLine(String line) {
        String[] p = line.split(Constants.FIELD_DELIMITER);
        return new Book(p[0], p[1], p[2], p[3], p[4], p[5],
                Integer.parseInt(p[6]), Integer.parseInt(p[7]));
    }

    @Override
    public String toString() {
        return String.format("%-6s | %-32s | %-20s | %-12s | %-15s | %3d/%3d available",
                bookId, truncate(title, 32), truncate(author, 20), category, isbn, availableCopies, totalCopies);
    }

    private static String truncate(String s, int len) {
        if (s == null) return "";
        return s.length() <= len ? s : s.substring(0, len - 1) + ".";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        return bookId.equals(((Book) o).bookId);
    }

    @Override
    public int hashCode() {
        return bookId.hashCode();
    }
}
