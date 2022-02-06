package x.funny.co;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import static x.funny.co.Main.MACOS;

public class DifferenceSwingComponent extends JFrame {
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private static final Dimension buttonSize = dim(screenSize.width / 8f, screenSize.height / 8f);
    String selector = MACOS ? "Control" : "Ctrl";
    String multiple = MACOS ? "Command" : "Alt";
    private static final String WELCOME_HTML = "<html><body style=\"color: #444; font-weight: lighter;font-size: 14px;\">" +
            "<p style=\"margin-bottom: 20px;\">Use menu tab or hotkeys below</p>" +
            "<p style=\"margin-bottom: 20px;\">Useful hotkeys</p>" +
            "<p>To open files to compare please click $_c + O, and then using $_s, select two files.</p>" +
            "<p>To find the difference of contents, click $_c + D</p>" +
            "<p>To enable editing, click $_c + E</p>" +
            "<p>To close files, click $_c + C</p>" +
//            "<p style=\"margin-top: 20px;\">Or just simply choose them one by one below</p>" +
            "</body></html>";

    String welcomeTip = WELCOME_HTML.replace("$_c", selector).replace("$_s", multiple);

    private DifferenceBetweenBlobs current;

    private static final ImageIcon icon = getIcon("/icon/add-icon-png.png");
    Border hoverButtonAnchorBorder = BorderFactory.createLineBorder(Color.decode("#ACCEF7"), 30);
    Border buttonAnchorBorder = BorderFactory.createLineBorder(Color.WHITE, 30);
    Border labelTopPadding = BorderFactory.createEmptyBorder(screenSize.height / 6, 0, 0, 0);
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
                setContentPane(differenceContentPane(model, selectedFiles));
                current = model;
                current.show();
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
        JComponent left = buildDifferencePanel(model, BorderLayout.WEST, selectedFiles[0]);
        JComponent right = buildDifferencePanel(model, BorderLayout.EAST, selectedFiles[1]);
        JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        main.setResizeWeight(0.5f);
        main.setOneTouchExpandable(false);
        return main;
    }

    private JComponent buildDifferencePanel(DifferenceBetweenBlobs model, String constraint, File selectedFile) {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setText("<h3>Content of the file will shown here ... </h3>");
        editorPane.setEditable(false);
        editorPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);


        model.appendFile(new Blob(selectedFile, editorPane), constraint);
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
