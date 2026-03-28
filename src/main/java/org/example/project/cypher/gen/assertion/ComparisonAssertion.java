package org.example.project.cypher.gen.assertion;

import org.example.project.cypher.standard_ast.expr.BinaryComparisonExpression;
import org.example.project.cypher.standard_ast.expr.ExprUnknownVal;

public class ComparisonAssertion implements ExpressionAssertion {
    private BinaryComparisonExpression.BinaryComparisonOperation operation;
    private Object leftOp;
    private boolean target;

    public ComparisonAssertion(BinaryComparisonExpression.BinaryComparisonOperation operation, Object leftOp, boolean target) {
        this.leftOp = leftOp;
        this.operation = operation;
        this.target = target;
    }

    public BinaryComparisonExpression.BinaryComparisonOperation getOperation() {
        return operation;
    }

    public void setOperation(BinaryComparisonExpression.BinaryComparisonOperation operation) {
        this.operation = operation;
    }

    public Object getLeftOp() {
        return leftOp;
    }

    public void setLeftOp(Object leftOp) {
        this.leftOp = leftOp;
    }

    public boolean trueTarget() {
        return target;
    }

    public void setTarget(boolean target) {
        this.target = target;
    }

    @Override
    public boolean check(Object value) {
        // Handle null values as unknown
        if (value == null || leftOp == null) {
            return true;
        }

        if (value == ExprUnknownVal.UNKNOWN_INTEGER || leftOp == ExprUnknownVal.UNKNOWN_INTEGER) {
            return true;
        }

        BinaryComparisonExpression.BinaryComparisonOperation effectiveOperation = trueTarget() ? operation : operation.reverse();

        // Compare numbers
        if (value instanceof Number && leftOp instanceof Number) {
            double leftValue = ((Number) leftOp).doubleValue();
            double rightValue = ((Number) value).doubleValue();

            switch (effectiveOperation) {
                case EQUAL:
                    return leftValue == rightValue;
                case HIGHER:
                    return leftValue > rightValue;
                case HIGHER_OR_EQUAL:
                    return leftValue >= rightValue;
                case SMALLER:
                    return leftValue < rightValue;
                case SMALLER_OR_EQUAL:
                    return leftValue <= rightValue;
                case NOT_EQUAL:
                    return leftValue != rightValue;
            }
        }

        // Compare strings
        if (value instanceof String && leftOp instanceof String) {
            String leftValue = (String) leftOp;
            String rightValue = (String) value;

            switch (effectiveOperation) {
                case EQUAL:
                    return leftValue.compareTo(rightValue) == 0;
                case HIGHER:
                    return leftValue.compareTo(rightValue) > 0;
                case HIGHER_OR_EQUAL:
                    return leftValue.compareTo(rightValue) >= 0;
                case SMALLER:
                    return leftValue.compareTo(rightValue) < 0;
                case SMALLER_OR_EQUAL:
                    return leftValue.compareTo(rightValue) <= 0;
                case NOT_EQUAL:
                    return leftValue.compareTo(rightValue) != 0;
            }
        }

        // Unsupported types
        return false;
    }
}
