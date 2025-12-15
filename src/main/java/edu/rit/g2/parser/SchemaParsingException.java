package edu.rit.g2.parser;

/**
 * Exception thrown when a schema file cannot be parsed correctly.  
 * This may occur due to missing fields, invalid structure, unsupported formats,
 * or low-level read errors encountered during schema processing.
 *
 * <p>Parsing implementations use this exception to signal any condition
 * that prevents successful conversion of an external schema file into a
 * {@link edu.rit.g2.model.SchemaModel}.</p>
 *
 * @author Nik Kaučić
 */
public class SchemaParsingException extends Exception {

    /**
     * Creates a new exception with a descriptive message.
     *
     * @param message description of the parsing error
     */
    public SchemaParsingException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a message and a cause.
     *
     * @param message description of the parsing error
     * @param cause underlying exception
     */
    public SchemaParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}