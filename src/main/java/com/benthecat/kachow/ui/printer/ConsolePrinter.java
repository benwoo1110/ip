package com.benthecat.kachow.ui.printer;

public class ConsolePrinter implements Printer {
    private static final Integer UI_INDENT = 4;

    private final StringBuilder data = new StringBuilder();

    @Override
    public void addData(String message) {
        data.append(message.indent(UI_INDENT)).append("\n");
    }

    @Override
    public void outputData() {
        System.out.print(data);
        data.setLength(0); // Clear the data after outputting
    }
}
