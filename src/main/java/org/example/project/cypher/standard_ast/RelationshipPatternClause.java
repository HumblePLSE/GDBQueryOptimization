package org.example.project.cypher.standard_ast;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.example.project.cypher.gen.*;
import org.example.project.Randomly;
import org.example.project.cypher.schema.IRelationTypeInfo;

/**
 * Represents a relationship pattern in Cypher queries.
 * relationshipPattern ::= [<direction>] relationshipType {relationshipProperties}
 */
public class RelationshipPatternClause extends Clause {

    private final String relationshipVariable; // 关系变量名
    private final String direction;           // 关系的方向: "FROM", "TO", "ANY", or "NONE"
    private final IRelationTypeInfo relationshipType; // 关系类型
    private final Map<String, Object> properties;     // 关系属性

    public RelationshipPatternClause(String relationshipVariable, String direction, IRelationTypeInfo relationshipType, Map<String, Object> properties) {
        super("RelationshipPattern");
        this.relationshipVariable = relationshipVariable;
        this.direction = direction;
        this.relationshipType = relationshipType;
        this.properties = properties;
    }

    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();
        if ("left".equals(direction)) {
            sb.append("<-[");
        } else {
            sb.append("-[");
        }

        if (relationshipVariable != null) {
            sb.append(relationshipVariable);
        }
        if (relationshipType != null) {
            sb.append(":").append(relationshipType.getName());
        }

        // 如果有属性，将属性添加到关系类型内部
        if (properties != null && !properties.isEmpty()) {
            sb.append(" {");
            boolean first = true;
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                sb.append(entry.getKey()).append(": ");
                Object value = entry.getValue();
                if (value instanceof String) {
                    sb.append("\"").append(value).append("\"");
                } else if (value instanceof Number || value instanceof Boolean) {
                    sb.append(value);
                } else if(value instanceof LocalDate){
                    sb.append("date('").append(value).append("')");
                } else if (value instanceof List) { // 新增列表类型处理
                    sb.append("[");
                    boolean isFirstElement = true;
                    for (Object element : (List<?>) value) {
                        if (!isFirstElement) {
                            sb.append(", ");
                        }
                        isFirstElement = false;

                        // 处理列表元素类型
                        if (element instanceof String) {
                            sb.append("\"").append(element).append("\"");
                        } else if (element instanceof Number) {
                            sb.append(element);
                        } else if (element instanceof Boolean) {
                            sb.append(element.toString().toLowerCase()); // true/false 小写
                        } else if (element instanceof LocalDate) {
                            sb.append("date('").append(element).append("')");
                        } else {
                            sb.append("\"").append(element).append("\""); // 其他类型转字符串
                        }
                    }
                    sb.append("]");
                }
                else {
                    sb.append("\"").append(value).append("\""); // 默认作为字符串处理
                }
            }
            sb.append("}");
        }

        sb.append("]");

        // 根据方向添加箭头
        if ("right".equals(direction)) {
            sb.append("->");
        } else {
            sb.append("-");
        }

        return sb.toString();
    }

    @Override
    public boolean validate() {
        return relationshipType != null && properties != null;
    }

    // 生成一个随机 RelationshipPatternClause
    public static RelationshipPatternClause generateRandomRelationshipPattern(AbstractRelationship relationship, String direction, GraphManager graphManager) {
        Randomly randomly = new Randomly();



        // 随机生成关系变量
        String relationshipVariable = null;
        if (randomly.getBoolean()) {
            RelationshipVariableManager relationshipVariableManager = graphManager.getRelationshipVariableManager();
            relationshipVariable = relationshipVariableManager.generateRelationshipVariable(relationship);
        }

        // 获取关系类型
        IRelationTypeInfo relationType = relationship.getType();
        if(randomly.getBoolean()) relationType=null;

        // 随机生成属性
        Map<String, Object> propertyClauses = new HashMap<>(relationship.getSmallNumProperties());


        return new RelationshipPatternClause(relationshipVariable, direction, relationType, propertyClauses);
    }

    public static RelationshipPatternClause generateFullRelationshipPattern(AbstractRelationship relationship, String direction, GraphManager graphManager) {
        // 获取 RelationshipVariableManager
        RelationshipVariableManager relationshipVariableManager = graphManager.getRelationshipVariableManager();

        // 随机生成关系变量
        String relationshipVariable = null;
        Randomly randomly = new Randomly();
        if (randomly.getBoolean()) {
            relationshipVariable = relationshipVariableManager.generateRelationshipVariable(relationship);
        }

        // 获取关系类型
        IRelationTypeInfo relationType = relationship.getType();

        // 获取所有属性
        Map<String, Object> propertyClauses = new HashMap<>();
        relationship.getProperties().forEach((key, value) -> propertyClauses.put(key, value));

        // 返回完整的 RelationshipPatternClause
        return new RelationshipPatternClause(relationshipVariable, direction, relationType, propertyClauses);
    }
}
