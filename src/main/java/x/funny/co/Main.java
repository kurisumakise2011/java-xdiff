package x.funny.co;

import javax.swing.*;
import java.util.Arrays;
import java.util.Locale;

public class Main {
    private static final Logger1 logger = Logger1.logger(Main.class);
    public static final String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    public static final boolean MACOS = (OS.contains("mac")) || (OS.contains("darwin"));

    public static void main(String[] args) {
        logger.info("running the application with args: '{}'", Arrays.toString(args));
        logger.info("current operation system is " + OS);
        SwingUtilities.invokeLater(() -> {
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                logger.error("uncaught exception occurred in the thread='{}'", e, t.toString());
            });
            try {
                if (MACOS) {
                    System.setProperty("apple.laf.useScreenMenuBar", "true");
                    UIManager.setLookAndFeel("com.apple.laf.AquaLookAndFeel");
                }
            } catch (Exception ignored) {
                logger.warn("could not setLookAndFeel for MACOS, default theme will be used ...");
            }

            DifferenceSwingComponent ui = new DifferenceSwingComponent();
            ui.pack();
            ui.setVisible(true);
            logger.info("application has been started successfully ... ");
        });
    }
}
