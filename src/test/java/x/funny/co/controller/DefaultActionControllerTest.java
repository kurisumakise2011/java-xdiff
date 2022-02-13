package x.funny.co.controller;

import org.junit.jupiter.api.Test;
import x.funny.co.Main;
import x.funny.co.model.DiffFinder;
import x.funny.co.model.DifferenceBetweenBlobs;
import x.funny.co.model.SplitSolutionDiffFinder;
import x.funny.co.view.DifferenceSwingComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultActionControllerTest {

    /**
     *
     * CAUTION!
     * This test requires access to PC.
     *
     */
    @Test
    public void testMatch() throws AWTException, UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (Main.MACOS) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            UIManager.setLookAndFeel("com.apple.laf.AquaLookAndFeel");
        }
        DiffFinder diffFinder = new SplitSolutionDiffFinder();
        DifferenceSwingComponent view = new DifferenceSwingComponent();
        DifferenceBetweenBlobs betweenBlobs = new DifferenceBetweenBlobs(diffFinder);
        DefaultActionController controller = new DefaultActionController(view, betweenBlobs, getClass().getResource("/").getPath());
        view.setPreferredSize(new Dimension(1440, 900));

        controller.dispatch();
        Robot bot = new Robot();
        bot.setAutoDelay(100);

        // Move to menu
        bot.mouseMove(150, 20);

        // Open menu
        bot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);

        bot.mouseMove(150, 50);

        // Open dialog
        bot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);
        bot.mouseMove(500, 320);

        // Open files directory
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        // Choose files
        bot.keyPress(KeyEvent.VK_SHIFT);
        bot.mouseMove(500, 320);

        bot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);

        bot.mouseMove(500, 340);

        bot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);

        bot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);
        bot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);
        bot.keyPress(KeyEvent.VK_SHIFT);

        // Select files
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        bot.mouseMove(930, 615);
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        assertEquals(1, betweenBlobs.getDiffPositions().size());
    }

}
