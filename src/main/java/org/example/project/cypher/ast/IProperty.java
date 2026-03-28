package org.example.project.cypher.ast;

public interface IProperty {
    ICypherType getType();
    IExpression getVal();
    String getKey();
    void setKey(String key);

}
