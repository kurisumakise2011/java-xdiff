package x.funny.co;

import javax.swing.*;
import java.util.Locale;

public class Main {
    public static final String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    public static final boolean MACOS = (OS.contains("mac")) || (OS.contains("darwin"));

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (MACOS) {
                    System.setProperty("apple.laf.useScreenMenuBar", "true");
                    UIManager.setLookAndFeel("com.apple.laf.AquaLookAndFeel");
                }
            } catch (Exception ignored) {
            }

            String path = Main.class.getResource("/logging.properties").getPath();
            System.setProperty("java.util.logging.config.file", path);
            DifferenceSwingComponent ui = new DifferenceSwingComponent();
            ui.pack();
            ui.setVisible(true);
        });
    }
}
