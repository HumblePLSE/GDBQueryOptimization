package org.example.project.cypher.standard_ast;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.example.project.cypher.gen.GraphManager;
import org.example.project.cypher.gen.AbstractNode;
import org.example.project.Randomly;
import org.example.project.cypher.gen.NodeVariableManager;
import org.example.project.cypher.schema.ILabelInfo;
import org.example.project.cypher.schema.IPropertyInfo;
import org.example.project.cypher.schema.IRelationTypeInfo;

/**
 * Represents a node pattern in Cypher queries.
 * nodePattern ::= (nodeLabels {nodeProperties})
 */
public class NodePatternClause extends Clause {

    private final String nodeVariable;       // 节点变量名
    private final List<ILabelInfo> nodeLabels;       // 节点标签
    private final Map<String, Object> SelectProperties; // 节点属性
    private final WhereClause whereClause;       // WHERE 子句（可选）

    public NodePatternClause(String nodeVariable, List<ILabelInfo> nodeLabels, Map<String, Object> SelectProperties, WhereClause whereClause) {
        super("NodePattern");
        this.nodeVariable = nodeVariable;
        this.nodeLabels = nodeLabels;
        this.SelectProperties=SelectProperties;
        this.whereClause = whereClause;
    }

    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");

        // 添加节点变量
        if (nodeVariable != null) {
            sb.append(nodeVariable); // 节点变量
        }

        // 添加节点标签
        if (nodeLabels != null && !nodeLabels.isEmpty()) {
            sb.append(":");
            sb.append(nodeLabels.stream()
                    .map(ILabelInfo::getName) // 获取标签名称
                    .collect(Collectors.joining(":")));
        }

        // 添加节点属性
        if (SelectProperties != null && !SelectProperties.isEmpty()) {
            sb.append(" {");
            boolean first = true;
            for (Map.Entry<String, Object> entry : SelectProperties.entrySet()) {
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

        sb.append(")");

        // 添加 WHERE 子句
        if (whereClause != null) {
            sb.append(whereClause.toCypher());
        }

        return sb.toString();
    }


    @Override
    public boolean validate() {
        // 验证节点变量是否有效（如果存在）
        if (nodeVariable != null && nodeVariable.isEmpty()) {
            return false;
        }

        // 验证节点标签是否有效
        if (nodeLabels != null && !nodeLabels.isEmpty()) {
            for (ILabelInfo label : nodeLabels) {
                if (label == null || label.getName() == null || label.getName().isEmpty()) {
                    return false;
                }
            }
        }

        // 验证节点属性是否有效
        if (SelectProperties != null && !SelectProperties.isEmpty()) {
            for (Map.Entry<String, Object> entry : SelectProperties.entrySet()) {
                if (entry.getKey() == null || entry.getKey().isEmpty() || entry.getValue() == null) {
                    return false;
                }
            }
        }


        return true;
    }


    // 生成一个随机 NodePatternClause
    public static NodePatternClause generateRandomNodePattern(AbstractNode node, GraphManager graphManager) {
        NodeVariableManager nodeVariableManager = graphManager.getNodeVariableManager();
        Randomly randomly = new Randomly();
        String nodeVariable = null;

        // 获取节点的标签信息并随机选择若干个标签
        List<ILabelInfo> labels = node.getLabelInfos();
        Collections.shuffle(labels); // 随机打乱标签顺序
        int labelCount=0;
        if(labels.size()!=0&&randomly.getInteger(0,3)==0) labelCount = randomly.getInteger(1, labels.size() + 1); // 随机选择 0 到 labels.size() 个标签
        List<ILabelInfo> selectedLabels = new ArrayList<>(labels.subList(0, labelCount)); // 随机截取部分标签

        // 获取节点的少量部分属性
        Map<String, Object> propertyClauses = new HashMap<>(node.getSmallNumProperties());

        // 随机决定是否给该节点分配变量名
        if (randomly.getBoolean()) {
            nodeVariable = nodeVariableManager.generateNodeVariable(node);
        }

        WhereClause whereClause = null;


        return new NodePatternClause(nodeVariable, selectedLabels, propertyClauses, whereClause);
    }


    public static NodePatternClause generateFullNodePattern(AbstractNode node, GraphManager graphManager) {
        NodeVariableManager nodeVariableManager = graphManager.getNodeVariableManager();
        Randomly randomly = new Randomly();

        // 获取节点的所有标签信息
        List<ILabelInfo> allLabels = node.getLabelInfos();

        // 获取节点的所有属性
        Map<String, Object> allProperties = new HashMap<>(node.getProperties());

        // 为该节点生成变量名（20% 概率）
        String nodeVariable = null;
        if (randomly.getInteger(0, 4) >= 3) {
            nodeVariable = nodeVariableManager.generateNodeVariable(node);
        }


        return new NodePatternClause(nodeVariable, allLabels, allProperties, null);
    }


    public static NodePatternClause generateVariablePattern(String nodeVariable){
        return new NodePatternClause(nodeVariable,null,null,null);
    }




}
