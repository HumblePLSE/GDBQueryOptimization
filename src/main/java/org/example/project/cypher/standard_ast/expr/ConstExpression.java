package org.example.project.cypher.standard_ast.expr;

import org.example.project.cypher.ast.IExpression;

public class ConstExpression extends CypherExpression {
    private Object value;

    public ConstExpression(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String toCypher() {
        if (value == null) {
            return "null"; // Cypher 中的 null
        } else if (value instanceof String) {
            return "'" + value + "'"; // 字符串用单引号括起来
        } else if (value instanceof Boolean) {
            return value.toString(); // 布尔值直接返回 true 或 false
        } else if (value instanceof Number) {
            return value.toString(); // 数字直接返回其字符串形式
        } else {
            throw new UnsupportedOperationException("Unsupported value type: " + value.getClass());
        }
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        throw new RuntimeException("ConstExpression does not support child replacement.");
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
