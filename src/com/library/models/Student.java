package com.library.models;

import com.library.exceptions.BorrowLimitExceededException;
import com.library.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete account type for students.
 *
 * <p>Unlike {@link Admin}, a Student carries extra state of its own — the
 * set of book IDs currently on loan — together with the behaviour that
 * protects it (the borrow limit check). Keeping the rule inside the class
 * that owns the data is a textbook example of Encapsulation: nothing
 * outside this class can push it into an invalid state.</p>
 *
 * <p>This in-memory list is rebuilt from the transaction history each time
 * the application starts (see {@code LibraryService#syncStudentLoans}); it
 * is not separately persisted, so there is a single source of truth.</p>
 */
public class Student extends User {

    private final List<String> borrowedBookIds = new ArrayList<>();

    public Student(String userId, String name, String username, String password) {
        super(userId, name, username, password);
    }

    @Override
    public Role getRole() {
        return Role.STUDENT;
    }

    /** Read-only view of the books this student currently has on loan. */
    public List<String> getBorrowedBookIds() {
        return Collections.unmodifiableList(borrowedBookIds);
    }

    public int getBorrowedCount() {
        return borrowedBookIds.size();
    }

    public boolean hasBorrowed(String bookId) {
        return borrowedBookIds.contains(bookId);
    }

    /**
     * Records a new borrow, enforcing the maximum-books-per-student rule.
     * @throws BorrowLimitExceededException if the student is already at the limit
     */
    public void borrowBook(String bookId) throws BorrowLimitExceededException {
        if (borrowedBookIds.size() >= Constants.MAX_BORROW_LIMIT) {
            throw new BorrowLimitExceededException(Constants.MAX_BORROW_LIMIT);
        }
        borrowedBookIds.add(bookId);
    }

    /** Used only to rebuild state from saved transaction history at startup — bypasses the limit check. */
    public void restoreBorrowedBook(String bookId) {
        if (!borrowedBookIds.contains(bookId)) {
            borrowedBookIds.add(bookId);
        }
    }

    public void returnBook(String bookId) {
        borrowedBookIds.remove(bookId);
    }

    @Override
    public String toString() {
        return "[STUDENT] " + super.toString() + String.format("  (%d/%d books on loan)",
                borrowedBookIds.size(), Constants.MAX_BORROW_LIMIT);
    }
}
