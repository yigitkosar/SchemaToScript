package edu.rit.g2.generator;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.rit.g2.model.SchemaModel;

/**
 * Generates SQL DDL statements for PostgreSQL based on a parsed {@link SchemaModel}.
 *
 * <p>This implementation mirrors the functionality of {@link MySqlGenerator},
 * but produces PostgreSQL-compatible SQL output. This includes:</p>
 *
 * <ul>
 *   <li>Column definitions using PostgreSQL data types</li>
 *   <li>Automatic conversion of PK fields ending in <code>_id</code> into <code>SERIAL</code></li>
 *   <li>Primary key inference</li>
 *   <li>Foreign key constraint generation for many-to-one relationships</li>
 * </ul>
 *
 * <p>Unlike MySQL, PostgreSQL does not use engine declarations nor foreign key
 * check toggles.</p>
 *
 * @author Yigit
 * @version 1.0
 * @since 1.0
 */
public class PostgreGenerator implements SqlGenerator {

    /** Logger instance for instrumentation and debugging output. */
    private static final Logger logger = LogManager.getLogger(PostgreGenerator.class);

    /** Key used to read related foreign key details inside relationship maps. */
    private static final String RELATED_FOREIGN_KEY = "relatedForeignKey";

    /** Key used to access a table’s relationship list from the schema map. */
    private static final String RELATIONSHIPS = "relationships";

    /**
     * Generates PostgreSQL SQL text from the provided {@link SchemaModel}.
     *
     * <p>The output includes <code>CREATE TABLE</code> statements for every
     * table defined in the schema, including inferred primary keys and foreign
     * key relationships.</p>
     *
     * @param schema the parsed schema representation; must not be {@code null}
     * @return a String containing valid PostgreSQL CREATE TABLE DDL statements
     *
     * @throws IllegalArgumentException if the schema is {@code null}
     */
    @Override
    public String generate(SchemaModel schema) {
        logger.info("Starting PostgreSQL generation...");

        if (schema == null) {
            logger.error("Received null schema. Aborting SQL generation.");
            throw new IllegalArgumentException("SchemaModel cannot be null");
        }

        StringBuilder sql = new StringBuilder();

        List<Map<String, Object>> tables = schema.getTables();
        if (tables == null || tables.isEmpty()) {
            logger.warn("No tables found in schema; returning empty SQL.");
            return sql.toString();
        }

        for (Map<String, Object> table : tables) {
            String tableName = (String) table.get("tableName");
            if (tableName == null || tableName.isBlank()) {
                logger.warn("Encountered table with missing name; skipping.");
                continue;
            }

            logger.debug("Generating SQL for table: {}", tableName);

            sql.append("CREATE TABLE IF NOT EXISTS \"")
               .append(tableName)
               .append("\" (\n");

            @SuppressWarnings("unchecked")
            List<Map<String, String>> columns = (List<Map<String, String>>) table.get("columns");
            generateColumns(sql, columns, tableName);

            String pkName = findPrimaryKey(columns);
            if (pkName != null) {
                sql.append(",  PRIMARY KEY (\"").append(pkName).append("\")\n");
            }

            generateRelationships(sql, table);

            sql.append(");\n\n");
        }

        logger.info("PostgreSQL SQL generation complete.");
        return sql.toString();
    }

    /**
     * Generates column definitions for a table and appends them into the SQL buffer.
     *
     * <p>Primary keys ending in <code>_id</code> are converted to PostgreSQL
     * <code>SERIAL</code> columns to emulate auto-increment behavior.</p>
     *
     * @param sql a StringBuilder to append SQL text to
     * @param columns the list of column definitions
     * @param tableName the name of the table (used for logging)
     */
    private void generateColumns(StringBuilder sql, List<Map<String, String>> columns, String tableName) {
        if (columns == null || columns.isEmpty()) {
            logger.warn("Table {} has no columns; creating placeholder id SERIAL", tableName);
            sql.append("  id SERIAL\n");
            return;
        }

        for (int i = 0; i < columns.size(); i++) {
            Map<String, String> col = columns.get(i);
            String colName = col.get("name");
            String colType = col.getOrDefault("type", "VARCHAR(255)");

            // PostgreSQL: auto-increment rule
            if (colName.endsWith("_id")) {
                colType = "SERIAL";
            }

            sql.append("  \"").append(colName).append("\" ").append(colType);

            if (i < columns.size() - 1) sql.append(",");
            sql.append("\n");
        }
    }

    /**
     * Attempts to infer the primary key for a table.
     *
     * <p>A convention is used: any column whose name ends with <code>_id</code>
     * is treated as the table’s primary key.</p>
     *
     * @param columns the list of column maps
     * @return the inferred primary key name, or {@code null} if not found
     */
    private String findPrimaryKey(List<Map<String, String>> columns) {
        if (columns == null) return null;
        for (Map<String, String> col : columns) {
            String name = col.get("name");
            if (name != null && name.endsWith("_id")) {
                return name;
            }
        }
        return null;
    }

    /**
     * Generates foreign key constraints for tables that define
     * <code>many-to-one</code> relationships.
     *
     * @param sql the SQL buffer to append constraints to
     * @param table the table metadata map containing relationship info
     */
    @SuppressWarnings("unchecked")
    private void generateRelationships(StringBuilder sql, Map<String, Object> table) {

        List<Map<String, String>> relationships =
                (List<Map<String, String>>) table.getOrDefault(RELATIONSHIPS, List.of());

        for (Map<String, String> rel : relationships) {

            String type = rel.get("relationshipType");
            if (type == null || !type.equalsIgnoreCase("many-to-one")) continue;

            String fkCol = rel.get("foreignKey");
            String relatedTable = rel.get("relatedTable");

            String relatedFk =
                    (rel.get(RELATED_FOREIGN_KEY) != null && !rel.get(RELATED_FOREIGN_KEY).isBlank())
                            ? rel.get(RELATED_FOREIGN_KEY)
                            : "id";

            sql.append(",  FOREIGN KEY (\"")
               .append(fkCol)
               .append("\") REFERENCES \"")
               .append(relatedTable)
               .append("\"(\"")
               .append(relatedFk)
               .append("\")\n");
        }
    }
}