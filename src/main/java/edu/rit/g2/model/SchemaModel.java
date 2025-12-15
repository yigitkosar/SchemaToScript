package edu.rit.g2.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents an in-memory data structure describing a parsed schema.
 *
 * <p>This class serves as the core model object within the application's MVC
 * architecture. It stores general schema metadata, table definitions, and
 * arbitrary key-value attributes parsed from external schema files (JSON or XML).
 * </p>
 *
 * <p>A {@code SchemaModel} instance may contain:</p>
 * <ul>
 *   <li>A schema <strong>type</strong> (e.g., json/xml)</li>
 *   <li>A generic <strong>data</strong> map containing additional metadata</li>
 *   <li>A list of <strong>tables</strong>, each represented as a map describing
 *       its columns, name, and relationships</li>
 *   <li>An embedded reference to a <strong>parsed schema</strong> (used when chaining parses)</li>
 * </ul>
 *
 * <p>The class includes detailed logging to support debugging, tracing, and
 * error analysis during schema parsing and SQL generation.</p>
 *
 * @author Robert
 * @version 1.0
 * @since 1.0
 */
public class SchemaModel {

    /** Logger for SchemaModel events, warnings, and debug output. */
    private static final Logger logger = LogManager.getLogger(SchemaModel.class);

    /** The type of schema (e.g., "json", "xml", or a custom label). */
    private String type;

    /**
     * A flexible key-value storage container for metadata extracted from the schema.
     * Keys are strings; values may be any object depending on schema content.
     */
    private Map<String, Object> data = new HashMap<>();

    /**
     * A list of tables contained in the schema. Each table is represented as a map:
     * <ul>
     *   <li><code>tableName</code> – String</li>
     *   <li><code>columns</code> – List&lt;Map&lt;String, String&gt;&gt;</li>
     *   <li><code>relationships</code> – List&lt;Map&lt;String, String&gt;&gt;</li>
     * </ul>
     */
    private List<Map<String, Object>> tables;

    /**
     * A nested SchemaModel reference used when wrapping or chaining schema parsing.
     * This allows the controller or parser to keep a reference to the original,
     * pre-processed model.
     */
    private SchemaModel parsedSchema;

    /**
     * Creates a new {@code SchemaModel} with default values.
     * Logs construction for debugging and tracing behavior.
     */
    public SchemaModel() {
        logger.info("SchemaModel default constructor called");
    }

    /**
     * Creates a {@code SchemaModel} with the specified schema type.
     *
     * @param type the type of schema (e.g., "json", "xml")
     */
    public SchemaModel(String type) {
        this.type = type;
        logger.info("SchemaModel constructor called with type: {}", type);
    }

    /**
     * Creates a mock/dummy schema model, primarily used for testing
     * or when a placeholder schema structure is required.
     *
     * @return a new SchemaModel of type "mock"
     */
    public static SchemaModel mock() {
        SchemaModel model = new SchemaModel();
        model.type = "mock";
        logger.info("SchemaModel mock constructor called");
        return model;
    }

    /**
     * Sets the parsed schema reference.
     * Useful for storing a processed, normalised, or validated version
     * of the input schema.
     *
     * @param schema the parsed schema to store; may be {@code null}
     */
    public void setSchema(SchemaModel schema) {
        if (schema == null) {
            logger.warn("Attempted to set null schema");
        } else {
            logger.info("Schema set successfully");
        }
        this.parsedSchema = schema;
    }

    /**
     * Retrieves the previously stored parsed schema reference.
     *
     * @return the nested parsed SchemaModel, or {@code null} if not set
     */
    public SchemaModel getSchema() {
        logger.info("Returning parsed schema");
        return parsedSchema;
    }

    /**
     * Returns the schema type string.
     *
     * @return the type of this schema
     */
    public String getType() {
        logger.info("Returning schema type");
        return type;
    }

    /**
     * Sets the schema type (e.g., "json", "xml").
     *
     * @param type a string representing the schema type
     */
    public void setType(String type) {
        logger.info("Setting schema type to: {}", type);
        this.type = type;
    }

    /**
     * Retrieves the metadata map. This map may contain arbitrary values
     * populated by the parser depending on the schema's structure.
     *
     * @return the metadata map
     */
    public Map<String, Object> getData() {
        logger.info("Returning schema data");
        return data;
    }

    /**
     * Inserts a key-value pair into the schema metadata map.
     *
     * @param key   the metadata key (must not be {@code null})
     * @param value the metadata value (must not be {@code null})
     */
    public void put(String key, Object value) {
        if (key == null || value == null) {
            logger.warn("Attempted to put null key or value into data");
        } else {
            logger.info("Putting key-value pair into data: {} -> {}", key, value);
        }
        this.data.put(key, value);
    }

    /**
     * Returns the list of tables contained in this schema.
     *
     * @return a list of tables, or {@code null} if none exist
     */
    public List<Map<String, Object>> getTables() {
        logger.info("Returning list of tables (size: {})", tables != null ? tables.size() : 0);
        return tables;
    }

    /**
     * Sets the list of tables represented in this schema.
     *
     * @param tables a list of maps describing table structures
     */
    public void setTables(List<Map<String, Object>> tables) {
        this.tables = tables;
        logger.info("Tables set (size: {})", tables != null ? tables.size() : 0);
    }

    public void updateColumn(String tableName, int columnIndex, String newName, String newType) {
        if (tables == null) return;
    
        for (Map<String, Object> table : tables) {
            if (tableName.equals(table.get("tableName"))) {
    
                @SuppressWarnings("unchecked")
                List<Map<String, String>> cols =
                        (List<Map<String, String>>) table.get("columns");
    
                if (cols == null || columnIndex < 0 || columnIndex >= cols.size())
                    return;
    
                Map<String, String> col = cols.get(columnIndex);
    
                col.put("name", newName);
                col.put("type", newType);
    
                return;
            }
        }
    }

    /**
     * Returns a detailed string representation of this schema model.
     *
     * @return a descriptive string containing type, metadata, and tables
     */
    @Override
    public String toString() {
        logger.info("Generating string representation of SchemaModel");
        return "SchemaModel{type='" + type + "', data=" + data + ", tables=" + tables + "}";
    }
}