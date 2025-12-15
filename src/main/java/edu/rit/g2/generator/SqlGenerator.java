package edu.rit.g2.generator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.rit.g2.model.SchemaModel;

/**
 * Defines the contract for components capable of converting a {@link SchemaModel}
 * into SQL DDL (Data Definition Language) statements.
 *
 * <p>This interface is implemented by database-specific generators
 * (MySQL, PostgreSQL, Oracle), each responsible for producing SQL
 * compatible with a particular DBMS.</p>
 *
 * <p>The interface also includes the helper method
 * {@link #generateLogged(SchemaModel)}, which wraps SQL generation with
 * logging and uniform error handling.</p>
 *
 * <p><strong>Design Pattern:</strong> Strategy Pattern</p>
 */
public interface SqlGenerator {

    /** Shared logger instance used by all SQL generators. */
    Logger log = LogManager.getLogger(SqlGenerator.class);

    /**
     * Generates SQL DDL statements based on the provided schema.
     *
     * @param schema the input schema model; must not be {@code null}
     * @return SQL string
     * @throws IllegalArgumentException if the schema is {@code null}
     */
    String generate(SchemaModel schema);

    /**
     * Wraps {@link #generate(SchemaModel)} with logging.
     *
     * <p>This method:</p>
     * <ul>
     *     <li>Validates schema is not null</li>
     *     <li>Logs start of generation</li>
     *     <li>Logs schema details in debug mode</li>
     *     <li>Delegates SQL generation</li>
     *     <li>Logs SQL length and preview</li>
     *     <li>Logs errors and rethrows</li>
     * </ul>
     *
     * @param schema the schema to convert into SQL
     * @return generated SQL
     * @throws IllegalArgumentException if schema is null
     */
    default String generateLogged(SchemaModel schema) {
        if (schema == null) {
            log.error("SQL generator called with null schema");
            throw new IllegalArgumentException("SchemaModel cannot be null");
        }

        log.info("Starting SQL generation for schema: {}", schema.getType());

        try {
            if (log.isDebugEnabled()) {
                log.debug("Schema details: {}", schema);
            }

            String sql = generate(schema);

            if (sql == null || sql.isEmpty()) {
                log.warn("SQL generation returned empty result for schema: {}", schema.getType());
            } else {
                log.info("SQL generation finished. Length={} chars", sql.length());

                if (log.isDebugEnabled()) {
                    log.debug("SQL Preview (first 100 chars): {}", 
                              sql.substring(0, Math.min(100, sql.length())));
                }
            }

            return sql;

        } catch (Exception e) {
            log.error("Error during SQL generation for schema: {}", schema.getType(), e);
            throw e;
        }
    }
}