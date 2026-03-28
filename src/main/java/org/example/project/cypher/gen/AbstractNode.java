package org.example.project.cypher.gen;

import org.example.project.Randomly;
import org.example.project.cypher.ast.ILabel;
import org.example.project.cypher.ast.IProperty;
import org.example.project.cypher.schema.ILabelInfo;
import org.example.project.cypher.standard_ast.Label;

import java.util.*;
import java.util.stream.Collectors;

public class AbstractNode {
    private List<ILabelInfo> labelInfos = new ArrayList<>();
    private int id;

    private Map<String, Object> properties = new HashMap<>();

    private List<AbstractRelationship> relationships = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;

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

    public void removeProperty(String key) {
        properties.remove(key);
    }

    public List<ILabelInfo> getLabelInfos() {
        return labelInfos;
    }

    public void removeLabels(List<String> labelNames) {
        labelInfos.removeIf(labelInfo -> labelNames.contains(labelInfo.getName()));
    }


    public List<ILabel> getLabels() {
        return new ArrayList<>(labelInfos.stream().map(l -> new Label(l.getName())).collect(Collectors.toList()));
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Map<String, Object> getSmallNumProperties() {
        Randomly randomly = new Randomly();
        int numChoice = randomly.getInteger(0, 10);
        // 随机选择数量（最多2个）
        int num;
        if (numChoice < 5) num = 0;
        else if (numChoice < 9) num = 1;
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


    public void setLabelInfos(List<ILabelInfo> labelInfos) {
        this.labelInfos = labelInfos;
    }

    public void addRelationship(AbstractRelationship relationship) {
        this.relationships.add(relationship);
    }

    public List<AbstractRelationship> getRelationships() {
        return relationships;
    }

    public List<AbstractRelationship> getRelationshipsfrom() {
        return relationships.stream()
                .filter(r -> r.getFrom().equals(this)) // Filter relationships where this node is the "from" node
                .collect(Collectors.toList());
    }

    public List<AbstractRelationship> getRelationshipsto() {
        return relationships.stream()
                .filter(r -> r.getTo().equals(this)) // Filter relationships where this node is the "to" node
                .collect(Collectors.toList());
    }

    public boolean hasRelationship() {
        return !relationships.isEmpty();
    }

    public enum NodeFunction {
        LABELS("labels", List.class), // 获取节点的所有标签
        COUNT("count", Integer.class), // 统计节点的数量
        MAX("max", AbstractNode.class), // 获取具有最大属性值的节点
        MIN("min", AbstractNode.class), // 获取具有最小属性值的节点
        ID("id", Integer.class); // 返回节点的唯一 ID

        private final String functionName;
        private final Class<?> returnType;

        NodeFunction(String functionName, Class<?> returnType) {
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
