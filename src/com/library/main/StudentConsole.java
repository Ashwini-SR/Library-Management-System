package com.library.main;

import com.library.exceptions.*;
import com.library.models.Book;
import com.library.models.Student;
import com.library.models.Transaction;
import com.library.services.LibraryService;
import com.library.utils.InputValidator;

import java.util.List;
import java.util.Scanner;

/**
 * Console menu shown to a logged-in {@link Student}.
 * Covers: viewing available books, searching by ID/title/author,
 * borrowing, and returning books.
 */
public class StudentConsole extends ConsoleMenu {

    private final Student student;
    private final LibraryService libraryService;

    public StudentConsole(Student student, LibraryService libraryService, Scanner scanner) {
        super(scanner);
        this.student = student;
        this.libraryService = libraryService;
    }

    @Override
    public void start() {
        boolean loggedIn = true;
        while (loggedIn) {
            printHeader("STUDENT DASHBOARD - Welcome, " + student.getName());
            System.out.println(" 1. View available books");
            System.out.println(" 2. Search books (by ID / title / author)");
            System.out.println(" 3. Borrow a book");
            System.out.println(" 4. Return a book");
            System.out.println(" 5. View my borrowed books");
            System.out.println(" 6. View my borrowing history");
            System.out.println(" 0. Logout");
            printDivider();

            int choice = InputValidator.readMenuChoice(scanner, "Choose an option: ", 0, 6);
            try {
                switch (choice) {
                    case 1 -> handleViewAvailableBooks();
                    case 2 -> handleSearchBooks();
                    case 3 -> handleBorrowBook();
                    case 4 -> handleReturnBook();
                    case 5 -> handleViewMyBooks();
                    case 6 -> handleViewHistory();
                    case 0 -> {
                        System.out.println("Logging out... Goodbye, " + student.getName() + "!");
                        loggedIn = false;
                    }
                }
            } catch (LibraryException e) {
                System.out.println("\n[ERROR] " + e.getMessage());
                pause();
            } catch (Exception e) {
                System.out.println("\n[UNEXPECTED ERROR] " + e.getMessage());
                pause();
            }
        }
    }

    private void handleViewAvailableBooks() {
        printHeader("Available Books");
        printBookTable(libraryService.getAvailableBooks());
        pause();
    }

    private void handleSearchBooks() {
        printHeader("Search Books");
        System.out.println("1. By Book ID   2. By Title   3. By Author   4. Any field");
        int mode = InputValidator.readMenuChoice(scanner, "Choose search type: ", 1, 4);
        String keyword = InputValidator.readNonEmptyLine(scanner, "Enter search keyword: ");

        List<Book> results = switch (mode) {
            case 1 -> safeFindById(keyword);
            case 2 -> libraryService.searchByTitle(keyword);
            case 3 -> libraryService.searchByAuthor(keyword);
            default -> libraryService.searchBooks(keyword);
        };
        printBookTable(results);
        pause();
    }

    private List<Book> safeFindById(String id) {
        try {
            return List.of(libraryService.getBookById(id));
        } catch (BookNotFoundException e) {
            return List.of();
        }
    }

    private void printBookTable(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }
        System.out.printf("%-6s | %-32s | %-20s | %-12s | %-15s | %s%n",
                "ID", "Title", "Author", "Category", "ISBN", "Availability");
        printDivider();
        for (Book b : books) {
            System.out.println(b);
        }
    }

    private void handleBorrowBook() throws BookNotFoundException, BookNotAvailableException,
            BorrowLimitExceededException {
        printHeader("Borrow a Book");
        System.out.println("You currently have " + student.getBorrowedCount() + " book(s) on loan "
                + "(limit: " + com.library.utils.Constants.MAX_BORROW_LIMIT + ").");
        String bookId = InputValidator.readNonEmptyLine(scanner, "Enter Book ID to borrow: ");
        Transaction t = libraryService.issueBook(bookId, student);
        System.out.println("\nBorrowed successfully! Please return by: " + t.getDueDate());
        pause();
    }

    private void handleReturnBook() throws BookNotFoundException, TransactionNotFoundException {
        printHeader("Return a Book");
        if (student.getBorrowedBookIds().isEmpty()) {
            System.out.println("You have no books currently borrowed.");
            pause();
            return;
        }
        System.out.println("Books currently borrowed by you: " + student.getBorrowedBookIds());
        String bookId = InputValidator.readNonEmptyLine(scanner, "Enter Book ID to return: ");
        Transaction t = libraryService.returnBook(bookId, student);
        System.out.println("\nReturned successfully.");
        if (t.getFineAmount() > 0) {
            System.out.printf("Note: this was returned late. Fine due: %.2f%n", t.getFineAmount());
        } else {
            System.out.println("Returned on time - no fine. Thank you!");
        }
        pause();
    }

    private void handleViewMyBooks() {
        printHeader("My Borrowed Books");
        if (student.getBorrowedBookIds().isEmpty()) {
            System.out.println("You have no books currently borrowed.");
        } else {
            for (String bookId : student.getBorrowedBookIds()) {
                try {
                    System.out.println(libraryService.getBookById(bookId));
                } catch (BookNotFoundException e) {
                    System.out.println(bookId + " (details unavailable)");
                }
            }
        }
        pause();
    }

    private void handleViewHistory() {
        printHeader("My Borrowing History");
        List<Transaction> history = libraryService.getTransactionsByUser(student.getUserId());
        if (history.isEmpty()) {
            System.out.println("You have no borrowing history yet.");
        } else {
            history.forEach(System.out::println);
        }
        pause();
    }
}
