package org.example.project.cypher.ast;

public interface IExpression {

    IExpression getParentExpression();
    void setParentExpression(IExpression parentExpression);

    void replaceChild(IExpression originalExpression, IExpression newExpression);

    Object getValue();

    /**
     * Converts the expression into its Cypher query representation.
     *
     * @return A string representing the expression in Cypher syntax.
     */
    String toCypher();
}
