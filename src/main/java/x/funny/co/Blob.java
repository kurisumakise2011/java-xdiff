package x.funny.co;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

public class Blob {
    private final File file;
    private final JTextPane content;
    private final ArrayList<Integer> diffPositions = new ArrayList<>();

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

    public boolean add(Integer integer) {
        return diffPositions.add(integer);
    }

    public Integer peek() {
        return diffPositions.size() == 0 ? null : diffPositions.get(0);
    }

    public ArrayList<Integer> getDiffPositions() {
        return diffPositions;
    }
}
