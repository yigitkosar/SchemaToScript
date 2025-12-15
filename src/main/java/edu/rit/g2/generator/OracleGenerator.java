package edu.rit.g2.generator;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.rit.g2.model.SchemaModel;

/**
 * Generates SQL DDL statements for Oracle based on a parsed {@link SchemaModel}.
 *
 * <p>This implementation mirrors the behavior of {@link MySqlGenerator},
 * but produces Oracle-compatible SQL syntax. This includes:</p>
 *
 * <ul>
 *   <li>Column definitions using Oracle data types (e.g., VARCHAR2)</li>
 *   <li>Primary key inference</li>
 *   <li>Foreign key generation for many-to-one relationships</li>
 * </ul>
 *
 * <p>Note: This version does not include Oracle sequences or triggers for
 * auto-increment columns, as that feature is not represented in the schema
 * input format. It focuses on structural DDL generation.</p>
 *
 * @author Acar
 * @version 1.0
 * @since 1.0
 */
public class OracleGenerator implements SqlGenerator {

    /** Logger instance used by Oracle SQL generator. */
    private static final Logger logger = LogManager.getLogger(OracleGenerator.class);

    /** Key used to read related foreign key details inside relationship maps. */
    private static final String RELATED_FOREIGN_KEY = "relatedForeignKey";

    /** Key used to access a table’s relationship list from the schema map. */
    private static final String RELATIONSHIPS = "relationships";

    /**
     * Generates Oracle SQL text from the provided {@link SchemaModel}.
     *
     * @param schema the parsed schema representation; must not be {@code null}
     * @return a String containing valid Oracle CREATE TABLE DDL statements
     *
     * @throws IllegalArgumentException if schema is {@code null}
     */
    @Override
    public String generate(SchemaModel schema) {
        logger.info("Starting Oracle SQL generation...");

        if (schema == null) {
            logger.error("Null schema received.");
            throw new IllegalArgumentException("SchemaModel cannot be null");
        }

        StringBuilder sql = new StringBuilder();

        List<Map<String, Object>> tables = schema.getTables();
        if (tables == null || tables.isEmpty()) {
            logger.warn("No tables found in schema.");
            return sql.toString();
        }

        for (Map<String, Object> table : tables) {

            String tableName = (String) table.get("tableName");
            if (tableName == null || tableName.isBlank()) {
                logger.warn("Encountered unnamed table; skipping.");
                continue;
            }

            logger.debug("Generating SQL for table: {}", tableName);

            sql.append("CREATE TABLE ").append(tableName).append(" (\n");

            @SuppressWarnings("unchecked")
            List<Map<String, String>> columns = (List<Map<String, String>>) table.get("columns");
            generateColumns(sql, columns, tableName);

            String pkName = findPrimaryKey(columns);
            if (pkName != null) {
                sql.append(",  PRIMARY KEY (").append(pkName).append(")\n");
            }

            generateRelationships(sql, table);

            sql.append(");\n\n");
        }

        logger.info("Oracle SQL generation complete.");
        return sql.toString();
    }

    /**
     * Generates column definitions for a table using Oracle conventions.
     *
     * <p>Any column with a missing or empty type defaults to
     * <code>VARCHAR2(255)</code>, since Oracle requires explicit sizing for
     * string types.</p>
     *
     * @param sql the StringBuilder buffer to append SQL to
     * @param columns list of column maps
     * @param tableName name of the table
     */
    private void generateColumns(StringBuilder sql, List<Map<String, String>> columns, String tableName) {
        if (columns == null || columns.isEmpty()) {
            logger.warn("Table {} has no columns; inserting placeholder id NUMBER", tableName);
            sql.append("  id NUMBER\n");
            return;
        }

        for (int i = 0; i < columns.size(); i++) {

            Map<String, String> col = columns.get(i);
            String colName = col.get("name");
            String colType = col.getOrDefault("type", "VARCHAR2(255)");

            // Oracle prefers VARCHAR2 instead of VARCHAR
            if (colType.toUpperCase().startsWith("VARCHAR")) {
                colType = colType.replace("VARCHAR", "VARCHAR2");
            }

            sql.append("  ").append(colName).append(" ").append(colType);

            if (i < columns.size() - 1) sql.append(",");
            sql.append("\n");
        }
    }

    /**
     * Infers primary key fields by naming convention: columns ending in <code>_id</code>.
     *
     * @param columns table column list
     * @return primary key column name or {@code null}
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
     * Generates foreign key relationships for Oracle SQL DDL output.
     *
     * <p>Only many-to-one relationships are supported, which produce
     * simple <code>FOREIGN KEY</code> constraints.</p>
     *
     * @param sql DDL output buffer
     * @param table table metadata containing relationship entries
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

            sql.append(",  FOREIGN KEY (")
               .append(fkCol)
               .append(") REFERENCES ")
               .append(relatedTable)
               .append("(")
               .append(relatedFk)
               .append(")\n");
        }
    }
}