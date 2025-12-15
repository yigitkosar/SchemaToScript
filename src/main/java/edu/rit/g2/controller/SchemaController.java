package edu.rit.g2.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import edu.rit.g2.factory.ParserFactory;
import edu.rit.g2.factory.SqlGeneratorFactory;
import edu.rit.g2.generator.SqlGenerator;
import edu.rit.g2.model.SchemaModel;
import edu.rit.g2.parser.DbmsType;
import edu.rit.g2.parser.SchemaParser;
import edu.rit.g2.parser.SchemaParsingException;
import edu.rit.g2.view.SchemaView;

/**
 * Controller class responsible for coordinating interactions between the
 * {@link SchemaModel} (data layer) and {@link SchemaView} (presentation layer)
 * within the Schema2Script application.
 *
 * <p>This class implements the <strong>Controller</strong> role of the MVC
 * architectural pattern. It:
 * <ul>
 *     <li>Loads and parses schema definition files (JSON or XML).</li>
 *     <li>Updates the {@link SchemaModel} with parsed data.</li>
 *     <li>Handles SQL generation requests using the selected DBMS generator.</li>
 *     <li>Notifies the view about success or error outcomes.</li>
 * </ul>
 *
 * <p>All functional operations performed by the view — such as loading a file,
 * parsing it, selecting DBMS types, and generating SQL — are delegated to this
 * controller to maintain proper separation of concerns.</p>
 *
 * @since 1.0
 */
public class SchemaController {

    /**
     * The model holding the parsed schema data used for SQL generation.
     */
    private final SchemaModel model;

    /**
     * The view responsible for displaying status messages and generated SQL output.
     */
    private final SchemaView view;

    /**
     * Creates a new {@code SchemaController} instance.
     *
     * @param model the schema model representing the application's data state;
     *              must not be {@code null}
     * @param view  the view used to display messages and results to the user;
     *              must not be {@code null}
     *
     * @throws IllegalArgumentException if either parameter is {@code null}
     */
    public SchemaController(SchemaModel model, SchemaView view) {
        if (model == null) throw new IllegalArgumentException("SchemaModel cannot be null");
        if (view == null)  throw new IllegalArgumentException("SchemaView cannot be null");
        this.model = model;
        this.view = view;
    }

    /**
     * Loads and parses a schema file by automatically detecting its format
     * based on the filename extension.
     *
     * <p>If the format cannot be determined (i.e., not JSON or XML), the view is
     * notified of the error and parsing is aborted.</p>
     *
     * @param schemaFile the file selected by the user; may be {@code null}
     */
    public void handleSchemaUpload(File schemaFile) {
        if (schemaFile == null) {
            view.showError("No file selected.");
            return;
        }
        String format = detectFormat(schemaFile.getName());
        if (format.isEmpty()) {
            view.showError("Cannot determine schema format from file name: " + schemaFile.getName());
            return;
        }
        handleSchemaUpload(schemaFile, format);
    }

    /**
     * Loads and parses a schema file using the explicitly provided format.
     *
     * <p>The method validates the input, retrieves the appropriate parser via
     * {@link ParserFactory}, attempts to parse the file, and updates the model
     * if successful.</p>
     *
     * <p>Any parsing- or format-related errors are communicated back through
     * the {@link SchemaView}.</p>
     *
     * @param schemaFile the schema file to parse; must not be {@code null}
     * @param format     the file format (e.g., "json", "xml"); must not be empty
     */
    public void handleSchemaUpload(File schemaFile, String format) {
        if (schemaFile == null) {
            view.showError("No file selected.");
            return;
        }
        if (format == null || format.trim().isEmpty()) {
            view.showError("No format provided.");
            return;
        }

        try {
            SchemaParser parser = ParserFactory.get(format.trim().toLowerCase());
            SchemaModel parsed = parser.parse(schemaFile);

            if (parsed == null) {
                view.showError("Parsed schema is null. Check your parser implementation.");
                return;
            }

            model.setSchema(parsed);
            view.showSuccess("Schema parsed successfully as " + format.toUpperCase()
                    + " → " + parsed);

        } catch (SchemaParsingException e) {
            view.showError("Parsing failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            view.showError("Unsupported format: " + e.getMessage());
        } catch (Exception e) {
            view.showError("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Detects the file format based on its filename extension.
     *
     * @param filename the name of the file to inspect
     * @return the format string ("json" or "xml"), or an empty string if
     *         no supported extension is found
     */
    private String detectFormat(String filename) {
        if (filename == null) return "";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".json")) return "json";
        if (lower.endsWith(".xml"))  return "xml";
        return "";
    }

    public void handleColumnEdit(String tableName, int columnIndex, String newName, String newType) {
        if (model.getSchema() == null) {
            view.showError("No schema loaded.");
            return;
        }
    
        model.getSchema().updateColumn(tableName, columnIndex, newName, newType);
        view.showSuccess("Updated: " + newName + " (" + newType + ")");
    }

    public SchemaModel getModel() {
        return model;
    }
    
    public void handleGenerateSqlFromModel(SchemaModel edited, DbmsType dbms) {
        try {
            SqlGenerator generator = SqlGeneratorFactory.of(dbms);
            String sql = generator.generateLogged(edited);
    
            view.showSuccess("SQL successfully generated!");
            view.showSql(sql);
    
            Files.createDirectories(Path.of("target/generated"));
            Files.writeString(Path.of("target/generated/schema.sql"), sql);
            view.savedTo("target/generated/schema.sql");
    
        } catch (Exception e) {
            view.showError("SQL generation failed: " + e.getMessage());
        }
    }

}