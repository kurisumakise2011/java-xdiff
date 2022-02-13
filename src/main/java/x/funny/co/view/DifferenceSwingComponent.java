package x.funny.co.view;

import x.funny.co.Logger1;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static x.funny.co.Main.MACOS;

public class DifferenceSwingComponent extends JFrame {
    private static final Logger1 log = Logger1.logger(DifferenceSwingComponent.class);
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private static final String selector = MACOS ? "Control" : "Ctrl";
    private static final String multiple = MACOS ? "Command" : "Alt";
    private static final String WELCOME_HTML = "<html><body style=\"color: #000; font-weight: lighter;font-size: 14px;\">" +
            "<p style=\"margin-bottom: 40px;font-size: 28px;\">Welcome</p>" +
            "<p style=\"margin-bottom: 20px;font-size: 14px;\">Use menu tab or hotkeys below</p>" +
            "<p style=\"margin-bottom: 20px;font-size: 14px;\">Useful hotkeys</p>" +
            "<p style=\"margin-bottom: 15px;font-size: 12px;\">To open files to compare please click $_c + O, and then using $_s, select two files.</p>" +
//            "<p style=\"margin-bottom: 15px;font-size: 12px;\">To enable editing, click $_c + E</p>" +
            "<p style=\"margin-bottom: 15px;font-size: 12px;\">To go next difference, click $_s + N</p>" +
            "<p style=\"margin-bottom: 15px;font-size: 12px;\">To go previous difference, click $_s + P</p>" +
            "<p style=\"margin-bottom: 15px;font-size: 12px;\">To close files, click $_c + C</p>" +
            "</body></html>";

    private static final String welcomeTipMessage = WELCOME_HTML.replace("$_c", selector).replace("$_s", multiple);
    private static final String helpTipMessage = ("Useful hotkeys\n" +
            "To open files to compare please click $_c + O, and then using $_s, select two files.\n" +
//            "To enable editing, click $_c + E\n" +
            "To go next difference, click $_s + N\n" +
            "To go previous difference, click $_s + P\n" +
            "To close files, click $_c + C\n").replace("$_c", selector).replace("$_s", multiple);

    private static final Border labelTopPadding = BorderFactory.createEmptyBorder(screenSize.height / 6, 0, 0, 0);
    private JMenuItem openFiles;
    private JMenuItem closeFiles;
    private JMenuItem about;
    private JMenuItem nextMatch;
    private JMenuItem previousMatch;
    private JMenuItem helpTip;
    private Container startContentPane;

    public DifferenceSwingComponent() throws HeadlessException {
        initComponents();
    }

    public void initComponents() {
        startContentPane = startContentPane();
        setPreferredSize(screenSize);
        log.info("preferred size {}", screenSize.toString());
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

        JMenu navigate = new JMenu("Navigate");
        navigate.getAccessibleContext().setAccessibleDescription("The navigate menu");
        navigate.setMnemonic(KeyEvent.VK_N);

        nextMatch = new JMenuItem("Next Match");
        nextMatch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_DOWN_MASK));
        navigate.add(nextMatch);

        previousMatch = new JMenuItem("Previous Match");
        previousMatch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.META_DOWN_MASK));
        navigate.add(previousMatch);

        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_F10);
        help.getAccessibleContext().setAccessibleDescription("The help dialog");

        helpTip = new JMenuItem("Shortcuts");
        helpTip.setMnemonic(KeyEvent.VK_F11);
        helpTip.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, KeyEvent.META_DOWN_MASK));
        help.add(helpTip);

        about = new JMenuItem("About");
        about.setMnemonic(KeyEvent.VK_F12);
        about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, KeyEvent.META_DOWN_MASK));
        help.add(about);

        menuBar.add(file);
        menuBar.add(navigate);
        menuBar.add(help);

        return menuBar;
    }

    private Container startContentPane() {
        JPanel openPanel = new JPanel();
        openPanel.setLayout(new BoxLayout(openPanel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(welcomeTipMessage, SwingConstants.CENTER);
        label.setBorder(labelTopPadding);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton button = new JButton("Click here to open two files to diff ... ");
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        openPanel.add(label);
        return openPanel;
    }

    public JSplitPane differenceContentPane() {
        JScrollPane left = buildDifferencePanel();
        JScrollPane right = buildDifferencePanel();
        JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        main.setResizeWeight(0.5f);
        main.setEnabled(false);
        main.setOneTouchExpandable(false);
        return main;
    }

    public void synchronizedScroll(JScrollPane left, JScrollPane right) {
        BoundedRangeModel yModel, xModel;
        yModel = left.getVerticalScrollBar().getModel();
        xModel = left.getHorizontalScrollBar().getModel();
        right.getVerticalScrollBar().setModel(yModel);
        right.getHorizontalScrollBar().setModel(xModel);
    }

    private JScrollPane buildDifferencePanel() {
        JTextPane textPane = new JTextPane();
        textPane.setText("Content of the file will shown here ... ");
        textPane.setEditable(false);
        textPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVisible(false);

        return scrollPane;
    }

    public JMenuItem getOpenFiles() {
        return openFiles;
    }

    public JMenuItem getCloseFiles() {
        return closeFiles;
    }

    public JMenuItem getAbout() {
        return about;
    }

    public JMenuItem getNextMatch() {
        return nextMatch;
    }

    public JMenuItem getPreviousMatch() {
        return previousMatch;
    }

    public Container getStartContentPane() {
        return startContentPane;
    }

    public JMenuItem getHelpTip() {
        return helpTip;
    }

    public String getHelpTipMessage() {
        return helpTipMessage;
    }
}
