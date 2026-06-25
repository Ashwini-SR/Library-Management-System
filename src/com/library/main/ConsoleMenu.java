package com.library.main;

import java.util.Scanner;

/**
 * Abstract base for the two role-specific console UIs.
 *
 * <p><b>Abstraction + Polymorphism:</b> {@code Main} only ever holds a
 * reference of type {@code ConsoleMenu} after login and calls
 * {@link #start()} on it — it never needs to know whether it is actually
 * talking to an {@link AdminConsole} or a {@link StudentConsole}; the
 * correct overridden version runs automatically at runtime.</p>
 */
public abstract class ConsoleMenu {

    protected final Scanner scanner;

    protected ConsoleMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Runs this menu's main loop until the user logs out. */
    public abstract void start();

    /** Shared visual helper reused by every concrete menu. */
    protected void printHeader(String title) {
        String bar = "=".repeat(Math.max(60, title.length() + 8));
        System.out.println("\n" + bar);
        System.out.println("   " + title);
        System.out.println(bar);
    }

    protected void printDivider() {
        System.out.println("-".repeat(60));
    }

    protected void pause() {
        System.out.print("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}
