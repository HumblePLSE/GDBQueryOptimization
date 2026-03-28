package org.example.project.cypher.standard_ast;

import org.example.project.cypher.ast.IClause;

/**
 * Abstract class representing a generic Reading Sub-Clause in a Cypher query.
 * Reading Sub-Clauses include WHERE, ORDER BY, LIMIT, and SKIP/OFFSET.
 */
public abstract class ReadingSubClause extends Clause {

    /**
     * Constructor for ReadingSubClause.
     * @param clauseType The type of the clause (e.g., WHERE, ORDER BY, LIMIT, SKIP).
     */
    public ReadingSubClause(String clauseType) {
        super(clauseType); // Call the Clause constructor with the clause type
    }

    /**
     * Convert the ReadingSubClause into Cypher query syntax.
     * The specific implementation will depend on the subclass.
     *
     * @return A String representing the Cypher syntax for this sub-clause.
     */
    @Override
    public abstract String toCypher();

    /**
     * Validate the ReadingSubClause to ensure it is well-formed.
     * The specific implementation will depend on the subclass.
     *
     * @return True if the sub-clause is valid, false otherwise.
     */
    @Override
    public abstract boolean validate();
}
