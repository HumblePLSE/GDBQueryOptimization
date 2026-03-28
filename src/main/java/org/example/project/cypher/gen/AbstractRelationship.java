package org.example.project.cypher.gen;

import org.example.project.Randomly;
import org.example.project.cypher.schema.IRelationTypeInfo;

import java.util.*;

public class AbstractRelationship {
    private IRelationTypeInfo type = null;
    private AbstractNode from;
    private AbstractNode to;

    private int id;
    private Map<String, Object> properties = new HashMap<>();

    public IRelationTypeInfo getType() {
        return type;
    }

    public void setType(IRelationTypeInfo type) {
        this.type = type;
    }

    public AbstractNode getFrom() {
        return from;
    }

    public void setFrom(AbstractNode from) {
        this.from = from;
    }

    public AbstractNode getTo() {
        return to;
    }

    public void setTo(AbstractNode to) {
        this.to = to;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Map<String, Object> getSmallNumProperties() {
        Randomly randomly = new Randomly();
         // 随机选择数量（最多2个）
        int numChoice = randomly.getInteger(0, 10);
        // 随机选择数量（最多2个）
        int num;
        if (numChoice < 5) num = 0;
        else if (numChoice < 8) num = 1;
        else num = 2;

        List<String> keys = new ArrayList<>(properties.keySet()); // 获取所有键
        Collections.shuffle(keys); // 随机打乱键的顺序

        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < num; i++) {
            String key = keys.get(i); // 按随机顺序选择键
            result.put(key, properties.get(key)); // 添加到结果中
        }

        return result; // 返回随机选择的键值对
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setProperty(String key, Object value) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        // 更新或插入新属性
        properties.put(key, value);
    }

    public void removeProperty(String key)
    {
        properties.remove(key);
    }

    public enum RelationshipFunction {
        TYPE("type", String.class), // 获取关系的类型
        START_NODE("startNode", AbstractNode.class), // 获取关系的起始节点
        END_NODE("endNode", AbstractNode.class), // 获取关系的终止节点
        COUNT("count", Integer.class), // 统计关系的数量
        MAX("max", AbstractRelationship.class), // 计算某个关系属性的最大值
        MIN("min", AbstractRelationship.class), // 计算某个关系属性的最小值
        ID("id", Integer.class); // 返回关系的唯一 ID

        private final String functionName;
        private final Class<?> returnType;

        RelationshipFunction(String functionName, Class<?> returnType) {
            this.functionName = functionName;
            this.returnType = returnType;
        }

        public String getFunctionName() {
            return functionName;
        }

        public Class<?> getReturnType() {
            return returnType;
        }
    }


}
