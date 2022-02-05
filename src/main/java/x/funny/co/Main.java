package x.funny.co;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String path = Main.class.getResource("/logging.properties").getPath();
            System.setProperty("java.util.logging.config.file", path);
            DifferenceSwingComponent ui = new DifferenceSwingComponent();
            ui.pack();
            ui.setVisible(true);
        });
    }
}
