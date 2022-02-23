package x.funny.co.view;

import javax.swing.*;
import javax.swing.text.Highlighter;

public class LinePosition {
    private final int from;
    private final int to;
    private final Highlighter.HighlightPainter painter;
    private final JTextPane pane;

    private LinePosition(JTextPane pane, int to, Highlighter.HighlightPainter painter, int from) {
        this.pane = pane;
        this.from = from;
        this.to = to;
        this.painter = painter;
    }

    public static LinePosition of(JTextPane pane, int from, int to, Highlighter.HighlightPainter painter) {
        return new LinePosition(pane, to, painter, from);
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public JTextPane getPane() {
        return pane;
    }

    public Highlighter.HighlightPainter getPainter() {
        return painter;
    }
}
