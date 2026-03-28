package org.example.project.cypher.schema;

import org.example.project.cypher.standard_ast.CypherType;

public interface IPropertyInfo {
    /**
     * 属性名
     * @return
     */
    String getKey();

    /**
     * 属性的类型
     * @return
     */
    CypherType getType();

    /**
     * 是否label/type下的每一个node/relation都具有该属性
     * @return
     */
    boolean isOptional();
}
