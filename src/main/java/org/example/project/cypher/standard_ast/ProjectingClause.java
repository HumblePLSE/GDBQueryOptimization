package org.example.project.cypher.standard_ast;

import org.example.project.cypher.gen.GraphManager;

/**
 * projecting_clause ::= return_clause | with_clause | unwind_clause | finish_clause
 * Abstract class representing a generic projecting clause in a Cypher query.
 * Each specific projecting clause (RETURN, WITH, UNWIND, FINISH) will be a subclass of this.
 */
public abstract class ProjectingClause extends Clause {

    public ProjectingClause(String clauseType) {
        super(clauseType);
    }

    @Override
    public abstract String toCypher();

    @Override
    public abstract boolean validate();
}
