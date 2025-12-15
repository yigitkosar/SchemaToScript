/**
 * Provides schema parsing logic and related components.
 *
 * <p>This package defines how schema files (JSON, XML, etc.) are converted into
 * {@link edu.rit.g2.model.SchemaModel} instances.</p>
 *
 * <p>Includes:</p>
 * <ul>
 *   <li>{@link edu.rit.g2.parser.SchemaParser} – top-level parser interface</li>
 *   <li>{@link edu.rit.g2.parser.JsonSchemaParser}</li>
 *   <li>{@link edu.rit.g2.parser.XmlSchemaParser}</li>
 *   <li>{@link edu.rit.g2.parser.DbmsType} – supported SQL dialects</li>
 *   <li>{@link edu.rit.g2.parser.SchemaParsingException} – parsing error type</li>
 * </ul>
 *
 * <p>Parsers are instantiated using {@link edu.rit.g2.factory.ParserFactory}.</p>
 *
 * @since 1.0
 */
package edu.rit.g2.parser;