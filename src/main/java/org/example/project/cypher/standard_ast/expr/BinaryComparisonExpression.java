package org.example.project.cypher.standard_ast.expr;

import org.example.project.Randomly;
import org.example.project.cypher.ast.IExpression;

import java.util.Map;

public class BinaryComparisonExpression extends CypherExpression {

    public enum BinaryComparisonOperation {
        SMALLER("<"),
        EQUAL("="),
        SMALLER_OR_EQUAL("<="),
        HIGHER(">"),
        HIGHER_OR_EQUAL(">="),
        NOT_EQUAL("<>");

        BinaryComparisonOperation(String textRepresentation) {
            this.TextRepresentation = textRepresentation;
        }

        private final String TextRepresentation;

        public String getTextRepresentation() {
            return this.TextRepresentation;
        }

        public BinaryComparisonOperation reverse() {
            switch (this) {
                case EQUAL:
                    return NOT_EQUAL;
                case NOT_EQUAL:
                    return EQUAL;
                case HIGHER:
                    return SMALLER_OR_EQUAL;
                case HIGHER_OR_EQUAL:
                    return SMALLER;
                case SMALLER:
                    return HIGHER_OR_EQUAL;
                case SMALLER_OR_EQUAL:
                    return HIGHER;
                default:
                    throw new RuntimeException();
            }
        }
    }

    private final IExpression left;
    private final IExpression right;
    private BinaryComparisonOperation op;

    public BinaryComparisonExpression(IExpression left, IExpression right, BinaryComparisonOperation op) {
        left.setParentExpression(this);
        right.setParentExpression(this);
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public IExpression getLeftExpression() {
        return left;
    }

    public IExpression getRightExpression() {
        return right;
    }

    public BinaryComparisonOperation getOperation() {
        return op;
    }

    public void setOperation(BinaryComparisonOperation op) {
        this.op = op;
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        if (originalExpression == left) {
            newExpression.setParentExpression(this);
        } else if (originalExpression == right) {
            newExpression.setParentExpression(this);
        } else {
            throw new RuntimeException("No matching child to replace.");
        }
    }

    @Override
    public Object getValue() {
        Object leftValue = left.getValue();
        Object rightValue = right.getValue();

        if (leftValue == null || rightValue == null) {
            return null; // Unknown value
        }

        if (leftValue instanceof String) {
            return compareStrings((String) leftValue, (String) rightValue);
        }

        if (leftValue instanceof Number) {
            return compareNumbers((Number) leftValue, (Number) rightValue);
        }

        throw new RuntimeException("Unsupported value types for comparison.");
    }

    private Boolean compareStrings(String left, String right) {
        switch (op) {
            case SMALLER:
                return left.compareTo(right) < 0;
            case SMALLER_OR_EQUAL:
                return left.compareTo(right) <= 0;
            case HIGHER:
                return left.compareTo(right) > 0;
            case NOT_EQUAL:
                return !left.equals(right);
            case EQUAL:
                return left.equals(right);
            case HIGHER_OR_EQUAL:
                return left.compareTo(right) >= 0;
            default:
                throw new RuntimeException("Invalid comparison operation.");
        }
    }

    private Boolean compareNumbers(Number left, Number right) {
        double leftValue = left.doubleValue();
        double rightValue = right.doubleValue();
        switch (op) {
            case SMALLER:
                return leftValue < rightValue;
            case SMALLER_OR_EQUAL:
                return leftValue <= rightValue;
            case HIGHER:
                return leftValue > rightValue;
            case NOT_EQUAL:
                return leftValue != rightValue;
            case EQUAL:
                return leftValue == rightValue;
            case HIGHER_OR_EQUAL:
                return leftValue >= rightValue;
            default:
                throw new RuntimeException("Invalid comparison operation.");
        }
    }

    public static BinaryComparisonOperation randomOperation() {
        Randomly randomly = new Randomly();
        int operationNum = randomly.getInteger(0, 100);
        if (operationNum < 5) {
            return BinaryComparisonOperation.EQUAL;
        }
        if (operationNum < 20) {
            return BinaryComparisonOperation.NOT_EQUAL;
        }
        if (operationNum < 40) {
            return BinaryComparisonOperation.HIGHER;
        }
        if (operationNum < 60) {
            return BinaryComparisonOperation.HIGHER_OR_EQUAL;
        }
        if (operationNum < 80) {
            return BinaryComparisonOperation.SMALLER;
        }
        return BinaryComparisonOperation.SMALLER_OR_EQUAL;
    }

    public static BinaryComparisonExpression randomComparison(IExpression left, IExpression right) {
        BinaryComparisonOperation operation = randomOperation();
        return new BinaryComparisonExpression(left, right, operation);
    }

    @Override
    public String toCypher() {
        StringBuilder sb=new StringBuilder();
        sb.append("(");
        sb.append(left.toCypher());
        sb.append(" ").append(op.getTextRepresentation()).append(" ");
        sb.append(right.toCypher());
        sb.append(")");
        return sb.toString();
    }
}
