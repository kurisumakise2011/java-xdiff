package x.funny.co;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static x.funny.co.Main.MACOS;

public class DifferenceSwingComponent extends JFrame {
    private static final int MB_1 = 1024 * 1024 * 1024;
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private static final Dimension buttonSize = dim(screenSize.width / 8f, screenSize.height / 8f);
    String selector = MACOS ? "Control" : "Ctrl";
    String multiple = MACOS ? "Command" : "Alt";
    private static final String WELCOME_HTML = "<html><body style=\"color: #000; font-weight: lighter;font-size: 14px;\">" +
            "<p style=\"margin-bottom: 40px;font-size: 28px;\">Welcome</p>" +
            "<p style=\"margin-bottom: 20px;font-size: 14px;\">Use menu tab or hotkeys below</p>" +
            "<p style=\"margin-bottom: 20px;font-size: 14px;\">Useful hotkeys</p>" +
            "<p style=\"margin-bottom: 15px;font-size: 12px;\">To open files to compare please click $_c + O, and then using $_s, select two files.</p>" +
            "<p style=\"margin-bottom: 15px;font-size: 12px;\">To find the difference of contents, click $_c + D</p>" +
            "<p style=\"margin-bottom: 15px;font-size: 12px;\">To enable editing, click $_c + E</p>" +
            "<p style=\"margin-bottom: 15px;font-size: 12px;\">To go next difference, click N</p>" +
            "<p style=\"margin-bottom: 15px;font-size: 12px;\">To go previous difference, click P</p>" +
            "<p style=\"margin-bottom: 15px;font-size: 12px;\">To close files, click $_c + C</p>" +
//            "<p style=\"margin-top: 20px;\">Or just simply choose them one by one below</p>" +
            "</body></html>";

    String welcomeTip = WELCOME_HTML.replace("$_c", selector).replace("$_s", multiple);

    private DifferenceBetweenBlobs current;

    private static final ImageIcon icon = getIcon("/icon/add-icon-png.png");
    private final Border labelTopPadding = BorderFactory.createEmptyBorder(screenSize.height / 6, 0, 0, 0);
    private JMenuItem openFiles;
    private JMenuItem closeFiles;
    private JMenuItem diffFiles;
    private JMenuItem enableEditing;
    private JMenu help;
    private JMenu about;
    private Container startContentPane;


    public DifferenceSwingComponent() throws HeadlessException {
        initComponents();
    }

    public void initComponents() {
        startContentPane = startContentPane();
        setPreferredSize(screenSize);
        setLocationRelativeTo(null);
        setContentPane(startContentPane);
        setJMenuBar(createMenuBar());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu file = new JMenu("File");
        file.getAccessibleContext().setAccessibleDescription("The file menu");
        file.setMnemonic(KeyEvent.VK_F);

        openFiles = new JMenuItem("Open Files");
        openFiles.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        file.add(openFiles);

        closeFiles = new JMenuItem("Close Files");
        closeFiles.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        file.add(closeFiles);

        JMenu edit = new JMenu("Edit");
        edit.getAccessibleContext().setAccessibleDescription("The edit menu");
        edit.setMnemonic(KeyEvent.VK_E);

        diffFiles = new JMenuItem("Diff Files");
        diffFiles.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
        edit.add(diffFiles);

        enableEditing = new JCheckBoxMenuItem("Enable Editing");
        enableEditing.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        edit.add(enableEditing);

        help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_F11);
        help.getAccessibleContext().setAccessibleDescription("The help dialog");

        about = new JMenu("About");
        about.setMnemonic(KeyEvent.VK_F12);
        about.getAccessibleContext().setAccessibleDescription("The about dialog");

        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(help);
        menuBar.add(about);

        // Actions
        openFiles.addActionListener(e -> {
            DifferenceBetweenBlobs model = new DifferenceBetweenBlobs();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            int code = fileChooser.showOpenDialog(this);
            File[] selectedFiles = fileChooser.getSelectedFiles();

            if (code == JFileChooser.APPROVE_OPTION && selectedFiles.length == 2) {
                if (selectedFiles[0].length() > MB_1) {
                    throw new SwingUserInterfaceException("file cannot be bigger than 1 MB");
                }
                if (selectedFiles[1].length() > MB_1) {
                    throw new SwingUserInterfaceException("file cannot be bigger than 1 MB");
                }
                setContentPane(differenceContentPane(model, selectedFiles));
                current = model;
                current.findDiff();
                validate();
                repaint();

            }
        });

        closeFiles.addActionListener(e -> setContentPane(startContentPane));

        enableEditing.addItemListener(e -> {
            if (current != null) {
                int stateChange = e.getStateChange();
                JEditorPane left = current.getLeft().getContent();
                JEditorPane right = current.getRight().getContent();
                left.setEditable(stateChange == ItemEvent.SELECTED);
                right.setEditable(stateChange == ItemEvent.SELECTED);
            }
        });

        diffFiles.addActionListener(e -> {
            if (current != null && current.canBeCompared()) {
                current.findDiff();
            }
        });

        return menuBar;
    }

    private Container startContentPane() {
        JPanel openPanel = new JPanel();
        openPanel.setLayout(new BoxLayout(openPanel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(welcomeTip, SwingConstants.CENTER);
        label.setBorder(labelTopPadding);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton button = new JButton("Click here to open two files to diff ... ");
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        openPanel.add(label);
//        openPanel.add(button);
        return openPanel;
    }

    private Container differenceContentPane(DifferenceBetweenBlobs model, File[] selectedFiles) {
        JScrollPane left = buildDifferencePanel(model, BorderLayout.WEST, selectedFiles[0]);
        JScrollPane right = buildDifferencePanel(model, BorderLayout.EAST, selectedFiles[1]);
        synchronizedScroll(left, right);
        JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        main.setResizeWeight(0.5f);
        main.setEnabled(false);
        main.setOneTouchExpandable(false);
        return main;
    }

    private void synchronizedScroll(JScrollPane left, JScrollPane right) {
        BoundedRangeModel yMdel = left.getVerticalScrollBar().getModel();
        right.getVerticalScrollBar().setModel(yMdel);

        BoundedRangeModel xModel = left.getHorizontalScrollBar().getModel();
        right.getHorizontalScrollBar().setModel(xModel);
    }

    private JScrollPane buildDifferencePanel(DifferenceBetweenBlobs model, String constraint, File selectedFile) {
        StyledDocument doc = new DefaultStyledDocument();
        doc.addDocumentListener(new DefaultDocumentListener());

        JTextPane textPane = new JTextPane();
        textPane.setText("Content of the file will shown here ... ");
        textPane.setEditable(false);
        textPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textPane.setDocument(doc);

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        Blob blob = new Blob(selectedFile, textPane);

        model.appendFile(blob, constraint);

        textPane.addKeyListener(new KeyAdapter() {
            int position = 0;
            List<Integer> iterator = blob.getDiffPositions();

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_N) {
                    if (position < iterator.size() - 1) {
                        position++;
                    }
                    textPane.setCaretPosition(iterator.get(position));
                }
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    if (position > 0) {
                        position--;
                    }
                    textPane.setCaretPosition(iterator.get(position));
                }
            }
        });

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
