package com.benthecat.kachow.ui.fx;

import com.benthecat.kachow.Kachow;
import com.benthecat.kachow.ui.printer.FxPrinter;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * Controller for the main GUI.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private Kachow kachow;

    /**
     * Initializes the chatbot and keeps the latest dialog visible.
     */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
        kachow = new Kachow(new FxPrinter(dialogContainer));
        kachow.sendWelcomeMessage();
    }

    /**
     * Creates two dialog boxes, one echoing user input and the other containing Kachow's reply and then appends them to
     * the dialog container. Clears the user input after processing.
     */
    @FXML
    private void handleUserInput() {
        dialogContainer.getChildren().add(DialogBox.createUserDialog(userInput.getText()));
        kachow.handleUserInput(userInput.getText());
        userInput.clear();
    }
}
