package edu.rit.g2.view;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base view abstraction for displaying messages and SQL output within the
 * Schema2Script MVC architecture.
 *
 * <p>
 * This class defines the minimal set of methods needed by controllers to
 * communicate with any supported user interface. It acts as the abstract
 * "View" layer in the MVC pattern, with concrete subclasses (such as
 * {@link edu.rit.g2.view.SchemaSwingView}) providing actual UI-specific
 * rendering.
 * </p>
 *
 * <p>
 * By default, this implementation logs all feedback using Log4j. Subclasses
 * may override one or more methods to present information visually (e.g.,
 * Swing windows) while still benefiting from shared behavior.
 * </p>
 *
 * <h2>Design Role:</h2>
 * <ul>
 *   <li>Acts as a generic communication interface for the Controller → View path</li>
 *   <li>Ensures no UI logic leaks into the Controller</li>
 *   <li>Provides default behavior for non-GUI or logging-only environments</li>
 * </ul>
 *
 * @author Acar
 * @version 1.0
 * @since 1.0
 */
public class SchemaView {

    /** Shared logger for all base view operations. */
    private static final Logger logger = LogManager.getLogger(SchemaView.class);

    /**
     * Displays a success message to the user.
     * <p>
     * Default behavior is logging the message as an info-level event.
     * Subclasses may override this method to show custom visual feedback.
     * </p>
     *
     * @param message the success message to display
     */
    public void showSuccess(String message) {
        logger.info("[SUCCESS] {}", message);
    }

    /**
     * Displays an error message to the user.
     * <p>
     * Default behavior logs an error-level message. Subclasses may override
     * this method to display GUI dialogs or other types of alerts.
     * </p>
     *
     * @param message the error message to display
     */
    public void showError(String message) {
        logger.error("[ERROR] {}", message);
    }

    /**
     * Displays generated SQL to the user.
     * <p>
     * The base implementation logs the SQL content. Concrete view classes may
     * override this to display the SQL in a window or text panel.
     * </p>
     *
     * @param sql the SQL text to display
     */
    public void showSql(String sql) {
        logger.info("\n----- GENERATED SQL -----\n{}", sql);
    }

    /**
     * Reports that the SQL file has been successfully saved.
     * <p>
     * Default behavior logs an informational message. GUI implementations
     * override this to provide visual confirmation.
     * </p>
     *
     * @param path the filesystem location of the saved SQL file
     */
    public void savedTo(String path) {
        logger.info("[SUCCESS] SQL saved to: {}", path);
    }
}