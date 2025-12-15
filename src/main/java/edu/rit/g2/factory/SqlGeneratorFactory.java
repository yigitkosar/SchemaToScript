package edu.rit.g2.factory;

import edu.rit.g2.generator.MySqlGenerator;
import edu.rit.g2.generator.OracleGenerator;
import edu.rit.g2.generator.PostgreGenerator;
import edu.rit.g2.generator.SqlGenerator;
import edu.rit.g2.parser.DbmsType;

/**
 * A factory class responsible for creating the appropriate
 * {@link SqlGenerator} implementation based on a given {@link DbmsType}.
 *
 * <p>This class follows the <strong>Factory Method</strong> design pattern.
 * Instead of directly instantiating SQL generator classes throughout the
 * application, all generator creation is centralized in one place. This ensures
 * consistency, reduces coupling, and makes the system easier to extend.</p>
 *
 * <p>Supported mappings:</p>
 * <ul>
 *     <li>{@link DbmsType#MYSQL} &nbsp;&rarr; {@link MySqlGenerator}</li>
 *     <li>{@link DbmsType#POSTGRES} &nbsp;&rarr; {@link PostgreGenerator}</li>
 *     <li>{@link DbmsType#ORACLE} &nbsp;&rarr; {@link OracleGenerator}</li>
 * </ul>
 *
 * <p>All SQL generators implement the same {@link SqlGenerator} interface,
 * ensuring a common API for the rest of the application.</p>
 *
 * <p>This is a non-instantiable utility class. All creation logic is exposed
 * through the static {@link #of(DbmsType)} method.</p>
 *
 * @since 1.0
 */
public final class SqlGeneratorFactory {

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>As a static utility class, {@code SqlGeneratorFactory} should never
     * be instantiated. The private constructor enforces this design rule.</p>
     */
    private SqlGeneratorFactory() {}

    /**
     * Returns a concrete {@link SqlGenerator} implementation for the given
     * database management system type.
     *
     * <p>This method uses a {@code switch} expression to return the correct
     * SQL generator. All supported DBMS types map to dedicated implementations
     * of {@link SqlGenerator}.</p>
     *
     * <pre>
     * // Example usage:
     * SqlGenerator g1 = SqlGeneratorFactory.of(DbmsType.MYSQL);
     * SqlGenerator g2 = SqlGeneratorFactory.of(DbmsType.POSTGRES);
     * </pre>
     *
     * @param type the selected database type (e.g., {@link DbmsType#MYSQL});
     *             must not be {@code null}
     *
     * @return a corresponding {@link SqlGenerator} instance
     *
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static SqlGenerator of(DbmsType type) {
        return switch (type) {
            case MYSQL -> new MySqlGenerator();
            case POSTGRES -> new PostgreGenerator();
            case ORACLE -> new OracleGenerator();
        };
    }
}