package org.example.project.cypher.standard_ast.expr;

import org.example.project.Randomly;
import org.example.project.cypher.ast.IExpression;

import java.util.Map;

public class SingleLogicalExpression extends CypherExpression {

    private IExpression child;
    private final SingleLogicalOperation op;

    public SingleLogicalExpression(IExpression child, SingleLogicalOperation op) {
        this.child = child;
        this.op = op;
        child.setParentExpression(this);
    }

    public IExpression getChildExpression() {
        return child;
    }

    public SingleLogicalOperation getOperation() {
        return op;
    }

    public static SingleLogicalOperation randomOperation() {
        Randomly randomly = new Randomly();
        int operationNum = randomly.getInteger(0, 100);
        if (operationNum < 40) {
            return SingleLogicalOperation.NOT;
        } else if (operationNum < 70) {
            return SingleLogicalOperation.IS_NULL;
        } else {
            return SingleLogicalOperation.IS_NOT_NULL;
        }
    }

    public static SingleLogicalExpression randomLogical(IExpression expr) {
        SingleLogicalOperation operation = randomOperation();
        return new SingleLogicalExpression(expr, operation);
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        if (originalExpression == child) {
            this.child = newExpression;
            newExpression.setParentExpression(this);
        } else {
            throw new IllegalArgumentException("Child expression not found for replacement.");
        }
    }

    @Override
    public Object getValue() {
        Object childValue = child.getValue();
        if (childValue == null) {
            return null;
        }

        switch (op) {
            case NOT:
                return !(Boolean) childValue;
            case IS_NULL:
                return childValue == null;
            case IS_NOT_NULL:
                return childValue != null;
            default:
                throw new UnsupportedOperationException("Unsupported operation: " + op);
        }
    }

    @Override
    public String toCypher() {
        StringBuilder sb=new StringBuilder();
        sb.append("(");
        if (op == SingleLogicalOperation.NOT) {
            sb.append(op.getTextRepresentation()).append(" ");
        }
        sb.append(child.toCypher());
        if (op != SingleLogicalOperation.NOT) {
            sb.append(" ").append(op.getTextRepresentation());
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SingleLogicalExpression)) {
            return false;
        }
        SingleLogicalExpression other = (SingleLogicalExpression) o;
        return child.equals(other.child) && op == other.op;
    }

    public enum SingleLogicalOperation {
        IS_NULL("IS NULL"),
        IS_NOT_NULL("IS NOT NULL"),
        NOT("NOT");

        private final String textRepresentation;

        SingleLogicalOperation(String textRepresentation) {
            this.textRepresentation = textRepresentation;
        }

        public String getTextRepresentation() {
            return this.textRepresentation;
        }
    }
}
