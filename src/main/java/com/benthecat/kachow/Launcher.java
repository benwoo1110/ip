package com.benthecat.kachow;

import javafx.application.Application;

public class Launcher {


    /** Starts Kachow using its default data file. */
    public static void main(String[] args) {
        // new Kachow(DATA_FILE).run();
        Application.launch(FxMain.class, args);
    }
}
