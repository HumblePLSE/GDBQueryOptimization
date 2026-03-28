package org.example.project.cypher.standard_ast.expr;

import org.example.project.Randomly;
import org.example.project.cypher.ast.IExpression;
import java.util.Map;

public class BinaryLogicalExpression extends CypherExpression {

    public enum BinaryLogicalOperation {
        OR("OR"),
        AND("AND"),
        XOR("XOR");

        private final String textRepresentation;

        BinaryLogicalOperation(String textRepresentation) {
            this.textRepresentation = textRepresentation;
        }

        public String getTextRepresentation() {
            return this.textRepresentation;
        }
    }

    private IExpression left;
    private IExpression right;
    private final BinaryLogicalOperation op;

    public BinaryLogicalExpression(IExpression left, IExpression right, BinaryLogicalOperation op) {
        this.left = left;
        this.right = right;
        this.op = op;
        if (left != null) {
            left.setParentExpression(this);
        }
        if (right != null) {
            right.setParentExpression(this);
        }
    }

    public IExpression getLeftExpression() {
        return left;
    }

    public IExpression getRightExpression() {
        return right;
    }

    public BinaryLogicalOperation getOperation() {
        return op;
    }

    public static BinaryLogicalOperation randomOp() {
        Randomly randomly = new Randomly();
        int operationNum = randomly.getInteger(0, 90);
        if (operationNum < 30) {
            return BinaryLogicalOperation.AND;
        } else if (operationNum < 60) {
            return BinaryLogicalOperation.OR;
        } else {
            return BinaryLogicalOperation.XOR;
        }
    }

    public static BinaryLogicalExpression randomLogical(IExpression left, IExpression right) {
        return new BinaryLogicalExpression(left, right, randomOp());
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
            throw new RuntimeException("Expression to replace not found");
        }
    }

    @Override
    public Object getValue() {
        Object leftValue = (left != null) ? left.getValue() : null;
        Object rightValue = (right != null) ? right.getValue() : null;

        if (leftValue == null || rightValue == null) {
            return null;
        }

        boolean leftBool = (Boolean) leftValue;
        boolean rightBool = (Boolean) rightValue;

        switch (op) {
            case AND:
                return leftBool && rightBool;
            case OR:
                return leftBool || rightBool;
            case XOR:
                return leftBool ^ rightBool;
            default:
                throw new RuntimeException("Unsupported logical operation");
        }
    }

    @Override
    public String toCypher() {
        StringBuilder sb=new StringBuilder();
        sb.append("(");
        if (left != null) {
            sb.append(left.toCypher());
        }
        sb.append(" ").append(op.getTextRepresentation()).append(" ");
        if (right != null) {
            sb.append(right.toCypher());
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BinaryLogicalExpression)) {
            return false;
        }
        BinaryLogicalExpression other = (BinaryLogicalExpression) obj;
        return left.equals(other.left) && right.equals(other.right) && op == other.op;
    }
}
