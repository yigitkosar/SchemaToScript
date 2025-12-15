package edu.rit.g2.generator;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.rit.g2.model.SchemaModel;

/**
 * Generates SQL DDL statements for MySQL based on a parsed {@link SchemaModel}.
 * <p>
 * This implementation converts the internal schema representation into
 * executable MySQL <code>CREATE TABLE</code> statements, including:
 * </p>
 * <ul>
 *   <li>Column definitions</li>
 *   <li>Primary key inference</li>
 *   <li>Foreign key relationships</li>
 *   <li>Toggling of foreign key checks</li>
 * </ul>
 *
 * <p>
 * The generator is invoked through the
 * {@link edu.rit.g2.factory.SqlGeneratorFactory} when the
 * user selects MySQL as the output DBMS. This class is stateless and safe to reuse.
 * </p>
 *
 * @author Frane
 * @version 1.0
 * @since 1.0
 */
public class MySqlGenerator implements SqlGenerator {

    private static final Logger logger = LogManager.getLogger(MySqlGenerator.class);

    /** Key used to read related foreign key details inside relationship maps. */
    private static final String RELATED_FOREIGN_KEY = "relatedForeignKey";

    /** Key used to access a table’s relationship list from the schema map. */
    private static final String RELATIONSHIPS = "relationships";

    /**
     * Generates MySQL SQL text from the provided {@link SchemaModel}.
     * <p>
     * The generation process includes:
     * </p>
     * <ul>
     *   <li>Disabling foreign key checks</li>
     *   <li>Iterating through all schema-defined tables</li>
     *   <li>Generating column definitions</li>
     *   <li>Inferring primary keys (fields ending in <code>_id</code>)</li>
     *   <li>Generating foreign key constraints for many-to-one relationships</li>
     *   <li>Re-enabling foreign key checks</li>
     * </ul>
     *
     * @param schema the parsed schema representation; must not be {@code null}
     * @return a String containing valid MySQL CREATE TABLE DDL statements
     *
     * @throws IllegalArgumentException if the schema is {@code null}
     */
    @Override
    public String generate(SchemaModel schema) {
        logger.info("Starting MySQL generation...");

        if (schema == null) {
            logger.error("Received null schema. Aborting SQL generation.");
            throw new IllegalArgumentException("SchemaModel cannot be null");
        }

        StringBuilder sql = new StringBuilder("SET FOREIGN_KEY_CHECKS=0;\n\n");

        List<Map<String, Object>> tables = schema.getTables();
        if (tables == null || tables.isEmpty()) {
            logger.warn("No tables found in schema; returning empty SQL body with FK checks toggled");
            sql.append("SET FOREIGN_KEY_CHECKS=1;\n");
            return sql.toString();
        }

        for (Map<String, Object> table : tables) {
            String tableName = (String) table.get("tableName");
            if (tableName == null || tableName.isBlank()) {
                logger.warn("Encountered table with missing name; skipping.");
                continue;
            }
            logger.debug("Generating SQL for table: {}", tableName);

            sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (\n");

            @SuppressWarnings("unchecked")
            List<Map<String, String>> columns = (List<Map<String, String>>) table.get("columns");
            generateColumns(sql, columns, tableName);

            String pkName = findPrimaryKey(columns);
            if (pkName != null) {
                sql.append(",  PRIMARY KEY (").append(pkName).append(")\n");
            }

            generateRelationships(sql, table);

            sql.append(") ENGINE=InnoDB;\n\n");
        }

        sql.append("SET FOREIGN_KEY_CHECKS=1;\n");
        logger.info("SQL generation completed.");
        return sql.toString();
    }

    /**
     * Generates column definitions for a table and appends them into the SQL buffer.
     * <p>
     * If no columns exist, a default <code>id INT</code> placeholder is used.
     * </p>
     *
     * @param sql a StringBuilder to append SQL text to
     * @param columns the list of column definitions, each represented as a map
     * @param tableName the name of the table (used for logging)
     */
    private void generateColumns(StringBuilder sql, List<Map<String, String>> columns, String tableName) {
        if (columns == null || columns.isEmpty()) {
            logger.warn("Table {} has no columns; creating placeholder id INT", tableName);
            sql.append("  id INT\n");
            return;
        }
        for (int i = 0; i < columns.size(); i++) {
            Map<String, String> col = columns.get(i);
            String colName = col.get("name");
            String colType = col.getOrDefault("type", "VARCHAR(255)");
            sql.append("  ").append(colName).append(" ").append(colType);
            if (i < columns.size() - 1) sql.append(",");
            sql.append("\n");
        }
    }

    /**
     * Attempts to infer the primary key for a table.
     * <p>
     * A convention is used: any column whose name ends with <code>_id</code>
     * is treated as the table’s primary key.
     * </p>
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
     * Generates foreign key constraints for tables that define <code>many-to-one</code>
     * relationships. Only these relationship types are currently supported.
     *
     * @param sql the SQL buffer to append constraints to
     * @param table the table metadata map containing potential relationship info
     */
    @SuppressWarnings("unchecked")
    private void generateRelationships(StringBuilder sql, Map<String, Object> table) {
        List<Map<String, String>> relationships =
                (List<Map<String, String>>) table.getOrDefault(RELATIONSHIPS, List.of());

        for (Map<String, String> rel : relationships) {
            String type = rel.get("relationshipType");
            if (type == null) continue;

            if ("many-to-one".equalsIgnoreCase(type)) {
                String fkCol = rel.get("foreignKey");
                String relatedTable = rel.get("relatedTable");
                String relatedFk =
                        (rel.get(RELATED_FOREIGN_KEY) != null && !rel.get(RELATED_FOREIGN_KEY).isBlank())
                        ? rel.get(RELATED_FOREIGN_KEY)
                        : "id";

                if (fkCol != null && relatedTable != null) {
                    sql.append(",  FOREIGN KEY (").append(fkCol).append(") REFERENCES `")
                       .append(relatedTable).append("(").append(relatedFk).append("`)\n");
                }
            }
        }
    }
}