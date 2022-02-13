package x.funny.co;

import x.funny.co.controller.ActionController;
import x.funny.co.controller.DefaultActionController;
import x.funny.co.model.SplitSolutionDiffFinder;
import x.funny.co.model.DifferenceBetweenBlobs;
import x.funny.co.view.DifferenceSwingComponent;

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
            Thread.setDefaultUncaughtExceptionHandler(
                    (t, e) -> logger.error("uncaught exception occurred in the thread='{}'", e, t.toString()));
            try {
                if (MACOS) {
                    System.setProperty("apple.laf.useScreenMenuBar", "true");
                    UIManager.setLookAndFeel("com.apple.laf.AquaLookAndFeel");
                }
            } catch (Exception ignored) {
                logger.warn("could not setLookAndFeel for MACOS, default theme will be used ...");
            }
            predefine();
            logger.info("application has been started successfully ... ");
        });
    }

    private static void predefine() {
        SplitSolutionDiffFinder diffFinder = new SplitSolutionDiffFinder();
        DifferenceSwingComponent ui = new DifferenceSwingComponent();
        DifferenceBetweenBlobs model = new DifferenceBetweenBlobs(diffFinder);

        ActionController actionController = new DefaultActionController(ui, model);
        actionController.dispatch();
    }
}
