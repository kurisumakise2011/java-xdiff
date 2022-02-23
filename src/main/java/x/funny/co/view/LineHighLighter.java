package x.funny.co.view;

import x.funny.co.Logger1;

import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import java.awt.*;

public class LineHighLighter extends DefaultHighlighter.DefaultHighlightPainter {
    private static final Logger1 log = Logger1.logger(LayeredHighlighter.class);

    private final Color color;
    private final Color lineColor;
    private final boolean line;


    /**
     * Constructs a new highlight painter. If <code>c</code> is null,
     * the JTextComponent will be queried for its selection color.
     *
     * @param lineColor the color for the highlight line
     * @param color the color for the highlight word
     */
    public LineHighLighter(Color color, Color lineColor, boolean line) {
        super(line ? lineColor : color);
        this.color = color;
        this.lineColor = lineColor;
        this.line = line;
    }

    @SuppressWarnings("deprecation")
    public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
        Rectangle alloc = bounds.getBounds();
        try {
            TextUI mapper = c.getUI();
            Rectangle p0 = mapper.modelToView(c, offs0);
            Rectangle p1 = mapper.modelToView(c, offs1);
            if (p0.y == p1.y) {
                // same line, render a rectangle
                Rectangle r = p0.union(p1);
                if (line) {
                    g.setColor(lineColor);
                    g.fillRect(0, r.y, c.getWidth(), r.height);
                } else {
                    g.setColor(color);
                    g.fillRect(r.x, r.y, r.width, r.height);
                }
            } else {
                // different lines
                int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
                if (line) {
                    g.setColor(lineColor);
                    g.fillRect(0, p0.y, c.getWidth(), p0.height);
                } else {

                    g.setColor(color);
                    g.fillRect(p0.x, p0.y, p0ToMarginWidth, p0.height);
                }


                if ((p0.y + p0.height) != p1.y) {
                    if (line) {
                        g.setColor(lineColor);
                        g.fillRect(0, p0.y + p0.height, c.getWidth(),
                                p1.y - (p0.y + p0.height));
                    } else {
                        g.setColor(color);
                        g.fillRect(alloc.x, p0.y + p0.height, alloc.width,
                                p1.y - (p0.y + p0.height));
                    }
                }

                if (line) {
                    g.setColor(lineColor);
                    g.fillRect(0, p1.y, c.getWidth(), p1.height);
                } else {
                    g.setColor(color);
                    g.fillRect(alloc.x, p1.y, (p1.x - alloc.x), p1.height);
                }
            }
        } catch (BadLocationException e) {
            log.error("bad location", e);
        }
    }
}
