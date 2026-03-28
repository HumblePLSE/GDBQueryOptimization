package org.example.project.cypher.standard_ast;

import org.example.project.cypher.ast.IClause;

/**
 * Abstract class representing a generic writing clause in a Cypher query.
 * WritingClause includes operations like CREATE, DELETE, DETACH DELETE, SET, and REMOVE.
 */
public abstract class WritingClause extends Clause {

    public WritingClause(String clauseType) {
        super(clauseType); // 调用 Clause 的构造函数
    }

    /**
     * Convert the clause into Cypher query syntax.
     * The specific implementation will depend on the subclass.
     *
     * @return A String representing the Cypher syntax for this clause.
     */
    @Override
    public abstract String toCypher();

    /**
     * Validate the clause to ensure it is well-formed.
     * The specific implementation will depend on the subclass.
     *
     * @return True if the clause is valid, false otherwise.
     */
    @Override
    public abstract boolean validate();
}
