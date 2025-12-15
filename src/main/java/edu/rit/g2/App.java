package edu.rit.g2;

import java.io.File;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import edu.rit.g2.ui.SchemaFrame;

/**
 * Entry point for the Schema2Script application.
 *
 * <p>
 * This class initializes the logging configuration, prepares required
 * directories, configures the system Look and Feel, and launches the graphical
 * user interface ({@link SchemaFrame}) on the Swing event dispatch thread.
 * </p>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li>Create required runtime folders (e.g., for logs)</li>
 *   <li>Initialize Log4j using the provided XML configuration</li>
 *   <li>Start the Swing-based GUI safely on the EDT</li>
 *   <li>Ensure the application adheres to platform-specific UI behavior</li>
 * </ul>
 *
 * <p>
 * This class contains no business logic and strictly serves as the environment
 * initializer and launcher.
 * </p>
 *
 * @author Yigit
 * @version 1.0
 * @since 1.0
 */
public class App {

    /**
     * Initializes logging, configures the UI theme, and launches the main GUI
     * window for the Schema2Script application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        // Ensures macOS follows system light/dark appearance
        System.setProperty("apple.awt.application.appearance", "system");

        // Create directory structure for Log4j output
        new File("target/logs").mkdirs();

        // Load custom logging configuration
        Configurator.initialize("schema2script", "src/main/resources/log4j.xml");

        Logger log = LogManager.getLogger(App.class);
        log.info("Launching GUI…");

        // Launch Swing UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Ensure UI components match OS look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                log.error("Error setting LookAndFeel", ignored);
            }

            // Show the main Schema2Script window
            new SchemaFrame().setVisible(true);
        });
    }
}