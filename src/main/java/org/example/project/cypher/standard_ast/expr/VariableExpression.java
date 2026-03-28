package org.example.project.cypher.standard_ast.expr;

import org.example.project.cypher.ast.IExpression;

public class VariableExpression extends CypherExpression {
    private final String key; // 属性键
    private final Object value; // 属性值

    public VariableExpression(String key, Object value) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty.");
        }
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();
        sb.append(key); // 添加属性键
        return sb.toString();
    }

    @Override
    public void replaceChild(IExpression originalExpression, IExpression newExpression) {
        throw new RuntimeException("VariableExpression does not support child replacement.");
    }
}
