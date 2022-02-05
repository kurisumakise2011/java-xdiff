package x.funny.co;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class DifferenceSwingComponent extends JFrame {
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private static final Dimension buttonSize = dim(screenSize.width / 8f, screenSize.height / 8f);

    private JComponent left;
    private JComponent right;
    private JSplitPane main;
    private DifferenceBetweenBlobs current;

    private static final ImageIcon icon = getIcon("/icon/add-icon-png.png");
    Border hoverButtonAnchorBorder = BorderFactory.createLineBorder(Color.decode("#ACCEF7"), 30);
    Border buttonAnchorBorder = BorderFactory.createLineBorder(Color.WHITE, 30);

    public DifferenceSwingComponent() throws HeadlessException {
        initComponents();
    }

    public void initComponents() {
        DifferenceBetweenBlobs model = new DifferenceBetweenBlobs();
        setPreferredSize(screenSize);
        setLocationRelativeTo(null);
        setContentPane(contentPane(model));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        current = model;
    }

    private Container contentPane(DifferenceBetweenBlobs model) {
        left = buildDifferencePanel(model, BorderLayout.WEST);
        right = buildDifferencePanel(model, BorderLayout.EAST);
        main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        main.setResizeWeight(0.5f);
        main.setOneTouchExpandable(false);
        model.findDiff();
        return main;
    }

    private JComponent buildDifferencePanel(DifferenceBetweenBlobs model, String constraint) {
        JPanel openPanel = new JPanel();
        JPanel diffPanel = new JPanel();

        JScrollPane scrollPane = new JScrollPane(openPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);


        JButton open = new JButton();
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setText("<h3>Content of the file will shown here ... </h3>");
        JPanel buttonAnchor = new JPanel();

        JButton close = new JButton("Click to close file");
        close.setBorder(BorderFactory.createEmptyBorder());
        close.addActionListener(e -> {
            scrollPane.setViewportView(openPanel);
        });

        open.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showOpenDialog(this);
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null && selectedFile.exists()) {
                model.appendFile(new Blob(selectedFile, editorPane), constraint);
                scrollPane.setViewportView(diffPanel);
                if (model.canBeCompared()) {
                    model.findDiff();
                    model.show();
                }
            }
        });

        buttonAnchor.setBorder(BorderFactory.createEmptyBorder());

        open.setBorder(buttonAnchorBorder);
        open.setText("Click to choose file ...");
//        open.setIcon(icon);
        open.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                open.setBorder(hoverButtonAnchorBorder);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                open.setBorder(buttonAnchorBorder);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                open.setBorder(buttonAnchorBorder);
            }
        });


        buttonAnchor.add(open, CENTER_ALIGNMENT);
        openPanel.add(buttonAnchor);
        diffPanel.add(close);
        diffPanel.add(editorPane);

        return scrollPane;
    }

    public static Dimension dim(double w, double h) {
        Dimension dim = new Dimension();
        dim.setSize(w, h);
        return dim;
    }

    public static ImageIcon getIcon(String iconPath) {
        try {
            Image image = ImageIO.read(DifferenceSwingComponent.class.getResource(iconPath))
                    .getScaledInstance(128, 128, Image.SCALE_SMOOTH);
            return new ImageIcon(image);
        } catch (IOException e) {
            throw new SwingUserInterfaceException("could not load icon");
        }
    }
}
