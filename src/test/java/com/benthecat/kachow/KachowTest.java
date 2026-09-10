package com.benthecat.kachow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.benthecat.kachow.ui.printer.Printer;

/** Tests command recovery and agreement between in-memory tasks and saved data. */
class KachowTest {
    @TempDir
    Path tempDirectory;

    @Test
    void handleUserInput_saveDenied_rollsBackEveryMutationAndAllowsRetry() throws IOException {
        assumeTrue(Files.getFileStore(tempDirectory).supportsFileAttributeView("posix"));
        Path dataFile = tempDirectory.resolve("kachow.txt");
        String original = "T | 0 | first\nT | 1 | second\n";
        Files.writeString(dataFile, original);
        RecordingPrinter printer = new RecordingPrinter();
        Kachow kachow = new Kachow(dataFile.toString(), printer);
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(dataFile);
        try {
            Files.setPosixFilePermissions(dataFile, PosixFilePermissions.fromString("r--r--r--"));
            assumeTrue(!Files.isWritable(dataFile), "Requires an account subject to file permissions.");
            for (String command : List.of("todo new", "mark 1", "unmark 2",
                    "edit 1 /description changed", "delete 1")) {
                printer.lines.clear();
                assertTrue(kachow.handleUserInput(command));
                assertTrue(printer.lines.getFirst().contains("No changes were applied."), command);
                assertEquals(original, Files.readString(dataFile));
                assertOriginalTasks(kachow, printer);
            }
        } finally {
            Files.setPosixFilePermissions(dataFile, permissions);
        }
        kachow.handleUserInput("mark 1");
        assertEquals("T | 1 | first\nT | 1 | second\n", Files.readString(dataFile));
    }

    @Test
    void handleUserInput_loadDenied_doesNotOverwriteUnreadData() throws IOException {
        assumeTrue(Files.getFileStore(tempDirectory).supportsFileAttributeView("posix"));
        Path dataFile = tempDirectory.resolve("kachow.txt");
        String original = "T | 0 | original\n";
        Files.writeString(dataFile, original);
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(dataFile);
        try {
            Files.setPosixFilePermissions(dataFile, PosixFilePermissions.fromString("---------"));
            assumeTrue(!Files.isReadable(dataFile), "Requires an account subject to file permissions.");
            RecordingPrinter printer = new RecordingPrinter();
            Kachow kachow = new Kachow(dataFile.toString(), printer);
            Files.setPosixFilePermissions(dataFile, permissions);
            kachow.handleUserInput("todo replacement");
            assertTrue(printer.lines.getFirst().contains("Saving is disabled"));
            assertEquals(original, Files.readString(dataFile));
        } finally {
            Files.setPosixFilePermissions(dataFile, permissions);
        }
    }

    @Test
    void handleUserInput_duplicateAddOrEdit_preservesDiskAndTaskStatus() throws IOException {
        Path dataFile = tempDirectory.resolve("kachow.txt");
        String original = "T | 0 | first\nT | 1 | second\n";
        Files.writeString(dataFile, original);
        RecordingPrinter printer = new RecordingPrinter();
        Kachow kachow = new Kachow(dataFile.toString(), printer);
        for (String command : List.of("todo FIRST", "edit 2 /description first")) {
            printer.lines.clear();
            kachow.handleUserInput(command);
            assertTrue(printer.lines.getFirst().contains("already on the grid as task 1"));
            assertEquals(original, Files.readString(dataFile));
            assertOriginalTasks(kachow, printer);
        }
    }

    /** Checks the complete list reply after a rejected mutation. */
    private void assertOriginalTasks(Kachow kachow, RecordingPrinter printer) {
        printer.lines.clear();
        kachow.handleUserInput("list");
        assertEquals(List.of(
                "Crew chief's clipboard! Here are all your tasks, from first lap to finish line:",
                "1.[T][ ] first", "2.[T][X] second"), printer.lines);
    }

    /** Captures shared application replies without a console or JavaFX dependency. */
    private static class RecordingPrinter implements Printer {
        private final List<String> lines = new ArrayList<>();

        @Override
        public void addData(String message) {
            lines.add(message);
        }

        @Override
        public void outputData() {
            // Replies remain available until the test clears them.
        }
    }
}
