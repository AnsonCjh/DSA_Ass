package dsa_ass.util;

/**
 * Console utility helpers for TARUMT Resort System
 */
public class ConsoleUtils {

    /**
     * Simulates clearing the screen by printing blank lines.
     * This is the only method that works inside the NetBeans Output panel,
     * since it does not support 'cls' or ANSI escape sequences.
     */
    public static void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
        System.out.flush();
    }
}
