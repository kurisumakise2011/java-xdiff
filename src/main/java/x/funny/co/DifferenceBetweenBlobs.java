package x.funny.co;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;

public class DifferenceBetweenBlobs {
    private static final String REMOVAL = "removal";
    private static final String REMOVAL_LINE = "removing_line";
    private static final String INSERTION_LINE = "inserting_line";
    private static final String INSERTION = "insertion";
    private static final String EQUALITY = "equality";
    private static final StyledColor[] styles = new StyledColor[]{
            new StyledColor(INSERTION_LINE, Color.decode("#d8fed8")),
            new StyledColor(INSERTION, Color.decode("#a9f2ab")),
            new StyledColor(REMOVAL, Color.decode("#ffcdd1")),
            new StyledColor(REMOVAL_LINE, Color.decode("#ffebee")),
            new StyledColor(EQUALITY, Color.WHITE)
    };

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
        String left = readAll(this.left);
        String right = readAll(this.right);

        StyledDocument leftDoc = (StyledDocument) this.left.getContent().getDocument();
        StyledDocument rightDoc = (StyledDocument) this.right.getContent().getDocument();

        addBackgroundStyles(leftDoc);
        addBackgroundStyles(rightDoc);

        LinkedList<DifferenceUtils.Difference> result = DifferenceUtils.computeDifferenceBetween(left, right);

        for (DifferenceUtils.Difference diff : result) {
            if (diff.type == DifferenceUtils.DifferenceType.INSERTION) {
                int pos = clarifyLine(rightDoc, diff.text, INSERTION_LINE, INSERTION);
                this.right.add(pos);
            }
            if (diff.type == DifferenceUtils.DifferenceType.REMOVAL) {
                int pos = clarifyLine(leftDoc, diff.text, REMOVAL_LINE, REMOVAL);
                this.left.add(pos);
            }
            if (diff.type == DifferenceUtils.DifferenceType.EQUALITY) {
                insertText(leftDoc, diff.text, leftDoc.getStyle(EQUALITY));
                insertText(rightDoc, diff.text, rightDoc.getStyle(EQUALITY));
            }
        }
        getPosition(this.left);
        getPosition(this.right);
    }

    private int clarifyLine(StyledDocument doc, String text, String lineColor, String diffColor) {
        int first = doc.getLength();
        int pos = insertText(doc, text, doc.getStyle(diffColor));
        Element p = doc.getParagraphElement(first);
        highlightLine(p, first, pos, doc.getStyle(lineColor));
        return pos;
    }

    private void highlightLine(Element paragraph, int exceptFrom, int exceptTo, Style color) {
        int start = paragraph.getStartOffset();
        int end = paragraph.getEndOffset();
        StyledDocument doc = (StyledDocument) paragraph.getDocument();

        doc.setCharacterAttributes(start, exceptFrom - start, color, false);
        doc.setCharacterAttributes(exceptTo, end - exceptTo, color, false);
    }

    private void addBackgroundStyles(StyledDocument document) {
        for (StyledColor sc : styles) {
            Style style = document.addStyle(sc.name, null);
            StyleConstants.setBackground(style, sc.color);
        }
    }

    private int insertText(StyledDocument doc, String text, Style attr) {
        try {
            doc.insertString(doc.getLength(), text, attr);
            return doc.getLength();
        } catch (BadLocationException ignored) {
            return -1;
        }
    }

    private void getPosition(Blob blob) {
        if (blob.peek() != null) {
            blob.getContent().setCaretPosition(blob.peek());
        } else {
            blob.getContent().setCaretPosition(0);
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

    private String readAll(Blob blob) {
        try {
            return new String(Files.readAllBytes(blob.getFile().toPath()));
        } catch (IOException e) {
            throw new SwingUserInterfaceException("could not read a file content", e);
        }
    }

    private static class StyledColor {
        private final String name;
        private final Color color;

        public StyledColor(String name, Color color) {
            this.name = name;
            this.color = color;
        }
    }
}
