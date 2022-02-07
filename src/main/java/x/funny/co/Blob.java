package x.funny.co;

import javax.swing.*;
import java.io.File;
import java.util.LinkedList;
import java.util.ListIterator;

public class Blob {
    private final File file;
    private final JTextPane content;
    private final LinkedList<Integer> diffPositions = new LinkedList<>();

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
        return diffPositions.peek();
    }

    public ListIterator<Integer> listIterator(int index) {
        return diffPositions.listIterator(index);
    }
}
