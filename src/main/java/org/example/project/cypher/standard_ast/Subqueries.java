package org.example.project.cypher.standard_ast;

import org.example.project.cypher.gen.GraphManager;

/**
 * subqueries ::= call_subquery | exists_subquery | count_subquery | ...
 * Abstract class representing a generic subquery in Cypher.
 * Each specific subquery type (CALL {}, EXISTS {}, etc.) will be a subclass of this.
 */
public abstract class Subqueries extends Clause {

    public Subqueries(String subqueryType) {
        super(subqueryType);
    }

    @Override
    public abstract String toCypher();

    @Override
    public abstract boolean validate();

}