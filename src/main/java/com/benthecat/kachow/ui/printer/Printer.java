package com.benthecat.kachow.ui.printer;

/**
 * Defines how Kachow accumulates and displays response text.
 */
public interface Printer {
    /** Adds one message to the current response. */
    void addData(String message);

    /** Displays the current response and prepares for the next response. */
    void outputData();
}
