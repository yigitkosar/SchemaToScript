package edu.rit.g2.view;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/**
 * A Swing-based implementation of the {@link SchemaView} abstraction.
 *
 * <p>
 * This view is responsible for presenting SQL output, status messages, and
 * error notifications to the user through standard Swing UI components. It
 * directly updates a {@link JTextArea} for SQL display and a {@link JLabel}
 * for status reporting, making it suitable for desktop-based interaction.
 * </p>
 *
 * <p>
 * This class does not contain business logic. Instead, it strictly follows MVC
 * principles by displaying information passed from the controller through
 * {@link SchemaView}'s abstract methods.
 * </p>
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *   <li>Show success messages in green</li>
 *   <li>Show error messages in red and in a popup dialog</li>
 *   <li>Display generated SQL in a non-editable text area</li>
 *   <li>Provide feedback on file save locations</li>
 * </ul>
 *
 * @author Acer
 * @version 1.0
 * @since 1.0
 */
public class SchemaSwingView extends SchemaView {

    /** Text area where generated SQL is displayed to the user. */
    private final JTextArea sqlArea;

    /** Label for status updates such as success or error messages. */
    private final JLabel statusLabel;

    /**
     * Constructs a Swing-based view with UI components dedicated for SQL display
     * and status messaging.
     *
     * @param sqlArea     the text area used for showing generated SQL
     * @param statusLabel the label used to show success/error/status messages
     */
    public SchemaSwingView(JTextArea sqlArea, JLabel statusLabel) {
        this.sqlArea = sqlArea;
        this.statusLabel = statusLabel;
    }

    /**
     * Displays a success message to the user.
     * <p>
     * Success text is colored green for positive visual feedback.
     * </p>
     *
     * @param message the message to display
     */
    @Override
    public void showSuccess(String message) {
        statusLabel.setForeground(new Color(20, 120, 20));
        statusLabel.setText("Success: " + message);
    }

    /**
     * Displays an error message to the user.
     * <p>
     * Error text is colored red and additionally shown in a Swing dialog.
     * </p>
     *
     * @param message the message to display
     */
    @Override
    public void showError(String message) {
        statusLabel.setForeground(new Color(170, 25, 25));
        statusLabel.setText("Error: " + message);
        JOptionPane.showMessageDialog(
            null,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Displays the generated SQL output.
     * <p>
     * The caret is reset to the start of the text to ensure the user sees the
     * beginning of the SQL output.
     * </p>
     *
     * @param sql the SQL string to display; if {@code null}, the area is cleared
     */
    @Override
    public void showSql(String sql) {
        sqlArea.setText(sql == null ? "" : sql);
        sqlArea.setCaretPosition(0);
    }

    /**
     * Displays a message informing the user where the generated SQL was saved.
     *
     * @param path the filesystem path where SQL was written
     */
    @Override
    public void savedTo(String path) {
        statusLabel.setForeground(new Color(20, 120, 20));
        statusLabel.setText("Saved to: " + path);
    }
}