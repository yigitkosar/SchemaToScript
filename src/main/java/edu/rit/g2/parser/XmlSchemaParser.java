package edu.rit.g2.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.rit.g2.model.SchemaModel;

/**
 * Parses XML-based schema definition files and converts them into a {@link SchemaModel}.  
 * This parser expects an XML structure containing {@code <schema>} as the root element,
 * with nested {@code <table>}, {@code <columns>}, {@code <column>}, and 
 * {@code <relationships>} elements.
 *
 * <p>The parser validates the structure, extracts table names, column definitions,
 * and relationship metadata, and produces a unified internal representation used by
 * the Schema2Script application during SQL script generation.</p>
 *
 * @author Nik Kaučić
 */
public class XmlSchemaParser implements SchemaParser {

    /** Logger for reporting parsing progress, warnings, and errors. */
    private static final Logger logger = LogManager.getLogger(XmlSchemaParser.class);

    /**
     * Parses an XML schema file and converts it into a {@link SchemaModel}.
     *
     * @param schemaFile the XML schema file to parse
     * @return a populated {@link SchemaModel}
     * @throws SchemaParsingException if parsing fails or the file is invalid
     */
    @Override
    public SchemaModel parse(File schemaFile) throws SchemaParsingException {
        logger.info("Starting XML schema parsing...");

        validateSchemaFile(schemaFile);

        try {
            Document doc = buildDocument(schemaFile);
            Element root = getValidatedRootElement(doc);

            List<Map<String, Object>> tables = parseTables(root);

            SchemaModel model = new SchemaModel("XML Schema");
            model.setTables(tables);

            logger.info("Parsed {} tables successfully from XML.", tables.size());
            return model;

        } catch (SchemaParsingException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during XML parsing", e);
            throw new SchemaParsingException("Unexpected error during XML parsing: " + e.getMessage(), e);
        }
    }

    /**
     * Validates that the XML file exists and is not null.
     *
     * @param schemaFile the XML file
     * @throws SchemaParsingException if missing or invalid
     */
    private void validateSchemaFile(File schemaFile) throws SchemaParsingException {
        if (schemaFile == null) {
            logger.error("Schema file is null");
            throw new SchemaParsingException("Schema file cannot be null");
        }
        if (!schemaFile.exists()) {
            logger.error("Schema file does not exist: {}", schemaFile.getAbsolutePath());
            throw new SchemaParsingException("Schema file does not exist: " + schemaFile.getAbsolutePath());
        }
    }

    /**
     * Builds a DOM document from the XML file.
     *
     * @param schemaFile XML input file
     * @return DOM document
     * @throws SchemaParsingException if XML cannot be parsed
     */
    private Document buildDocument(File schemaFile) throws SchemaParsingException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringElementContentWhitespace(true);
            dbf.setNamespaceAware(false);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(schemaFile);
            doc.getDocumentElement().normalize();
            return doc;

        } catch (ParserConfigurationException e) {
            throw new SchemaParsingException("XML Parser configuration error: " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new SchemaParsingException("XML parsing error (SAX): " + e.getMessage(), e);
        } catch (IOException e) {
            throw new SchemaParsingException("File I/O error during XML parsing: " + e.getMessage(), e);
        }
    }

    /**
     * Validates that the document contains a {@code <schema>} root node.
     *
     * @param doc DOM document
     * @return the validated root element
     * @throws SchemaParsingException if missing or incorrect
     */
    private Element getValidatedRootElement(Document doc) throws SchemaParsingException {
        Element root = doc.getDocumentElement();
        if (root == null || !"schema".equalsIgnoreCase(root.getTagName())) {
            throw new SchemaParsingException("Root element {@code <schema>} is required");
        }
        return root;
    }

    /**
     * Parses all {@code <table>} elements inside the schema.
     *
     * @param root the {@code <schema>} element
     * @return list of parsed table metadata
     * @throws SchemaParsingException if tables are malformed
     */
    private List<Map<String, Object>> parseTables(Element root) throws SchemaParsingException {
        List<Map<String, Object>> tables = new ArrayList<>();
        NodeList tableNodes = root.getElementsByTagName("table");

        for (int i = 0; i < tableNodes.getLength(); i++) {
            Element tableEl = (Element) tableNodes.item(i);
            tables.add(parseTableElement(tableEl));
        }
        return tables;
    }

    /**
     * Parses a single {@code <table>} element.
     *
     * @param tableEl XML element
     * @return parsed table metadata
     * @throws SchemaParsingException if table is missing a name
     */
    private Map<String, Object> parseTableElement(Element tableEl) throws SchemaParsingException {
        String tableName = tableEl.getAttribute("name");
        if (tableName == null || tableName.isBlank()) {
            throw new SchemaParsingException("Each {@code <table>} must have a non-empty 'name' attribute");
        }

        Map<String, Object> table = new HashMap<>();
        table.put("tableName", tableName);
        table.put("columns", parseColumns(tableEl, tableName));
        table.put("relationships", parseRelationships(tableEl));
        return table;
    }

    /**
     * Parses {@code <column>} elements inside a table.
     *
     * @param tableEl table element
     * @param tableName name of the table
     * @return list of column metadata
     * @throws SchemaParsingException if a column is missing its name
     */
    private List<Map<String, String>> parseColumns(Element tableEl, String tableName) throws SchemaParsingException {
        List<Map<String, String>> columns = new ArrayList<>();

        NodeList columnsList = tableEl.getElementsByTagName("columns");
        if (columnsList.getLength() > 0) {
            Element colsEl = (Element) columnsList.item(0);
            NodeList colNodes = colsEl.getElementsByTagName("column");

            for (int c = 0; c < colNodes.getLength(); c++) {
                Element colEl = (Element) colNodes.item(c);

                String columnName = colEl.getAttribute("name");
                if (columnName == null || columnName.isBlank()) {
                    throw new SchemaParsingException(
                            "Each {@code <column>} in table '" + tableName + "' must have a non-empty 'name' attribute");
                }

                Map<String, String> col = new HashMap<>();
                col.put("name", columnName);

                String type = colEl.getAttribute("type");
                col.put("type", (type == null || type.isBlank()) ? "VARCHAR(255)" : type);

                columns.add(col);
            }
        }
        return columns;
    }

    /**
     * Parses {@code <relationship>} elements in a table.
     *
     * @param tableEl table element
     * @return list of relationships
     */
    private List<Map<String, String>> parseRelationships(Element tableEl) {
        List<Map<String, String>> relationships = new ArrayList<>();

        NodeList relsList = tableEl.getElementsByTagName("relationships");
        if (relsList.getLength() > 0) {
            Element relsEl = (Element) relsList.item(0);
            NodeList relNodes = relsEl.getElementsByTagName("relationship");

            for (int r = 0; r < relNodes.getLength(); r++) {
                Element relEl = (Element) relNodes.item(r);

                Map<String, String> rel = new HashMap<>();
                rel.put("relationshipType", relEl.getAttribute("relationshipType"));
                rel.put("relatedTable", relEl.getAttribute("relatedTable"));
                rel.put("throughTable", relEl.getAttribute("throughTable"));
                rel.put("foreignKey", relEl.getAttribute("foreignKey"));
                rel.put("relatedForeignKey", relEl.getAttribute("relatedForeignKey"));

                relationships.add(rel);
            }
        }

        return relationships;
    }
}