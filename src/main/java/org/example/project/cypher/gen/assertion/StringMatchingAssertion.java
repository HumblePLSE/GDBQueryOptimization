package org.example.project.cypher.gen.assertion;

import org.example.project.cypher.standard_ast.expr.StringMatchingExpression;

public class StringMatchingAssertion implements ExpressionAssertion {
    private StringMatchingExpression.StringMatchingOperation operation;
    private Object string;
    private boolean target;

    public StringMatchingAssertion(StringMatchingExpression.StringMatchingOperation operation, Object string, boolean target) {
        this.operation = operation;
        this.string = string;
        this.target = target;
    }

    public StringMatchingExpression.StringMatchingOperation getOperation() {
        return operation;
    }

    public void setOperation(StringMatchingExpression.StringMatchingOperation operation) {
        this.operation = operation;
    }

    public Object getString() {
        return string;
    }

    public void setString(Object string) {
        this.string = string;
    }

    public boolean isTarget() {
        return target;
    }

    public void setTarget(boolean target) {
        this.target = target;
    }

    @Override
    public boolean check(Object value) {
        // Handle null values as unknown
        if (value == null || this.string == null) {
            return true;
        }

        // Ensure both are of type String
        if (!(value instanceof String) || !(this.string instanceof String)) {
            return false;
        }

        boolean result = false;
        String stringValue = (String) this.string;

        switch (operation) {
            case CONTAINS:
                result = ((String)value).contains(stringValue);
                break;
            case STARTS_WITH:
                result = ((String)value).startsWith(stringValue);
                break;
            case ENDS_WITH:
                result = ((String)value).endsWith(stringValue);
                break;
        }

        // If target is true, return result; otherwise, return its negation
        return target ? result : !result;
    }
}
