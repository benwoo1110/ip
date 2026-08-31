package com.benthecat.kachow.ui.printer;

import com.benthecat.kachow.ui.fx.DialogBox;

import javafx.scene.layout.VBox;

/**
 * Buffers Kachow's response and displays it in the graphical dialog container.
 */
public class FxPrinter implements Printer {

    private final VBox dialogContainer;
    private final StringBuilder data = new StringBuilder();

    public FxPrinter(VBox dialogContainer) {
        this.dialogContainer = dialogContainer;
    }

    @Override
    public void addData(String message) {
        data.append(message).append("\n");
    }

    @Override
    public void outputData() {
        dialogContainer.getChildren().add(DialogBox.createKachowDialog(data.toString()));
        data.setLength(0);
    }
}
