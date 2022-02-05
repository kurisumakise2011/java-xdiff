package x.funny.co;

import javax.swing.*;
import java.io.File;

public class Blob {
    private final File file;
    private final JEditorPane content;

    public Blob(File file, JEditorPane content) {
        this.file = file;
        this.content = content;
    }

    public File getFile() {
        return file;
    }

    public JEditorPane getContent() {
        return content;
    }
}
