package edu.rit.g2.factory;

import edu.rit.g2.parser.JsonSchemaParser;
import edu.rit.g2.parser.SchemaParser;
import edu.rit.g2.parser.XmlSchemaParser;

/**
 * A factory class responsible for creating appropriate {@link SchemaParser}
 * implementations based on an input schema format.
 *
 * <p>This class follows the <strong>Factory Method</strong> design pattern,
 * centralizing the logic for parser creation. Rather than instantiating parser
 * classes directly, client code calls {@link #get(String)} to retrieve the
 * correct parser depending on the selected schema type.</p>
 *
 * <p>Supported formats:</p>
 * <ul>
 *     <li><strong>json</strong> → {@link JsonSchemaParser}</li>
 *     <li><strong>xml</strong>  → {@link XmlSchemaParser}</li>
 * </ul>
 *
 * <p>If an unsupported or invalid format is provided, an
 * {@link IllegalArgumentException} is thrown. This ensures early failure and
 * clear feedback for the user or calling component.</p>
 *
 * <p>The class is implemented as a non-instantiable utility class — all methods
 * are static, and the constructor is private.</p>
 *
 * @since 1.0
 */
public class ParserFactory {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * <p>The factory is not meant to be instantiated. All parser creation
     * functions are exposed through static methods.</p>
     */
    private ParserFactory() {
        // Utility class to prevent instantiation
    }

    /**
     * Returns an appropriate {@link SchemaParser} implementation based on the
     * provided format string.
     *
     * <p>The format lookup is case-insensitive. Valid examples:</p>
     * <pre>
     *     ParserFactory.get("json");
     *     ParserFactory.get("XML");
     *     ParserFactory.get("Json");
     * </pre>
     *
     * @param format the schema format to parse (e.g., "json", "xml");
     *               must not be {@code null}
     *
     * @return a corresponding {@link SchemaParser} instance
     *
     * @throws IllegalArgumentException if {@code format} is {@code null} or
     *         does not match any supported type
     */
    public static SchemaParser get(String format) {
        if (format == null) {
            throw new IllegalArgumentException("Format cannot be null");
        }

        switch (format.toLowerCase()) {
            case "json":
                return new JsonSchemaParser();
            case "xml":
                return new XmlSchemaParser();
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }
}