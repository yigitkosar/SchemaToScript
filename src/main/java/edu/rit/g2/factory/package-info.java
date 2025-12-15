/**
 * Provides factory classes for creating parsers and SQL generators.
 *
 * <p>This package follows the Factory Method design pattern to centralize object
 * creation for:</p>
 *
 * <ul>
 *   <li>{@link edu.rit.g2.parser.SchemaParser} implementations via
 *       {@link edu.rit.g2.factory.ParserFactory}</li>
 *   <li>{@link edu.rit.g2.generator.SqlGenerator} implementations via
 *       {@link edu.rit.g2.factory.SqlGeneratorFactory}</li>
 * </ul>
 *
 * <p>By delegating creation logic to factory classes, the application maintains
 * loose coupling and simplifies extension for new formats and DBMS types.</p>
 *
 * @since 1.0
 */
package edu.rit.g2.factory;