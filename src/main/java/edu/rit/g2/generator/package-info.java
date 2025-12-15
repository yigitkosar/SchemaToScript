/**
 * Contains SQL generator interfaces and implementations.
 *
 * <p>The central interface {@link edu.rit.g2.generator.SqlGenerator}
 * defines the contract for converting a parsed {@link edu.rit.g2.model.SchemaModel}
 * into SQL source code.</p>
 *
 * <p>This package includes database-specific generators, such as:</p>
 *
 * <ul>
 *   <li>{@link edu.rit.g2.generator.MySqlGenerator}</li>
 *   <li>{@link edu.rit.g2.generator.PostgreGenerator}</li>
 *   <li>{@link edu.rit.g2.generator.OracleGenerator}</li>
 * </ul>
 *
 * <p>These classes are created using
 * {@link edu.rit.g2.factory.SqlGeneratorFactory}.</p>
 *
 * @since 1.0
 */
package edu.rit.g2.generator;