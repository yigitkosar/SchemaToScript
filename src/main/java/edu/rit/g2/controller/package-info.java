/**
 * Contains the controller layer of the Schema2Script application.
 *
 * <p>This package implements the “C” of the MVC pattern. Controllers handle
 * user actions, coordinate between parsers, models, and SQL generators, and
 * communicate results to the appropriate view implementation.</p>
 *
 * <p>The primary class, {@link edu.rit.g2.controller.SchemaController},
 * manages:</p>
 * <ul>
 *   <li>Schema file uploads</li>
 *   <li>Format detection</li>
 *   <li>Delegating parsing work to {@code parser} package</li>
 *   <li>Delegating SQL generation to {@code generator} package</li>
 *   <li>Reporting results to {@code view} package</li>
 * </ul>
 *
 * <p>This package does not perform UI rendering or database logic; it purely
 * orchestrates workflow.</p>
 *
 * @since 1.0
 */
package edu.rit.g2.controller;