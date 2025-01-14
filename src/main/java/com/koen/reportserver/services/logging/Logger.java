package com.koen.reportserver.services.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    // ANSI escape codes for colors
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";

    // Log levels
    public enum Level {
        INFO, DEBUG, WARN, ERROR
    }

    // Logger function
    public static void log(Level level, String message) {
        String color;
        color = switch (level) {
            case INFO -> GREEN;
            case DEBUG -> BLUE;
            case WARN -> YELLOW;
            case ERROR -> RED;
            default -> RESET;
        };

        // Get current time
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Print log message with only the level colored
        System.out.println("[" + timestamp + "] [" + color + level + RESET + "] " + message);
    }

    public static void started(int address) {
        log(Level.INFO, "  _____                       _      _____                          ");
        log(Level.INFO, " |  __ \\                     | |    / ____|                         ");
        log(Level.INFO, " | |__) |___ _ __   ___  _ __| |_  | (___   ___ _ ____   _____ _ __ ");
        log(Level.INFO, " |  _  // _ \\ '_ \\ / _ \\| '__| __|  \\___ \\ / _ \\ '__\\ \\ / / _ \\ '__|");
        log(Level.INFO, " | | \\ \\  __/ |_) | (_) | |  | |_   ____) |  __/ |   \\ V /  __/ |   ");
        log(Level.INFO, " |_|  \\_\\___| .__/ \\___/|_|   \\__| |_____/ \\___|_|    \\_/ \\___|_|   ");
        log(Level.INFO, "            | |                                                     ");
        log(Level.INFO, "            |_|                                                     ");
        log(Level.INFO, "Server listening on port: " + address);
    }
}