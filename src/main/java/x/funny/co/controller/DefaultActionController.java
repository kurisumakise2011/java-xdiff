package x.funny.co.controller;

import x.funny.co.Logger1;
import x.funny.co.view.DifferenceSwingComponent;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DefaultActionController implements ActionController {
    private static final Logger1 log = Logger1.logger(DefaultActionController.class);
    private static final int MB_1 = 1024 * 1024 * 1024;
    private final DifferenceSwingComponent view;
    private final DifferenceBetweenBlobs model;
    private final String dir;

    public DefaultActionController(DifferenceSwingComponent view, DifferenceBetweenBlobs model) {
        this(view, model, ".");
    }

    public DefaultActionController(DifferenceSwingComponent view, DifferenceBetweenBlobs model, String dir) {
        this.view = view;
        this.model = model;
        this.dir = dir;

        bindActionListener(view.getOpenFiles(), new OpenFileAction());
        bindActionListener(view.getCloseFiles(), new CloseFileAction());
        bindActionListener(view.getAbout(), e -> JOptionPane.showMessageDialog(null, "Jetbrains test task"));
        bindActionListener(view.getHelpTip(), e -> JOptionPane.showMessageDialog(null, view.getHelpTipMessage()));
        bindActionListener(view.getNextMatch(), new NextMatchAction());
        bindActionListener(view.getPreviousMatch(), new PreviousMatchAction());
    }

    @Override
    public void bindActionListener(AbstractButton abstractButton, ActionListener actionListener) {
        abstractButton.addActionListener(actionListener);
    }

    @Override
    public void bindItemListener(AbstractButton abstractButton, ItemListener itemListener) {
        abstractButton.addItemListener(itemListener);
    }

    @Override
    public void bindKeyListener(JTextPane textPane, KeyListener keyListener) {
        textPane.addKeyListener(keyListener);
    }

    @Override
    public void dispatch() {
        view.pack();
        view.setVisible(true);
    }

    private class NextMatchAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Can use any of contents see @synchronizedScroll
            JTextPane pane = model.getLeft().getContent();
            Deque<Integer> diffPositions = model.getDiffPositions();
            findMatch(pane, diffPositions::pollFirst, diffPositions::addLast);
        }
    }

    private class PreviousMatchAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Can use any of contents see @synchronizedScroll
            JTextPane pane = model.getLeft().getContent();
            Deque<Integer> diffPositions = model.getDiffPositions();
            findMatch(pane, diffPositions::pollLast, diffPositions::addFirst);
        }
    }

    private void findMatch(JTextPane pane, Supplier<Integer> getter, Consumer<Integer> setter) {
        SwingUtilities.invokeLater(() -> {
            Integer pos = getter.get();
            if (pos != null && model.canBeCompared()) {
                if (pos == pane.getCaretPosition()) {
                    setter.accept(pos);
                    pos = getter.get();
                    if (pos != null) {
                        updateCaret(pane, pos);
                        setter.accept(pos);
                    }
                } else {
                    updateCaret(pane, pos);
                    setter.accept(pos);
                }
            }
        });
    }

    private void updateCaret(JTextPane pane, Integer pos) {
        setPosition(pane, pos);

        view.validate();
        view.repaint();

        pane.grabFocus();
    }

    private void setPosition(JTextPane pane, Integer pos) {
        Document document = pane.getDocument();
        int start = document.getStartPosition().getOffset();
        int end = document.getEndPosition().getOffset();
        if (pos >= start && pos <= end) {
            pane.setCaretPosition(pos);
        }
    }

    private class CloseFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.info("closing current files");
            view.setContentPane(view.getStartContentPane());
            model.clear();
        }
    }

    private class OpenFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.clear();
            log.info("waiting for dialog");
            JFileChooser fileChooser = new JFileChooser(dir);
            fileChooser.setMultiSelectionEnabled(true);
            int code = fileChooser.showOpenDialog(view);
            File[] selectedFiles = fileChooser.getSelectedFiles();
            if (code == JFileChooser.CANCEL_OPTION) {
                log.info("closing dialog");
                return;
            }

            if (code == JFileChooser.APPROVE_OPTION && selectedFiles.length == 2) {
                if (selectedFiles[0].length() > MB_1) {
                    throw new SwingEventControllerRuntimeException("file cannot be bigger than 1 MB");
                }
                if (selectedFiles[1].length() > MB_1) {
                    throw new SwingEventControllerRuntimeException("file cannot be bigger than 1 MB");
                }
                SwingUtilities.invokeLater(() -> interactWithView(selectedFiles));
            }
        }

        private void interactWithView(File[] selectedFiles) {
            log.info("invoking a search of the difference between '{}' and '{}' ", selectedFiles[0], selectedFiles[1]);
            JSplitPane pane = view.differenceContentPane();
            modelAppendFiles(pane, selectedFiles);
            view.setContentPane(pane);
            model.findDiff();
            JScrollPane left = (JScrollPane) pane.getLeftComponent();
            JScrollPane right = (JScrollPane) pane.getRightComponent();
            view.synchronizedScroll(left, right);
            right.setVisible(true);
            left.setVisible(true);
            view.validate();
            view.repaint();
            log.info("difference found, repainted and validated");
        }
    }

    private void modelAppendFiles(JSplitPane pane, File[] selectedFiles) {
        JScrollPane leftScroll = (JScrollPane) pane.getLeftComponent();
        JScrollPane rightScroll = (JScrollPane) pane.getRightComponent();

        appendSingleFileToModel(leftScroll, selectedFiles[0], BorderLayout.WEST);
        appendSingleFileToModel(rightScroll, selectedFiles[1], BorderLayout.EAST);
    }

    private void appendSingleFileToModel(JScrollPane scrollPane, File file, String constraint) {
        JTextPane textPane = (JTextPane) scrollPane.getViewport().getView();
        Blob target = new Blob(file, textPane);
        model.appendFile(target, constraint);
    }
}
