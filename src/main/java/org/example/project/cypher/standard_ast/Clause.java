package org.example.project.cypher.standard_ast;

import org.example.project.cypher.ast.IClause;

/**
 * Abstract class representing a generic Clause in a Cypher query.
 * Provides common functionality for all specific clause implementations.
 */
public abstract class Clause implements IClause {

    protected String clauseType; // 子句的名称（例如：MATCH、CREATE、RETURN）

    public Clause(String clauseType) {
        this.clauseType = clauseType;
    }

    /**
     * Converts the clause into its corresponding Cypher query representation.
     * This method must be implemented by concrete subclasses.
     *
     * @return A string representing the clause in valid Cypher syntax.
     */
    @Override
    public abstract String toCypher();

    /**
     * Validates the clause to ensure it is well-formed.
     * Subclasses can override this method to provide specific validation logic.
     *
     * @return True if the clause is valid, otherwise false.
     */
    @Override
    public boolean validate() {
        return clauseType != null && !clauseType.isEmpty();
    }

    /**
     * Returns the name of the clause.
     *
     * @return The clause name as a string.
     */
    public String getClauseType() {
        return clauseType;
    }
}
