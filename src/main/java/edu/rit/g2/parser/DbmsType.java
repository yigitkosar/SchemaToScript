package edu.rit.g2.parser;

/**
 * Represents the supported Database Management System (DBMS) types that the
 * Schema2Script application can work with. Each enum constant corresponds to a
 * specific database platform for which schema definitions may be parsed or
 * generated.
 *
 * <p>This enumeration is used throughout the parsing and script-generation
 * process to determine DBMS-specific rules, formatting conventions, and SQL
 * syntax variations.</p>
 *
 * @author Nik Kaučić
 */
public enum DbmsType {

    /**
     * Represents the MySQL database management system.
     */
    MYSQL,

    /**
     * Represents the PostgreSQL database management system.
     */
    POSTGRES,

    /**
     * Represents the Oracle database management system.
     */
    ORACLE
}