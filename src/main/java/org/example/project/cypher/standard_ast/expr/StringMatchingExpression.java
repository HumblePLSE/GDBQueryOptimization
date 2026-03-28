package org.example.project.cypher.standard_ast.expr;

import org.example.project.Randomly;
import org.example.project.cypher.ast.IExpression;

import java.util.Map;

public class StringMatchingExpression extends CypherExpression {
    private IExpression source;
    private IExpression pattern;
    private StringMatchingOperation op;

    public StringMatchingExpression(IExpression source, IExpression pattern, StringMatchingOperation op) {
        this.source = source;
        this.pattern = pattern;
        this.op = op;
        source.setParentExpression(this);
        pattern.setParentExpression(this);
    }

    public IExpression getSource() {
        return source;
    }

    public void setSource(IExpression source) {
        this.source = source;
        source.setParentExpression(this);
    }

    public IExpression getPattern() {
        return pattern;
    }

    public void setPattern(IExpression pattern) {
        this.pattern = pattern;
        pattern.setParentExpression(this);
    }

    public StringMatchingOperation getOp() {
        return op;
    }

    public void setOp(StringMatchingOperation op) {
        this.op = op;
    }

    public enum StringMatchingOperation {
        CONTAINS("CONTAINS"),
        STARTS_WITH("STARTS WITH"),
        ENDS_WITH("ENDS WITH");

        private final String textRepresentation;

        StringMatchingOperation(String textRepresentation) {
            this.textRepresentation = textRepresentation;
        }

        public String getTextRepresentation() {
            return this.textRepresentation;
        }
    }

    public static StringMatchingOperation randomOperation() {
        Randomly randomly = new Randomly();
        int operationNum = randomly.getInteger(0, 90);
        if (operationNum < 30) {
            return StringMatchingOperation.CONTAINS;
        } else if (operationNum < 60) {
            return StringMatchingOperation.ENDS_WITH;
        } else {
            return StringMatchingOperation.STARTS_WITH;
        }
    }

    public static StringMatchingExpression randomMatching(IExpression source, IExpression pattern) {
        return new StringMatchingExpression(source, pattern, randomOperation());
    }

    @Override
    public String toCypher() {
        StringBuilder sb=new StringBuilder();
        sb.append("(");
        sb.append(source.toCypher());
        sb.append(" ").append(op.getTextRepresentation()).append(" ");
        sb.append(pattern.toCypher());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        if (originalExpression == source) {
            setSource(newExpression);
        } else if (originalExpression == pattern) {
            setPattern(newExpression);
        } else {
            throw new RuntimeException("Child expression not found for replacement.");
        }
    }

    @Override
    public Object getValue() {
        Object sourceValue = source.getValue();
        Object patternValue = pattern.getValue();

        if (sourceValue == null || patternValue == null) {
            return null; // Null represents unknown value
        }

        if (!(sourceValue instanceof String) || !(patternValue instanceof String)) {
            throw new RuntimeException("Invalid types for string matching operation.");
        }

        String sourceString = (String) sourceValue;
        String patternString = (String) patternValue;

        switch (op) {
            case CONTAINS:
                return sourceString.contains(patternString);
            case STARTS_WITH:
                return sourceString.startsWith(patternString);
            case ENDS_WITH:
                return sourceString.endsWith(patternString);
            default:
                throw new RuntimeException("Unknown string matching operation.");
        }
    }
}
