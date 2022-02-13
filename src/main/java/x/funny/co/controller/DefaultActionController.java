package x.funny.co.controller;

import x.funny.co.Logger1;
import x.funny.co.model.Blob;
import x.funny.co.model.DifferenceBetweenBlobs;
import x.funny.co.view.DifferenceSwingComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

public class ActionController {
    private static final Logger1 log = Logger1.logger(ActionController.class);
    private static final int MB_1 = 1024 * 1024 * 1024;
    private final DifferenceSwingComponent view;
    private final DifferenceBetweenBlobs model;

    public ActionController(DifferenceSwingComponent view, DifferenceBetweenBlobs model) {
        this.view = view;
        this.model = model;

        bindItemListener(view.getEnableEditing(), new EnableEditing());
        bindActionListener(view.getOpenFiles(), new OpenFileAction());
        bindActionListener(view.getCloseFiles(), new ClosFileAction());
        bindActionListener(view.getAbout(), e -> JOptionPane.showMessageDialog(null, "Jetbrains test task"));
        bindActionListener(view.getHelpTip(), e -> JOptionPane.showMessageDialog(null, view.getHelpTipMessage()));
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new JTextPaneKeyEventDispatcher());
    }

    private void bindActionListener(AbstractButton abstractButton, ActionListener actionListener) {
        abstractButton.addActionListener(actionListener);
    }

    private void bindItemListener(AbstractButton abstractButton, ItemListener itemListener) {
        abstractButton.addItemListener(itemListener);
    }

    public void dispatch() {
        view.pack();
        view.setVisible(true);
    }

    public class JTextPaneKeyEventDispatcher implements KeyEventDispatcher {
        int position = 0;

        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            Component component = e.getComponent();
            JTextPane textPane = null;
            if (component instanceof JTextPane) {
                textPane = (JTextPane) component;
            }

            if (textPane == null) {
                return false;
            }
            final List<Integer> pointer = model.getDiffPositions();
            if (e.getKeyCode() == KeyEvent.VK_N) {
                if (position < pointer.size() - 1) {
                    position++;
                }
                if (position < pointer.size()) {
                    textPane.setCaretPosition(pointer.get(position));
                }

            }
            if (e.getKeyCode() == KeyEvent.VK_P) {
                if (position > 0) {
                    position--;
                }
                if (position < pointer.size()) {
                    textPane.setCaretPosition(pointer.get(position));
                }
            }
            return false;
        }
    }

    private class EnableEditing implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (model.canBeCompared()) {
                log.info("enabled editing mode is activated");
                int stateChange = e.getStateChange();
                JEditorPane left = model.getLeft().getContent();
                JEditorPane right = model.getRight().getContent();
                left.setEditable(stateChange == ItemEvent.SELECTED);
                right.setEditable(stateChange == ItemEvent.SELECTED);
            }
        }
    }

    private class ClosFileAction implements ActionListener {
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
            JFileChooser fileChooser = new JFileChooser();
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
        Blob target = new Blob(file, (JTextPane) scrollPane.getViewport().getView());
        model.appendFile(target, constraint);
    }
}
