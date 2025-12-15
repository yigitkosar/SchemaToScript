package edu.rit.g2.parser;

import java.io.File;

import edu.rit.g2.model.SchemaModel;

/**
 * Defines the contract for schema parsers capable of reading external schema files
 * and converting them into a {@link SchemaModel}.  
 * Implementations of this interface handle different schema formats such as JSON or XML.
 *
 * <p>Each implementation is responsible for validating the input file, interpreting
 * its structure, and producing a unified internal model used by the Schema2Script
 * application for SQL script generation.</p>
 *
 * @author Nik Kaučić
 */
public interface SchemaParser {
    /**
     * Parses the provided schema file and converts it into a {@link SchemaModel}.  
     * Implementations must validate the file format and handle malformed or
     * incomplete schema definitions appropriately.
     *
     * @param schemaFile the input file containing the schema definition
     * @return a populated {@link SchemaModel} representing the parsed schema
     * @throws SchemaParsingException if the file cannot be read or contains an invalid structure
     */
    SchemaModel parse(File schemaFile) throws SchemaParsingException;
}
