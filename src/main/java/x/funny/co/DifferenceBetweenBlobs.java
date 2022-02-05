package x.funny.co;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class DifferenceBetweenBlobs {
    private Blob left;
    private Blob right;

    public DifferenceBetweenBlobs() {
    }

    public void appendFile(Blob target, String constraint) {
        if (BorderLayout.WEST.equals(constraint)) {
            left = target;
        }
        if (BorderLayout.EAST.equals(constraint)) {
            right = target;
        }
    }

    public void findDiff() {
        if (!canBeCompared()) {
            return;
        }
    }

    public boolean canBeCompared() {
        return left != null && right != null && left.getFile().exists() && right.getFile().exists();
    }

    public Blob getLeft() {
        return left;
    }

    public Blob getRight() {
        return right;
    }

    public void show() {
        if (canBeCompared()) {
            readAndFill(left);
            readAndFill(right);
        }
    }

    private void readAndFill(Blob blob) {
        try {
            blob.getContent().setText(new String(Files.readAllBytes(blob.getFile().toPath())));
        } catch (IOException e) {
            throw new SwingUserInterfaceException("could not read file content", e);
        }
    }
}
