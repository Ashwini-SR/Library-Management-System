package com.library.main;

import com.library.exceptions.AuthenticationException;
import com.library.exceptions.DuplicateUserException;
import com.library.exceptions.InvalidInputException;
import com.library.models.Admin;
import com.library.models.Role;
import com.library.models.Student;
import com.library.models.User;
import com.library.services.AuthService;
import com.library.services.LibraryService;
import com.library.utils.InputValidator;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        AuthService authService = new AuthService();
        LibraryService libraryService = new LibraryService();
        libraryService.syncStudentLoans(authService.getAllStudents());

        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            libraryService.saveAll();
            authService.save();
        }));

        printWelcomeBanner();

        boolean running = true;
        while (running) {
            System.out.println("\n==================== LIBRARY MANAGEMENT SYSTEM ====================");
            System.out.println(" 1. Login");
            System.out.println(" 2. Register as a new Student");
            System.out.println(" 0. Exit");
            System.out.println("=====================================================================");

            int choice = InputValidator.readMenuChoice(scanner, "Choose an option: ", 0, 2);
            switch (choice) {
                case 1 -> login(scanner, authService, libraryService);
                case 2 -> register(scanner, authService);
                case 0 -> {
                    libraryService.saveAll();
                    authService.save();
                    System.out.println("\nAll data saved. Thank you for using the Library Management System!");
                    running = false;
                }
            }
        }
        scanner.close();
    }

    private static void login(Scanner scanner, AuthService authService, LibraryService libraryService) {
        System.out.println("\n--- Login ---");
        String username = InputValidator.readNonEmptyLine(scanner, "Username: ");
        String password = InputValidator.readNonEmptyLine(scanner, "Password: ");
        try {
            User user = authService.authenticate(username, password);
            System.out.println("\nLogin successful. Role: " + user.getRole());

         
            ConsoleMenu menu;
            if (user.getRole() == Role.ADMIN) {
                menu = new AdminConsole((Admin) user, libraryService, authService, scanner);
            } else {
                menu = new StudentConsole((Student) user, libraryService, scanner);
            }
            menu.start();
        } catch (AuthenticationException e) {
            System.out.println("\n[LOGIN FAILED] " + e.getMessage());
        }
    }

    private static void register(Scanner scanner, AuthService authService) {
        System.out.println("\n--- Student Registration ---");
        String name = InputValidator.readNonEmptyLine(scanner, "Full name: ");
        String username = InputValidator.readNonEmptyLine(scanner, "Choose a username: ");
        String password = InputValidator.readNonEmptyLine(scanner, "Choose a password: ");
        try {
            Student student = authService.registerStudent(name, username, password);
            System.out.println("\nRegistration successful! Your Student ID is: " + student.getUserId());
            System.out.println("You can now log in using your username and password.");
        } catch (DuplicateUserException | InvalidInputException e) {
            System.out.println("\n[REGISTRATION FAILED] " + e.getMessage());
        }
    }

    private static void printWelcomeBanner() {
        System.out.println("=====================================================================");
        System.out.println("   WELCOME TO THE CORE JAVA LIBRARY MANAGEMENT SYSTEM");
        System.out.println("=====================================================================");
        
    }
}
