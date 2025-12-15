/**
 * Contains the core data model for the Schema2Script application.
 *
 * <p>The key class {@link edu.rit.g2.model.SchemaModel} acts as the container
 * for parsed schema data, including:</p>
 *
 * <ul>
 *   <li>Schema type (XML or JSON)</li>
 *   <li>Raw schema data map</li>
 *   <li>List of tables and their metadata</li>
 *   <li>Relationships between tables</li>
 * </ul>
 *
 * <p>This package holds only data representation—no parsing or SQL generation.
 * All transformation logic occurs in the {@code parser} and {@code generator}
 * packages.</p>
 *
 * @since 1.0
 */
package edu.rit.g2.model;