package x.funny.co.view;

import x.funny.co.Logger1;
import x.funny.co.controller.Blob;
import x.funny.co.model.ApplicationLogicRuntimeException;
import x.funny.co.model.Difference;
import x.funny.co.model.DifferenceType;
import x.funny.co.model.SplitSolutionDiffFinder;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class DifferenceBetweenBlobs {
    private static final Logger1 log = Logger1.logger(DifferenceBetweenBlobs.class);

    private static final String REMOVAL = "removal";
    private static final String INSERTION = "insertion";
    private static final String EQUALITY = "equality";


    private static final String REMOVAL_LINE = "removing_line";
    private static final String INSERTION_LINE = "inserting_line";

    private static final Color REMOVAL_COLOR = Color.decode("#ffcdd1");
    private static final Color INSERTION_COLOR = Color.decode("#a9f2ab");

    private static final Color REMOVAL_COLOR_LINE = Color.decode("#ffebee");
    private static final Color INSERTION_COLOR_LINE = Color.decode("#d8fed8");

    private static final LineHighLighter removalLinePainter = new LineHighLighter(REMOVAL_COLOR, REMOVAL_COLOR_LINE, true);
    private static final LineHighLighter insertionLinePainter = new LineHighLighter(INSERTION_COLOR, INSERTION_COLOR_LINE, true);

    private static final Painter removal = new Painter(REMOVAL_LINE, REMOVAL, new LineHighLighter(REMOVAL_COLOR, REMOVAL_COLOR_LINE, false));
    private static final Painter insertion = new Painter(INSERTION_LINE, INSERTION, new LineHighLighter(INSERTION_COLOR, INSERTION_COLOR_LINE, false));

    private static final StyledColor[] styles = new StyledColor[]{
            new StyledColor(INSERTION_LINE, INSERTION_COLOR_LINE),
            new StyledColor(INSERTION, INSERTION_COLOR),
            new StyledColor(REMOVAL, REMOVAL_COLOR),
            new StyledColor(REMOVAL_LINE, REMOVAL_COLOR_LINE),
            new StyledColor(EQUALITY, Color.WHITE)
    };

    private final SplitSolutionDiffFinder diffFinder = new SplitSolutionDiffFinder();
    public static final int MAX_DISTANCE = 500;
    private final int distance;

    private Blob left;
    private Blob right;
    private final Deque<Integer> diffPositions = new ConcurrentLinkedDeque<>();
    private final List<LinePosition> lines = new ArrayList<>();
    private final List<LinePosition> chars = new ArrayList<>();

    public DifferenceBetweenBlobs() {
        this(MAX_DISTANCE);
    }

    public DifferenceBetweenBlobs(int maxDistance) {
        this.distance = maxDistance;
    }

    public void add(Integer integer) {
        Integer last = diffPositions.peekLast();
        if (last != null && Math.abs(integer - last) <= distance) {
            return;
        }
        diffPositions.add(integer);
    }

    public void clear() {
        diffPositions.clear();
        lines.clear();
        chars.clear();
    }

    public Deque<Integer> getDiffPositions() {
        return diffPositions;
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

        JTextPane leftContent = this.left.getContent();
        JTextPane rightContent = this.right.getContent();

        StyledDocument leftDoc = new DefaultStyledDocument();
        StyledDocument rightDoc = new DefaultStyledDocument();
        leftContent.setDocument(leftDoc);
        rightContent.setDocument(rightDoc);

        addBackgroundStyles(leftDoc);
        addBackgroundStyles(rightDoc);

        LinkedList<Difference> result = diffFinder.computeDifferenceBetween(left, right);

        for (Difference diff : result) {
            if (diff.getType() == DifferenceType.INSERTION) {
                proceed(rightDoc, leftDoc, rightContent, diff, insertion);
            }
            if (diff.getType() == DifferenceType.REMOVAL) {
                proceed(leftDoc, rightDoc, leftContent, diff, removal);
            }
            if (diff.getType() == DifferenceType.EQUALITY) {
                insertText(leftDoc, diff.getText());
                insertText(rightDoc, diff.getText());
            }
        }

        leftContent.getHighlighter().removeAllHighlights();
        rightContent.getHighlighter().removeAllHighlights();

        highlightLines();
        highlightDifferences();

        getPosition(this.left);
        this.left.getContent().grabFocus();
    }

    private void proceed(StyledDocument targetDoc, StyledDocument sourceDoc, JTextPane content, Difference diff, Painter painter) {
        int len = targetDoc.getLength();
        int pos = checkLine(targetDoc, diff.getText(), painter.lineColor, painter.color);
        this.add((pos + len) / 2);
//        alignDocument(targetDoc, sourceDoc, len, diff.length());
        chars.add(LinePosition.of(content, len, len + diff.length(), painter.painter));
        int n = numberOfNewLines(diff.getText());
        if (n > 0) {
            insertText(sourceDoc, "\n".repeat(n));
        }
    }

    private int numberOfNewLines(String text) {
        return (int) text.chars().filter(c -> ((char)c) == '\n').count();
    }

    private void highlightLines() {
        for (LinePosition line : lines) {
            highlight(line.getPane().getHighlighter(), line.getFrom(), line.getTo(), line.getPainter());
        }
    }

    private void highlightDifferences() {
        for (LinePosition line : chars) {
            highlight(line.getPane().getHighlighter(), line.getFrom(), line.getTo(), line.getPainter());
        }

    }

//    private void alignDocument(StyledDocument source, StyledDocument target, int from, int len) {
//        try {
//            StringBuilder sb = new StringBuilder();
//            int startLine = findStartLine(target);
//            int targetLen = target.getLength() - startLine;
//            String after = getText(target, startLine, targetLen);
//            target.remove(startLine, targetLen);
//
//            String text = source.getText(from, len);
//            text = text.replace(after, "");
//            for (int i = 0; i < text.length(); i++) {
//                if (text.charAt(i) == '\n') {
//                    sb.append('\n');
//                } else {
//                    sb.append('\u0000');
//                }
//            }
//            target.insertString(startLine, sb.toString(), null);
//            if (!after.isEmpty()) {
//                target.insertString(target.getLength(), after, null);
//            }
//        } catch (BadLocationException e) {
//            log.debug("invalid location", e);
//            throw new ApplicationLogicRuntimeException(e.getMessage());
//        }
//    }

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

    public static String getText(Document target, int offset, int length) {
        try {
            return target.getText(offset, length);
        } catch (BadLocationException e) {
            log.debug("invalid location", e);
            throw new ApplicationLogicRuntimeException(e.getMessage());
        }
    }

    private int checkLine(StyledDocument doc, String text, String lineColor, String diffColor) {
        int first = doc.getLength();
        int pos = insertText(doc, text);
        JTextPane pane = getPane(diffColor);
        futureHighlight(first, pos, lineColor, pane);
        return pos;
    }

    private Highlighter.HighlightPainter resolveLinePainter(String color) {
        return INSERTION_LINE.equals(color) ? insertionLinePainter : removalLinePainter;
    }

    private JTextPane getPane(String diffColor) {
        return REMOVAL.equals(diffColor) ? left.getContent() : right.getContent();
    }

    private void futureHighlight(int start, int end, String color, JTextPane pane) {
        lines.add(LinePosition.of(pane, start, end, resolveLinePainter(color)));
    }

    private void highlight(Highlighter highlighter, int start, int end, Highlighter.HighlightPainter painter) {
        try {
            highlighter.addHighlight(start, end, painter);
        } catch (BadLocationException e) {
            log.debug("invalid location", e);
            throw new ApplicationLogicRuntimeException(e);
        }
    }

    private void addBackgroundStyles(StyledDocument document) {
        for (StyledColor sc : styles) {
            Style style = document.addStyle(sc.name, null);
            StyleConstants.setBackground(style, sc.color);
        }
    }

    private int insertText(StyledDocument doc, String text) {
        try {
            doc.insertString(doc.getLength(), text, null);
            return doc.getLength();
        } catch (BadLocationException e) {
            log.debug("invalid location", e);
            throw new ApplicationLogicRuntimeException(e.getMessage());
        }
    }

    private void getPosition(Blob blob) {
        Integer pos = diffPositions.pollFirst();
        if (pos != null) {
            blob.getContent().setCaretPosition(pos);
            diffPositions.addLast(pos);
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

    public String readAll(Blob blob) {
        try {
            return new String(Files.readAllBytes(blob.getFile().toPath()));
        } catch (IOException e) {
            log.debug("IO exception", e);
            throw new ApplicationLogicRuntimeException("could not read a file content", e);
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

    private static class Painter {
        private String lineColor;
        private String color;
        private Highlighter.HighlightPainter painter;

        public Painter(String lineColor, String color, Highlighter.HighlightPainter painter) {
            this.lineColor = lineColor;
            this.color = color;
            this.painter = painter;
        }
    }
}
