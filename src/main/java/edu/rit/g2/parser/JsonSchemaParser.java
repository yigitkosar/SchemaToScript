package edu.rit.g2.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.rit.g2.model.SchemaModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses schema definitions written in JSON format and converts them into a {@link SchemaModel}.
 * This parser expects the top-level JSON structure to be an array of table objects, where each
 * table defines its name, columns, and optional relationships.  
 *
 * <p>The parser extracts table metadata, column definitions, and relationship mappings and builds
 * a unified {@code SchemaModel} instance used by the Schema2Script application for downstream
 * SQL generation.</p>
 *
 * @author Nik Kaučić
 */
public class JsonSchemaParser implements SchemaParser {

    /**
     * Jackson {@link ObjectMapper} used for reading and mapping JSON schema data.
     */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Logger instance used for reporting parsing progress, warnings, and errors.
     */
    private static final Logger logger = LogManager.getLogger(JsonSchemaParser.class);

    /**
     * JSON key used for extracting relationship definitions from a table node.
     */
    private static final String RELATIONSHIPS = "relationships";

    /**
     * Parses a JSON schema file and converts it into a {@link SchemaModel}.  
     * The method validates the file, reads its contents, verifies the structure,
     * and processes table definitions into an internal model representation.
     *
     * @param schemaFile the JSON file containing the schema definition
     * @return a populated {@link SchemaModel} instance derived from the JSON input
     * @throws SchemaParsingException if the file is missing, unreadable, or incorrectly structured
     */
    @Override
    public SchemaModel parse(File schemaFile) throws SchemaParsingException {

        logger.info("Parsing JSON schema from file: {}", (schemaFile == null ? "null" : schemaFile.getPath()));

        if (schemaFile == null || !schemaFile.exists()) {
            logger.error("Schema file does not exist: {}", schemaFile);
            throw new SchemaParsingException("Schema file does not exist: " + schemaFile);
        }

        try (InputStream in = new FileInputStream(schemaFile)) {
            logger.debug("Opened input stream for schema file: {}", schemaFile);

            JsonNode root = mapper.readTree(in);
            logger.debug("Parsed JSON schema successfully");

            if (!root.isArray()) {
                logger.error("Top-level JSON must be an array of table definitions");
                throw new SchemaParsingException("Top-level JSON must be an array of table definitions");
            }

            List<Map<String, Object>> tables = new ArrayList<>();

            for (JsonNode tableNode : root) {
                tables.add(parseTable(tableNode));
            }

            SchemaModel model = new SchemaModel("JSON Schema");
            model.setTables(tables);
            logger.info("Parsed {} tables successfully.", tables.size());
            return model;

        } catch (IOException e) {
            logger.error("Failed to read schema file: {}", schemaFile, e);
            throw new SchemaParsingException("Failed to read schema file: " + schemaFile, e);
        }
    }

    /**
     * Parses a single table definition from the given JSON node.  
     * Extracts the table name, column definitions, and relationship mappings.
     *
     * @param tableNode the JSON node representing a table definition
     * @return a map containing the parsed table metadata
     * @throws SchemaParsingException if required fields such as {@code tableName} are missing
     */
    private Map<String, Object> parseTable(JsonNode tableNode) throws SchemaParsingException {
        Map<String, Object> table = new HashMap<>();

        String tableName = tableNode.path("tableName").asText(null);
        if (tableName == null || tableName.isBlank()) {
            throw new SchemaParsingException("Missing 'tableName' in one of the table definitions");
        }
        table.put("tableName", tableName);
        table.put("columns", parseColumns(tableNode));
        table.put(RELATIONSHIPS, parseRelationships(tableNode));
        return table;
    }

    /**
     * Extracts column definitions from a table node.  
     * Each column must define a {@code name}, and may optionally specify {@code type}.
     *
     * @param tableNode the JSON node containing a {@code columns} array
     * @return a list of column metadata maps
     */
    private List<Map<String, String>> parseColumns(JsonNode tableNode) {
        List<Map<String, String>> columns = new ArrayList<>();
        for (JsonNode colNode : tableNode.path("columns")) {
            Map<String, String> col = new HashMap<>();
            col.put("name", colNode.path("name").asText());
            col.put("type", colNode.path("type").asText("VARCHAR(255)"));
            columns.add(col);
        }
        return columns;
    }

    /**
     * Extracts relationship definitions from a table node, if present.  
     * Supports relationship type, related table, through-table, and foreign key references.
     *
     * @param tableNode the JSON node containing an optional {@code relationships} array
     * @return a list of relationship metadata maps
     */
    private List<Map<String, String>> parseRelationships(JsonNode tableNode) {
        List<Map<String, String>> relationships = new ArrayList<>();
        if (tableNode.has(RELATIONSHIPS)) {
            for (JsonNode relNode : tableNode.path(RELATIONSHIPS)) {
                Map<String, String> rel = new HashMap<>();
                rel.put("relationshipType", relNode.path("relationshipType").asText(null));
                rel.put("relatedTable", relNode.path("relatedTable").asText(null));
                rel.put("throughTable", relNode.path("throughTable").asText(null));
                rel.put("foreignKey", relNode.path("foreignKey").asText(null));
                rel.put("relatedForeignKey", relNode.path("relatedForeignKey").asText(null));
                relationships.add(rel);
            }
        }
        return relationships;
    }
}