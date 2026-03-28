package org.example.project.cypher.ast;

/**
 * Represents a generic Clause in a Cypher query.
 * All specific clauses (e.g., MATCH, CREATE, RETURN) must implement this interface.
 */
public interface IClause {

    /**
     * Converts the clause into its corresponding Cypher query representation.
     *
     * @return A string representing the clause in valid Cypher syntax.
     */
    String toCypher();

    /**
     * Validates the clause to ensure it is well-formed and follows Cypher syntax rules.
     * This method can be optionally overridden by implementing classes.
     *
     * @return True if the clause is valid, otherwise false.
     */
    default boolean validate() {
        return true; // Default implementation assumes the clause is always valid
    }
}
