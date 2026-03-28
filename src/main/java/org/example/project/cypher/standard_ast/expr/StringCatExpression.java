package org.example.project.cypher.standard_ast.expr;

import org.example.project.cypher.ast.IExpression;

import java.util.Map;

public class StringCatExpression extends CypherExpression {
    private IExpression left, right;

    public IExpression getLeft() {
        return left;
    }

    public void setLeft(IExpression left) {
        this.left = left;
    }

    public IExpression getRight() {
        return right;
    }

    public void setRight(IExpression right) {
        this.right = right;
    }

    public StringCatExpression(IExpression left, IExpression right) {
        this.left = left;
        this.right = right;
        left.setParentExpression(this);
        right.setParentExpression(this);
    }

    @Override
    public String toCypher() {
        StringBuilder sb=new StringBuilder();
        sb.append("(");
        sb.append(left.toCypher());
        sb.append(" + ");
        sb.append(right.toCypher());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        if (originalExpression == left) {
            this.left = newExpression;
            newExpression.setParentExpression(this);
        } else if (originalExpression == right) {
            this.right = newExpression;
            newExpression.setParentExpression(this);
        } else {
            throw new RuntimeException("Expression not found as a child.");
        }
    }

    @Override
    public Object getValue() {
        Object leftValue = left.getValue();
        Object rightValue = right.getValue();

        if (leftValue == null || rightValue == null) {
            return null;
        }

        if (leftValue instanceof String && rightValue instanceof String) {
            return (String) leftValue + (String) rightValue;
        }

        throw new RuntimeException("Invalid types for StringCatExpression: " +
                leftValue.getClass() + " and " + rightValue.getClass());
    }
}
