package x.funny.co;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.GapContent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DifferenceBetweenBlobs {
    private static final Logger1 log = Logger1.logger(DifferenceBetweenBlobs.class);

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
    private static final Color grey = Color.decode("#f8f9fa");
    private static final int MAX_DISTANCE = 1000;

    private Blob left;
    private Blob right;
    private final LinkedList<Integer> diffPositions = new LinkedList<>();

    public void add(Integer integer) {
        Integer last = diffPositions.peekLast();
        if (last != null && Math.abs(integer - last) <= MAX_DISTANCE) {
            return;
        }
        diffPositions.add(integer);
    }

    public Integer peek() {
        return diffPositions.peek();
    }

    public ArrayList<Integer> getDiffPositions() {
        return new ArrayList<>(diffPositions);
    }

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

    boolean isLeftBigger = false;
    public boolean isLeftBigger() {
        return isLeftBigger;
    }

    public void findDiff() {
        if (!canBeCompared()) {
            return;
        }
        String left = readAll(this.left);
        String right = readAll(this.right);

        int capacity = Math.max(left.length(), right.length());
        isLeftBigger = capacity == left.length();

        StyledDocument leftDoc = new DefaultStyledDocument(new GapContent(capacity), new StyleContext());
        StyledDocument rightDoc = new DefaultStyledDocument(new GapContent(capacity), new StyleContext());
        this.left.getContent().setDocument(leftDoc);
        this.right.getContent().setDocument(rightDoc);

        addBackgroundStyles(leftDoc);
        addBackgroundStyles(rightDoc);

        LinkedList<DifferenceUtils.Difference> result = DifferenceUtils.computeDifferenceBetween(left, right);

        for (DifferenceUtils.Difference diff : result) {
            if (diff.type == DifferenceUtils.DifferenceType.INSERTION) {
                int len = rightDoc.getLength();
                int pos = checkLine(rightDoc, diff.text, INSERTION_LINE, INSERTION);
                alignDocument(rightDoc, leftDoc, len, diff.text.length());
                this.add(pos);
            }
            if (diff.type == DifferenceUtils.DifferenceType.REMOVAL) {
                int len = leftDoc.getLength();
                int pos = checkLine(leftDoc, diff.text, REMOVAL_LINE, REMOVAL);
                alignDocument(leftDoc, rightDoc, len, diff.text.length());
                this.add(pos);
            }
            if (diff.type == DifferenceUtils.DifferenceType.EQUALITY) {
                insertText(leftDoc, diff.text, leftDoc.getStyle(EQUALITY));
                insertText(rightDoc, diff.text, rightDoc.getStyle(EQUALITY));
            }
        }

        if (isLeftBigger) {
            fill(rightDoc, leftDoc,capacity - rightDoc.getLength());
        } else {
            fill(leftDoc, rightDoc,capacity - leftDoc.getLength());
        }
        getPosition(this.left);
        getPosition(this.right);
    }

    private void fill(StyledDocument target, StyledDocument source, int count) {
        try {
            StringBuilder sb = new StringBuilder();
            String text = source.getText(target.getLength(), count);
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == '\n') {
                    sb.append('\n');
                } else {
                    sb.append('\u0000');
                }
            }
            target.insertString(target.getLength(), sb.toString(), null);
        } catch (BadLocationException e) {
            throw new ApplicationLogicRuntimeException(e.getMessage());
        }
    }

    private static class Position {
        private int start;
        private int end;
        private AttributeSet attributes;
    }

    private void alignDocument(StyledDocument source, StyledDocument target, int from, int len) {
        try {
            int startLine = findStartLine(target);
            int targetLen = target.getLength() - startLine - 1;
            String after = getText(target, startLine + 1, targetLen);
            Element paragraph = target.getParagraphElement(from);
            List<Position> positions = new ArrayList<>();
            if (paragraph instanceof AbstractDocument.BranchElement) {
                var element = (AbstractDocument.BranchElement) paragraph;
                var iterator = element.children().asIterator();
                while (iterator.hasNext()) {
                    var node = iterator.next();
                    if (node instanceof AbstractDocument.LeafElement) {
                        var child = (AbstractDocument.LeafElement) node;
                        Position position = new Position();
                        position.start = child.getStartOffset();
                        position.end = child.getEndOffset();
                        position.attributes = child.getAttributes();
                        positions.add(position);
                    }
                }
            }
            target.remove(startLine + 1, targetLen);

            String text = source.getText(from, len);
            text = text.replace(after, "");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == '\n') {
                    sb.append('\n');
                } else {
                    sb.append('\u0000');
                }
            }
            SimpleAttributeSet attributeSet = new SimpleAttributeSet();
            StyleConstants.setBackground(attributeSet, grey);
            target.insertString(startLine, sb.toString(), attributeSet);
            if (!after.isEmpty()) {
                target.insertString(target.getLength(), after, paragraph.getAttributes());
                for (Position position : positions) {
                    target.setCharacterAttributes(position.start + 1, position.end - position.start + 1, position.attributes, false);
                }
            }
        } catch (BadLocationException e) {
            log.debug("invalid location", e);
            throw new ApplicationLogicRuntimeException(e.getMessage());
        }
    }

    private int findStartLine(StyledDocument target) {
        int len = target.getLength() - 1;
        for (int offset = len; offset >= 0; offset--) {
            String str = getChar(target, offset);
            if ("\n".equals(str)) {
                return offset;
            }
        }
        return 0;
    }

    private String getChar(StyledDocument target, int offset) {
        try {
            return target.getText(offset, 1);
        } catch (BadLocationException e) {
            log.debug("invalid location", e);
            throw new ApplicationLogicRuntimeException(e.getMessage());
        }
    }

    private String getText(StyledDocument target, int offset, int length) {
        try {
            return target.getText(offset, length);
        } catch (BadLocationException e) {
            throw new ApplicationLogicRuntimeException(e.getMessage());
        }
    }

    private int checkLine(StyledDocument doc, String text, String lineColor, String diffColor) {
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
        } catch (BadLocationException e) {
            log.debug("invalid location", e);
            throw new ApplicationLogicRuntimeException(e.getMessage());
        }
    }

    private void getPosition(Blob blob) {
        if (this.peek() != null) {
            blob.getContent().setCaretPosition(this.peek());
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
            log.debug("IO exception", e);
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
