package com.library.main;

import com.library.exceptions.*;
import com.library.models.Admin;
import com.library.models.Book;
import com.library.models.Student;
import com.library.models.Transaction;
import com.library.services.AuthService;
import com.library.services.LibraryService;
import com.library.utils.InputValidator;

import java.util.List;
import java.util.Scanner;

/**
 * Console menu shown to a logged-in {@link Admin}.
 * Covers: add / update / delete / search / view-all books, plus issuing
 * and returning books on behalf of any student.
 */
public class AdminConsole extends ConsoleMenu {

    private final Admin admin;
    private final LibraryService libraryService;
    private final AuthService authService;

    public AdminConsole(Admin admin, LibraryService libraryService, AuthService authService, Scanner scanner) {
        super(scanner);
        this.admin = admin;
        this.libraryService = libraryService;
        this.authService = authService;
    }

    @Override
    public void start() {
        boolean loggedIn = true;
        while (loggedIn) {
            printHeader("ADMIN DASHBOARD - Welcome, " + admin.getName());
            System.out.println(" 1. Add a new book");
            System.out.println(" 2. Update an existing book");
            System.out.println(" 3. Delete a book");
            System.out.println(" 4. Search books (by ID / title / author)");
            System.out.println(" 5. View all books");
            System.out.println(" 6. Issue a book to a student");
            System.out.println(" 7. Return a book on behalf of a student");
            System.out.println(" 8. View all transactions");
            System.out.println(" 9. View overdue books");
            System.out.println("10. View registered students");
            System.out.println(" 0. Logout");
            printDivider();

            int choice = InputValidator.readMenuChoice(scanner, "Choose an option: ", 0, 10);
            try {
                switch (choice) {
                    case 1 -> handleAddBook();
                    case 2 -> handleUpdateBook();
                    case 3 -> handleDeleteBook();
                    case 4 -> handleSearchBooks();
                    case 5 -> handleViewAllBooks();
                    case 6 -> handleIssueBook();
                    case 7 -> handleReturnBook();
                    case 8 -> handleViewAllTransactions();
                    case 9 -> handleViewOverdue();
                    case 10 -> handleViewStudents();
                    case 0 -> {
                        System.out.println("Logging out... Goodbye, " + admin.getName() + "!");
                        loggedIn = false;
                    }
                }
            } catch (LibraryException e) {
                // Every business-rule failure (duplicate ID, not found, no copies, etc.)
                // surfaces here as a clear message instead of crashing the application.
                System.out.println("\n[ERROR] " + e.getMessage());
                pause();
            } catch (Exception e) {
                // Final safety net so an unexpected error never kills the whole program.
                System.out.println("\n[UNEXPECTED ERROR] " + e.getMessage());
                pause();
            }
        }
    }

    private void handleAddBook() throws InvalidInputException, DuplicateBookException {
        printHeader("Add a New Book");
        String title = InputValidator.validateNotEmpty(
                InputValidator.readNonEmptyLine(scanner, "Title       : "), "Title");
        String author = InputValidator.validateNotEmpty(
                InputValidator.readNonEmptyLine(scanner, "Author      : "), "Author");
        String isbn = InputValidator.validateIsbn(
                InputValidator.readNonEmptyLine(scanner, "ISBN        : "));
        String category = InputValidator.validateNotEmpty(
                InputValidator.readNonEmptyLine(scanner, "Category    : "), "Category");
        String publisher = InputValidator.validateNotEmpty(
                InputValidator.readNonEmptyLine(scanner, "Publisher   : "), "Publisher");
        int copies = InputValidator.validatePositiveInt(
                InputValidator.readNonEmptyLine(scanner, "Total copies: "), "Total copies");

        Book added = libraryService.addBook(title, author, isbn, category, publisher, copies);
        System.out.println("\nBook added successfully with ID: " + added.getBookId());
        pause();
    }

    private void handleUpdateBook() throws BookNotFoundException, InvalidInputException {
        printHeader("Update a Book");
        String id = InputValidator.readNonEmptyLine(scanner, "Enter Book ID to update: ");
        Book existing = libraryService.getBookById(id); // throws if not found
        System.out.println("Current details: " + existing);
        System.out.println("Enter new values (press ENTER to keep the current value):");

        String title = promptWithDefault("Title", existing.getTitle());
        String author = promptWithDefault("Author", existing.getAuthor());
        String isbn = promptWithDefault("ISBN", existing.getIsbn());
        String category = promptWithDefault("Category", existing.getCategory());
        String publisher = promptWithDefault("Publisher", existing.getPublisher());
        String copiesRaw = promptWithDefault("Total copies", String.valueOf(existing.getTotalCopies()));
        int copies = InputValidator.validatePositiveInt(copiesRaw, "Total copies");

        libraryService.updateBook(id, title, author, isbn, category, publisher, copies);
        System.out.println("\nBook updated successfully.");
        pause();
    }

    private String promptWithDefault(String label, String currentValue) {
        System.out.print(label + " [" + currentValue + "]: ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? currentValue : input;
    }

    private void handleDeleteBook() throws BookNotFoundException, LibraryException {
        printHeader("Delete a Book");
        String id = InputValidator.readNonEmptyLine(scanner, "Enter Book ID to delete: ");
        Book book = libraryService.getBookById(id);
        System.out.println("About to delete: " + book);
        String confirm = InputValidator.readNonEmptyLine(scanner, "Type YES to confirm: ");
        if (confirm.equalsIgnoreCase("YES")) {
            libraryService.deleteBook(id);
            System.out.println("Book deleted.");
        } else {
            System.out.println("Deletion cancelled.");
        }
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

    private void handleViewAllBooks() {
        printHeader("All Books in Catalog");
        printBookTable(libraryService.getAllBooks());
        pause();
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

    private void handleIssueBook() throws BookNotFoundException, BookNotAvailableException,
            BorrowLimitExceededException, InvalidInputException {
        printHeader("Issue a Book to a Student");
        Student student = selectStudent();
        if (student == null) return;
        String bookId = InputValidator.readNonEmptyLine(scanner, "Enter Book ID to issue: ");
        Transaction t = libraryService.issueBook(bookId, student);
        System.out.println("\nIssued successfully. Transaction ID: " + t.getTransactionId()
                + " | Due back by: " + t.getDueDate());
        pause();
    }

    private void handleReturnBook() throws BookNotFoundException, TransactionNotFoundException {
        printHeader("Return a Book on Behalf of a Student");
        Student student = selectStudent();
        if (student == null) return;
        String bookId = InputValidator.readNonEmptyLine(scanner, "Enter Book ID to return: ");
        Transaction t = libraryService.returnBook(bookId, student);
        System.out.println("\nReturned successfully.");
        if (t.getFineAmount() > 0) {
            System.out.printf("This book was returned late. Fine due: %.2f%n", t.getFineAmount());
        }
        pause();
    }

    private Student selectStudent() {
        List<Student> students = authService.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("There are no registered students yet.");
            pause();
            return null;
        }
        System.out.println("Registered students:");
        for (Student s : students) {
            System.out.println("  " + s);
        }
        String userId = InputValidator.readNonEmptyLine(scanner, "Enter Student ID: ");
        for (Student s : students) {
            if (s.getUserId().equalsIgnoreCase(userId)) return s;
        }
        System.out.println("No student found with that ID.");
        pause();
        return null;
    }

    private void handleViewAllTransactions() {
        printHeader("All Transactions");
        List<Transaction> all = libraryService.getAllTransactions();
        if (all.isEmpty()) {
            System.out.println("No transactions recorded yet.");
        } else {
            all.forEach(System.out::println);
        }
        pause();
    }

    private void handleViewOverdue() {
        printHeader("Overdue Books");
        List<Transaction> overdue = libraryService.getOverdueTransactions();
        if (overdue.isEmpty()) {
            System.out.println("No overdue books. Everything is on time!");
        } else {
            overdue.forEach(System.out::println);
        }
        pause();
    }

    private void handleViewStudents() {
        printHeader("Registered Students");
        List<Student> students = authService.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students registered yet.");
        } else {
            students.forEach(System.out::println);
        }
        pause();
    }
}
