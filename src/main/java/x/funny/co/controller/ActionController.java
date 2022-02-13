package x.funny.co.controller;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;

public interface ActionController {

    void bindActionListener(AbstractButton abstractButton, ActionListener actionListener);

    void bindItemListener(AbstractButton abstractButton, ItemListener itemListener);

    void bindKeyListener(JTextPane textPane, KeyListener keyListener);

    void dispatch();
}
