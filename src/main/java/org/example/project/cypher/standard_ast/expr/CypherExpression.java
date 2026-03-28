package org.example.project.cypher.standard_ast.expr;

import org.example.project.cypher.ast.IExpression;

public abstract class CypherExpression implements IExpression {
    protected IExpression parentExpression;

    @Override
    public IExpression getParentExpression() {
        return parentExpression;
    }

    @Override
    public void setParentExpression(IExpression parentExpression){
        this.parentExpression = parentExpression;
    }

    /**
     * Converts the expression into its Cypher query representation.
     *
     * @return A string representing the expression in Cypher syntax.
     */
    @Override
    public abstract String toCypher();
}
