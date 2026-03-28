package org.example.project.cypher.standard_ast;

import org.example.project.cypher.ast.IClause;

/**
 * Abstract class representing a generic Reading/Writing Sub-Clause in a Cypher query.
 * Reading/Writing Sub-Clauses include MERGE, MATCH, CREATE, etc.
 */
public abstract class ReadingWritingClause extends Clause {

    /**
     * Constructor for ReadingWritingClause.
     * @param clauseType The type of the clause (e.g., MERGE, MATCH, CREATE).
     */
    public ReadingWritingClause(String clauseType) {
        super(clauseType); // Call the Clause constructor with the clause type
    }

    /**
     * Convert the ReadingWritingClause into Cypher query syntax.
     * The specific implementation will depend on the subclass.
     *
     * @return A String representing the Cypher syntax for this sub-clause.
     */
    @Override
    public abstract String toCypher();

    /**
     * Validate the ReadingWritingClause to ensure it is well-formed.
     * The specific implementation will depend on the subclass.
     *
     * @return True if the sub-clause is valid, false otherwise.
     */
    @Override
    public abstract boolean validate();
}
