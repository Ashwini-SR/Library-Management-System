package com.library.models;

import com.library.utils.Constants;

import java.time.LocalDate;

/**
 * Represents one borrow event: which book, which user, when it was issued,
 * when it is due, and (once returned) when it actually came back and any
 * fine incurred. Encapsulates its own fine calculation so callers never
 * have to duplicate that arithmetic.
 */
public class Transaction {

    private final String transactionId;
    private final String bookId;
    private final String userId;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;
    private TransactionStatus status;
    private double fineAmount;

    public Transaction(String transactionId, String bookId, String userId,
                        LocalDate issueDate, LocalDate dueDate, LocalDate returnDate,
                        TransactionStatus status, double fineAmount) {
        this.transactionId = transactionId;
        this.bookId = bookId;
        this.userId = userId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.fineAmount = fineAmount;
    }

    /** Creates a brand-new ISSUED transaction starting today, due in {@link Constants#BORROW_DURATION_DAYS} days. */
    public static Transaction newIssue(String transactionId, String bookId, String userId) {
        LocalDate today = LocalDate.now();
        return new Transaction(transactionId, bookId, userId, today,
                today.plusDays(Constants.BORROW_DURATION_DAYS), null, TransactionStatus.ISSUED, 0.0);
    }

    public String getTransactionId() { return transactionId; }
    public String getBookId() { return bookId; }
    public String getUserId() { return userId; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public TransactionStatus getStatus() { return status; }
    public double getFineAmount() { return fineAmount; }

    public boolean isOverdue() {
        return status == TransactionStatus.ISSUED && LocalDate.now().isAfter(dueDate);
    }

    /** Marks the book as returned today and computes any late fine. */
    public void markReturned() {
        this.returnDate = LocalDate.now();
        this.status = TransactionStatus.RETURNED;
        long lateDays = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(dueDate, returnDate));
        this.fineAmount = lateDays * Constants.FINE_PER_DAY;
    }

    public String toFileLine() {
        return String.join(Constants.FIELD_JOIN_DELIMITER,
                transactionId, bookId, userId,
                issueDate.toString(), dueDate.toString(),
                returnDate == null ? "null" : returnDate.toString(),
                status.name(), String.valueOf(fineAmount));
    }

    public static Transaction fromFileLine(String line) {
        String[] p = line.split(Constants.FIELD_DELIMITER);
        LocalDate returnDate = p[5].equals("null") ? null : LocalDate.parse(p[5]);
        return new Transaction(p[0], p[1], p[2], LocalDate.parse(p[3]), LocalDate.parse(p[4]),
                returnDate, TransactionStatus.valueOf(p[6]), Double.parseDouble(p[7]));
    }

    @Override
    public String toString() {
        String ret = returnDate == null ? "-" : returnDate.toString();
        return String.format("%-6s | Book:%-6s | User:%-6s | Issued:%s | Due:%s | Returned:%-10s | %-8s | Fine: %.2f",
                transactionId, bookId, userId, issueDate, dueDate, ret, status, fineAmount);
    }
}
