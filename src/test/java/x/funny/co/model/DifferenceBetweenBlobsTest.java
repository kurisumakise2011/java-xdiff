package x.funny.co.model;

import org.junit.jupiter.api.Test;
import x.funny.co.controller.Blob;
import x.funny.co.view.DifferenceBetweenBlobs;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DifferenceBetweenBlobsTest {


    @Test
    public void shouldFindDifference() throws IOException {
        DifferenceBetweenBlobs betweenBlobs = new DifferenceBetweenBlobs(0);
        Path tempFile1 = Files.createTempFile("data1", "tmp");
        Files.write(tempFile1, "hello world!".getBytes(StandardCharsets.UTF_8));

        Path tempFile2 = Files.createTempFile("data2", "tmp");
        Files.write(tempFile2, "Hello World".getBytes(StandardCharsets.UTF_8));

        Blob data1 = new Blob(tempFile1.toFile(), new JTextPane());
        Blob data2 = new Blob(tempFile2.toFile(), new JTextPane());
        betweenBlobs.appendFile(data1, BorderLayout.WEST);
        betweenBlobs.appendFile(data2, BorderLayout.EAST);

        betweenBlobs.findDiff();

        // h small removed, H big added - 2 difference
        // w small removed, W big added - 2 difference
        // exclamation mark removed - 1 difference
        assertEquals(2, betweenBlobs.getDiffPositions().size());
    }

    @Test
    public void shouldNotWorkWithOnlyOneFile() throws IOException {
        DifferenceBetweenBlobs betweenBlobs = new DifferenceBetweenBlobs(0);
        Path tempFile1 = Files.createTempFile("data1", "tmp");
        Files.write(tempFile1, "hello world!".getBytes(StandardCharsets.UTF_8));

        Blob data1 = new Blob(tempFile1.toFile(), new JTextPane());
        betweenBlobs.appendFile(data1, BorderLayout.WEST);

        betweenBlobs.findDiff();
        assertFalse(betweenBlobs.canBeCompared());
    }

    @Test
    public void shouldFindZeroDifferenceForEqualFiles() throws IOException {
        DifferenceBetweenBlobs betweenBlobs = new DifferenceBetweenBlobs(0);
        Path tempFile1 = Files.createTempFile("data1", "tmp");
        Files.write(tempFile1, "hello world!".getBytes(StandardCharsets.UTF_8));

        Blob data1 = new Blob(tempFile1.toFile(), new JTextPane());
        Blob data2 = new Blob(tempFile1.toFile(), new JTextPane());
        betweenBlobs.appendFile(data1, BorderLayout.WEST);
        betweenBlobs.appendFile(data2, BorderLayout.EAST);

        betweenBlobs.findDiff();
        assertEquals(0, betweenBlobs.getDiffPositions().size());
    }
}
