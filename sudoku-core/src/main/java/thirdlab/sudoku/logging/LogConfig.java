package thirdlab.sudoku.logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class LogConfig {
    private static final Logger LOG = Logger.getLogger(LogConfig.class.getName());

    private LogConfig() {
    }

    public static void init() {
        init(LogConfig.class.getClassLoader().getResourceAsStream("logging.properties"));
    }

    static void init(InputStream input) {
        if (input == null) {
            LOG.warning("logging.properties was not found");
            installStdOut();
            return;
        }
        try (input) {
            LogManager.getLogManager().readConfiguration(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to configure logging", exception);
        }
        // Handlers are installed here, not named in logging.properties.
        // Android's LogManager loads handler classes with the system class loader
        // and would not see an application class listed in the file.
        installStdOut();
        LOG.info("Logging configured");
    }

    public static void useHandler(Handler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler is required");
        }
        attach("thirdlab.sudoku", handler);
        attach("com.thirdlab.sudoku", handler);
    }

    private static void attach(String name, Handler handler) {
        Logger logger = Logger.getLogger(name);
        logger.setLevel(Level.FINE);
        logger.setUseParentHandlers(false);
        for (Handler existing : logger.getHandlers()) {
            logger.removeHandler(existing);
        }
        logger.addHandler(handler);
    }

    private static void installStdOut() {
        Logger root = Logger.getLogger("");
        for (Handler handler : root.getHandlers()) {
            root.removeHandler(handler);
        }
        StdOutHandler stdout = new StdOutHandler();
        stdout.setFormatter(new SimpleFormatter());
        stdout.setLevel(Level.FINE);
        root.addHandler(stdout);
        root.setLevel(Level.INFO);
        Logger.getLogger("thirdlab.sudoku").setLevel(Level.FINE);
        Logger.getLogger("com.thirdlab.sudoku").setLevel(Level.FINE);
    }
}
