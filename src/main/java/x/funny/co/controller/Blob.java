package x.funny.co.controller;

import javax.swing.*;
import java.io.File;

public class Blob {
    private final File file;
    private final JTextPane content;

    public Blob(File file, JTextPane content) {
        this.file = file;
        this.content = content;
    }

    public File getFile() {
        return file;
    }

    public JTextPane getContent() {
        return content;
    }
}
