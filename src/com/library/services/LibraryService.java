package com.library.services;

import com.library.exceptions.*;
import com.library.models.Book;
import com.library.models.Student;
import com.library.models.Transaction;
import com.library.models.TransactionStatus;
import com.library.utils.Constants;
import com.library.utils.IDGenerator;

import java.util.*;

/**
 * Core business logic of the library: the book catalog and the
 * issue/return transaction log. Both Admin and Student menus drive their
 * book/borrow operations through this single class, which is the only
 * place that is allowed to mutate a {@link Book}'s copy counts or create
 * {@link Transaction} records — keeping all of the catalog's invariants
 * in one place instead of scattered across the UI layer.
 */
public class LibraryService {

    private final List<Book> books;
    private final List<Transaction> transactions;

    public LibraryService() {
        FileService.ensureDataDirectoryExists();
        this.books = new ArrayList<>();
        this.transactions = new ArrayList<>();
        loadBooks();
        loadTransactions();
        if (books.isEmpty()) {
            seedSampleBooks();
        }
    }

    // ----------------------------------------------------------------- load/save

    private void loadBooks() {
        for (String line : FileService.readLines(Constants.BOOKS_FILE)) {
            try {
                books.add(Book.fromFileLine(line));
            } catch (Exception e) {
                System.err.println("Skipping malformed book record: " + line);
            }
        }
    }

    private void loadTransactions() {
        for (String line : FileService.readLines(Constants.TRANSACTIONS_FILE)) {
            try {
                transactions.add(Transaction.fromFileLine(line));
            } catch (Exception e) {
                System.err.println("Skipping malformed transaction record: " + line);
            }
        }
    }

    /** Ships the application with a small, ready-to-use catalog if books.txt is missing/empty. */
    private void seedSampleBooks() {
        books.add(new Book("B001", "The Pragmatic Programmer", "Andrew Hunt", "9780201616224", "Computer Science", "Addison-Wesley", 5));
        books.add(new Book("B002", "Effective Java", "Joshua Bloch", "9780134685991", "Computer Science", "Addison-Wesley", 4));
        books.add(new Book("B003", "Clean Code", "Robert C. Martin", "9780132350884", "Computer Science", "Prentice Hall", 3));
        books.add(new Book("B004", "Introduction to Algorithms", "Thomas H. Cormen", "9780262033848", "Computer Science", "MIT Press", 2));
        books.add(new Book("B005", "The Great Gatsby", "F. Scott Fitzgerald", "9780743273565", "Fiction", "Scribner", 4));
        books.add(new Book("B006", "To Kill a Mockingbird", "Harper Lee", "9780061120084", "Fiction", "J.B. Lippincott", 3));
        books.add(new Book("B007", "A Brief History of Time", "Stephen Hawking", "9780553380163", "Science", "Bantam", 2));
        books.add(new Book("B008", "Sapiens", "Yuval Noah Harari", "9780062316097", "History", "Harper", 3));
        saveBooks();
    }

    public void saveBooks() {
        List<String> lines = new ArrayList<>();
        for (Book b : books) lines.add(b.toFileLine());
        FileService.writeLines(Constants.BOOKS_FILE, lines);
    }

    public void saveTransactions() {
        List<String> lines = new ArrayList<>();
        for (Transaction t : transactions) lines.add(t.toFileLine());
        FileService.writeLines(Constants.TRANSACTIONS_FILE, lines);
    }

    public void saveAll() {
        saveBooks();
        saveTransactions();
    }

    /**
     * Rebuilds every student's in-memory "currently borrowed" list from the
     * saved transaction history. Called once at startup, after both the
     * book catalog and the user list have been loaded.
     */
    public void syncStudentLoans(List<Student> students) {
        for (Transaction t : transactions) {
            if (t.getStatus() == TransactionStatus.ISSUED) {
                for (Student s : students) {
                    if (s.getUserId().equals(t.getUserId())) {
                        s.restoreBorrowedBook(t.getBookId());
                    }
                }
            }
        }
    }

    // ----------------------------------------------------------------- CRUD (Admin)

    /** @throws DuplicateBookException if a book with this ID already exists */
    public void addBook(Book book) throws DuplicateBookException {
        for (Book b : books) {
            if (b.getBookId().equalsIgnoreCase(book.getBookId())) {
                throw new DuplicateBookException(book.getBookId());
            }
        }
        books.add(book);
        saveBooks();
    }

    /** Overloaded convenience method: builds the Book (auto-generating its ID) and adds it in one call. */
    public Book addBook(String title, String author, String isbn, String category,
                         String publisher, int totalCopies) throws DuplicateBookException {
        String id = IDGenerator.generate("B", books, Book::getBookId);
        Book book = new Book(id, title, author, isbn, category, publisher, totalCopies);
        addBook(book);
        return book;
    }

    /** @throws BookNotFoundException if no book with this ID exists */
    public Book getBookById(String bookId) throws BookNotFoundException {
        for (Book b : books) {
            if (b.getBookId().equalsIgnoreCase(bookId)) {
                return b;
            }
        }
        throw new BookNotFoundException(bookId);
    }

    /**
     * Updates an existing book's details in place.
     * @throws BookNotFoundException if the ID does not exist
     */
    public void updateBook(String bookId, String title, String author, String isbn,
                           String category, String publisher, int totalCopies) throws BookNotFoundException {
        Book book = getBookById(bookId);
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setCategory(category);
        book.setPublisher(publisher);
        book.setTotalCopies(totalCopies);
        saveBooks();
    }

    /**
     * Removes a book from the catalog entirely.
     * @throws BookNotFoundException if the ID does not exist
     * @throws LibraryException if some copies are currently on loan (cannot safely delete)
     */
    public void deleteBook(String bookId) throws BookNotFoundException, LibraryException {
        Book book = getBookById(bookId);
        if (book.getIssuedCopies() > 0) {
            throw new LibraryException("Cannot delete \"" + book.getTitle() + "\" - "
                    + book.getIssuedCopies() + " copy(ies) are still on loan.");
        }
        books.remove(book);
        saveBooks();
    }

    // ----------------------------------------------------------------- viewing & search

    /** All books, sorted alphabetically by title. */
    public List<Book> getAllBooks() {
        List<Book> copy = new ArrayList<>(books);
        copy.sort(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
        return Collections.unmodifiableList(copy);
    }

    /** Only books that currently have at least one available copy. */
    public List<Book> getAvailableBooks() {
        List<Book> available = new ArrayList<>();
        for (Book b : books) {
            if (b.isAvailable()) available.add(b);
        }
        available.sort(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
        return available;
    }

    public List<Book> searchByTitle(String keyword) {
        return filter(b -> contains(b.getTitle(), keyword));
    }

    public List<Book> searchByAuthor(String keyword) {
        return filter(b -> contains(b.getAuthor(), keyword));
    }

    /** Overloaded search: a single keyword matched against ID, title, AND author at once. */
    public List<Book> searchBooks(String keyword) {
        return filter(b -> contains(b.getBookId(), keyword)
                || contains(b.getTitle(), keyword)
                || contains(b.getAuthor(), keyword));
    }

    private List<Book> filter(java.util.function.Predicate<Book> predicate) {
        List<Book> result = new ArrayList<>();
        for (Book b : books) {
            if (predicate.test(b)) result.add(b);
        }
        result.sort(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private boolean contains(String haystack, String needle) {
        return haystack != null && needle != null
                && haystack.toLowerCase(Locale.ROOT).contains(needle.trim().toLowerCase(Locale.ROOT));
    }

    // ----------------------------------------------------------------- issue / return

    /**
     * Issues a copy of a book to a student.
     * @throws BookNotFoundException if the ID does not exist
     * @throws BookNotAvailableException if no copies are free
     * @throws BorrowLimitExceededException if the student is already at the borrow limit
     */
    public Transaction issueBook(String bookId, Student student)
            throws BookNotFoundException, BookNotAvailableException, BorrowLimitExceededException {
        Book book = getBookById(bookId);
        if (!book.isAvailable()) {
            throw new BookNotAvailableException(book.getTitle());
        }
        student.borrowBook(bookId);   // throws BorrowLimitExceededException and leaves state untouched if it does
        book.issueCopy();
        String txId = IDGenerator.generate("T", transactions, Transaction::getTransactionId);
        Transaction t = Transaction.newIssue(txId, bookId, student.getUserId());
        transactions.add(t);
        saveAll();
        return t;
    }

    /**
     * Returns a previously issued book on behalf of a student.
     * @throws BookNotFoundException if the book ID does not exist
     * @throws TransactionNotFoundException if there is no matching active issue record
     */
    public Transaction returnBook(String bookId, Student student)
            throws BookNotFoundException, TransactionNotFoundException {
        Book book = getBookById(bookId);
        Transaction active = null;
        for (Transaction t : transactions) {
            if (t.getBookId().equalsIgnoreCase(bookId)
                    && t.getUserId().equals(student.getUserId())
                    && t.getStatus() == TransactionStatus.ISSUED) {
                active = t;
                break;
            }
        }
        if (active == null) {
            throw new TransactionNotFoundException(bookId, student.getUserId());
        }
        active.markReturned();
        book.returnCopy();
        student.returnBook(bookId);
        saveAll();
        return active;
    }

    public List<Transaction> getTransactionsByUser(String userId) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getUserId().equals(userId)) result.add(t);
        }
        result.sort(Comparator.comparing(Transaction::getIssueDate).reversed());
        return result;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> copy = new ArrayList<>(transactions);
        copy.sort(Comparator.comparing(Transaction::getIssueDate).reversed());
        return copy;
    }

    public List<Transaction> getOverdueTransactions() {
        List<Transaction> overdue = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.isOverdue()) overdue.add(t);
        }
        return overdue;
    }
}
