package com.benthecat.kachow.ui.fx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.Objects;

public class DialogBox extends HBox {

    private static final Image userImage = new Image(
            Objects.requireNonNull(DialogBox.class.getResourceAsStream("/images/DaUser.png")));
    private static final Image kachowsImage = new Image(
            Objects.requireNonNull(DialogBox.class.getResourceAsStream("/images/DaKachow.png")));

    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

    private DialogBox(String text, Image img) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.setText(text);
        displayPicture.setImage(img);
    }

    private DialogBox flip() {
        this.setAlignment(Pos.TOP_LEFT);
        ObservableList<Node> tmp = FXCollections.observableArrayList(this.getChildren());
        FXCollections.reverse(tmp);
        this.getChildren().setAll(tmp);
        dialog.getStyleClass().add("reply-label");
        return this;
    }

    public static DialogBox getUserDialog(String s) {
        return new DialogBox(s, userImage);
    }

    public static DialogBox getKachowDialog(String s) {
        return new DialogBox(s, kachowsImage).flip();
    }
}
