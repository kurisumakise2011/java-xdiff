package x.funny.co;

import java.util.MissingResourceException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Logger1 extends Logger {
    static {
        String path = Main.class.getResource("/logging.properties").getPath();
        System.setProperty("java.util.logging.config.file", path);
    }

    public Logger1(Class<?> clazz) {
        this(clazz.getName(), null);
    }

    /**
     * Protected method to construct a logger for a named subsystem.
     * <p>
     * The logger will be initially configured with a null Level
     * and with useParentHandlers set to true.
     *
     * @param name               A name for the logger.  This should
     *                           be a dot-separated name and should normally
     *                           be based on the package name or class name
     *                           of the subsystem, such as java.net
     *                           or javax.swing.  It may be null for anonymous Loggers.
     * @param resourceBundleName name of ResourceBundle to be used for localizing
     *                           messages for this logger.  May be null if none
     *                           of the messages require localization.
     * @throws MissingResourceException if the resourceBundleName is non-null and
     *                                  no corresponding resource can be found.
     */
    protected Logger1(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }

    public static Logger1 logger(Class<?> clazz) {
        Logger1 logger1 = new Logger1(clazz);
        logger1.addHandler(new ConsoleHandler());
        return logger1;
    }

    public void trace(String message, Object... params) {
        logWithParams(Level.FINEST, message, params);
    }

    public void debug(String message, Object... params) {
        logWithParams(Level.FINE, message, params);
    }

    public void info(String message, Object... params) {
        logWithParams(Level.INFO, message, params);
    }

    public void warn(String message, Object... params) {
        logWithParams(Level.WARNING, message, params);
    }

    public void error(String message, Object... params) {
        logWithParams(Level.SEVERE, message, params);
    }

    private void logWithParams(Level level, String message, Object... params) {
        if (params.length > 0) {
            Throwable t = null;
            int start = 0;
            if (params[0] instanceof Throwable) {
                t = (Throwable) params[0];
                start = 1;
            }
            message = message.replace("{}", "%s");
            for (int i = start; i < params.length; i++) {
                message = String.format(message, params);
            }
            if (t != null) {
                log(level, message, t);
            } else {
                log(level, message);
            }
        }
    }
}
