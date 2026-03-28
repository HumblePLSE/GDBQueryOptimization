package org.example.project.cypher.standard_ast;

import org.example.project.Randomly;
import org.example.project.cypher.gen.AbstractNode;
import org.example.project.cypher.gen.GraphManager;
import org.example.project.cypher.gen.NodeVariableManager;
import org.example.project.cypher.gen.PathVariableManager;
import org.example.project.cypher.schema.CypherSchema;
import org.example.project.cypher.standard_ast.expr.ExprUnknownVal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuantifiedPathPatternClause {
    private final NodePatternClause startNodePattern;

    private final NodePatternClause endNodePattern;
    private int direction;  // 方向：0 = 双向, 1 = <-, 2 = ->
    private String relationshipVariable;  // 关系变量名
    private List<String> typeIdentifiers;  // 关系类型集合
    private LengthQuantifier lengthQuantifier;  // 长度量化
    private String pathVariable;//路径变量
    private String shortestPath;

    public static class LengthQuantifier {
        private Integer fixedLength;  // 定长长度
        private Integer lowerBound;  // 下界
        private Integer upperBound;  // 上界

        // 构造函数：定长
        public LengthQuantifier(int fixedLength) {
            this.fixedLength = fixedLength;
            this.lowerBound = null;
            this.upperBound = null;
        }

        // 构造函数：范围
        public LengthQuantifier(Integer lowerBound, Integer upperBound) {
            this.fixedLength = null;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;

            if (this.lowerBound == null && this.upperBound == null) {
                throw new IllegalArgumentException("LengthQuantifier: lowerBound and upperBound cannot both be null");
            }
        }

        public boolean isFixedLength() {
            return fixedLength != null;
        }

        public Integer getFixedLength() {
            return fixedLength;
        }

        public Integer getLowerBound() {
            return lowerBound;
        }

        public Integer getUpperBound() {
            return upperBound;
        }

        public String toCypher() {
            if (isFixedLength()) {
                return "*" + fixedLength;
            }
            StringBuilder sb = new StringBuilder("*");
            if (lowerBound != null) {
                sb.append(lowerBound);
            }
            sb.append("..");
            if (upperBound != null) {
                sb.append(upperBound);
            }
            return sb.toString();
        }
    }

    public QuantifiedPathPatternClause(NodePatternClause startNodePattern,NodePatternClause endNodePattern, int direction, String relationshipVariable, List<String> typeIdentifiers, LengthQuantifier lengthQuantifier, String pathVariable,String shortestPath) {
        this.startNodePattern=startNodePattern;
        this.endNodePattern=endNodePattern;
        this.direction = direction;
        this.relationshipVariable = relationshipVariable;
        this.typeIdentifiers = typeIdentifiers;
        this.lengthQuantifier = lengthQuantifier;
        this.pathVariable = pathVariable;
        this.shortestPath = shortestPath;
    }

    public String getRelationshipVariable() {
        return relationshipVariable;
    }

    public List<String> getTypeIdentifiers() {
        return typeIdentifiers;
    }

    public LengthQuantifier getLengthQuantifier() {
        return lengthQuantifier;
    }

    public String toCypher() {
        StringBuilder sb = new StringBuilder();

        if (pathVariable != null) {
            sb.append(pathVariable).append(" = ");
        }
        if(shortestPath != null) {
            sb.append(shortestPath).append("(");
        }
        sb.append(startNodePattern.toCypher()).append(" ");
        // 添加方向
        if (direction == 1) {
            sb.append("<-");
        } else {
            sb.append("-");
        }

        // 添加关系变量和类型
        sb.append("[");
        if (relationshipVariable != null) {
            sb.append(relationshipVariable);
        }
        if (!typeIdentifiers.isEmpty()) {
            sb.append(":");
            sb.append(String.join("|", typeIdentifiers));
        }

        // 添加长度量化
        if (lengthQuantifier != null) {
            sb.append(lengthQuantifier.toCypher());
        }

        sb.append("]");

        // 添加方向
        if (direction == 2) {
            sb.append("->");
        } else {
            sb.append("-");
        }
        sb.append(endNodePattern.toCypher()).append(" ");
        if(shortestPath != null) {
            sb.append(")");
        }

        return sb.toString();
    }

    public static QuantifiedPathPatternClause generateRandomVarLengthRelationship(GraphManager graphManager) {
        Randomly randomly = new Randomly();

        // 随机选择起始节点,终止节点
        AbstractNode startNode,endNode;

        // 获取 GraphManager 中的 NodeVariableManager
        NodeVariableManager nodeVariableManager = graphManager.getNodeVariableManager();

        NodePatternClause startNodePattern,endNodePattern;
        // 50% 概率从 NodeVariableManager 中获取随机节点变量
        if (randomly.getBoolean() && !nodeVariableManager.getNodeVariableMap().isEmpty()) {
            // 从变量管理器中随机选择一个节点变量
            List<String> variableKeys = new ArrayList<>(nodeVariableManager.getNodeVariableMap().keySet());
            String randomVariable = variableKeys.get(randomly.getInteger(0, variableKeys.size()));
            startNode = nodeVariableManager.getNodeVariable(randomVariable);
            startNodePattern = NodePatternClause.generateVariablePattern(randomVariable);
        } else {
            // 否则从现有图中的节点中随机选择
            startNode = graphManager.getNodes().get(randomly.getInteger(0, graphManager.getNodeNumber()));
            startNodePattern = NodePatternClause.generateRandomNodePattern(startNode, graphManager);
        }

        // 50% 概率从 NodeVariableManager 中获取随机节点变量
        if (randomly.getBoolean() && !nodeVariableManager.getNodeVariableMap().isEmpty()) {
            // 从变量管理器中随机选择一个节点变量
            List<String> variableKeys = new ArrayList<>(nodeVariableManager.getNodeVariableMap().keySet());
            String randomVariable = variableKeys.get(randomly.getInteger(0, variableKeys.size()));
            endNode = nodeVariableManager.getNodeVariable(randomVariable);
            endNodePattern = NodePatternClause.generateVariablePattern(randomVariable);
        } else {
            // 否则从现有图中的节点中随机选择
            endNode = graphManager.getNodes().get(randomly.getInteger(0, graphManager.getNodeNumber()));
            endNodePattern = NodePatternClause.generateRandomNodePattern(endNode, graphManager);
        }
        // 随机方向
        int direction = randomly.getInteger(0, 3);

        String relationshipVariable=null;
        // 随机关系变量名
        if(randomly.getBoolean()){
            relationshipVariable=graphManager.getAsVariableManager().addAlias(ExprUnknownVal.UNKNOWN_LIST);
        }

        // 获取关系类型
        List<String> typeIdentifiers = new ArrayList<>();
        List<CypherSchema.CypherRelationTypeInfo> relationTypes = graphManager.getSchema().getRelationTypes();

        if (!relationTypes.isEmpty()) {
            int numTypes = randomly.getInteger(0, 4); // 随机 0~3 个关系类型
            List<String> allRelationTypeNames = relationTypes.stream()
                    .map(CypherSchema.CypherRelationTypeInfo::getName)
                    .collect(Collectors.toList());

            // 随机选取关系类型名称
            for (int i = 0; i < numTypes; i++) {
                String selectedType = allRelationTypeNames.get(randomly.getInteger(0, allRelationTypeNames.size()));
                if (!typeIdentifiers.contains(selectedType)) {
                    typeIdentifiers.add(selectedType); // 确保类型不重复
                }
            }
        }

        // 随机长度量化
        LengthQuantifier lengthQuantifier;
        if (randomly.getInteger(0, 4) == 0) {
            lengthQuantifier = new LengthQuantifier(randomly.getInteger(1, 5));  // 随机定长 1~5
        } else {
            Integer lowerBound = randomly.getBoolean() ? randomly.getInteger(0, 3) : null;
            Integer upperBound = randomly.getInteger(lowerBound != null ? lowerBound  : 0, 7);
            lengthQuantifier = new LengthQuantifier(lowerBound, upperBound);
        }

        return new QuantifiedPathPatternClause(startNodePattern,endNodePattern,direction, relationshipVariable, typeIdentifiers, lengthQuantifier,null,null);
    }

    public static QuantifiedPathPatternClause generateRandomShortestVarLengthRelationship(GraphManager graphManager,String shortestPath) {
        Randomly randomly = new Randomly();

        String pathVariable = null;
        if(randomly.getBoolean()){
            PathVariableManager pathVariableManager=graphManager.getPathVariableManager();
            pathVariable=pathVariableManager.generatePathVariable();
        }
        // 随机选择起始节点,终止节点
        AbstractNode startNode,endNode;

        // 获取 GraphManager 中的 NodeVariableManager
        NodeVariableManager nodeVariableManager = graphManager.getNodeVariableManager();

        NodePatternClause startNodePattern,endNodePattern;
        // 50% 概率从 NodeVariableManager 中获取随机节点变量
        if (randomly.getBoolean() && !nodeVariableManager.getNodeVariableMap().isEmpty()) {
            // 从变量管理器中随机选择一个节点变量
            List<String> variableKeys = new ArrayList<>(nodeVariableManager.getNodeVariableMap().keySet());
            String randomVariable = variableKeys.get(randomly.getInteger(0, variableKeys.size()));
            startNode = nodeVariableManager.getNodeVariable(randomVariable);
            startNodePattern = NodePatternClause.generateVariablePattern(randomVariable);
        } else {
            // 否则从现有图中的节点中随机选择
            startNode = graphManager.getNodes().get(randomly.getInteger(0, graphManager.getNodeNumber()));
            startNodePattern = NodePatternClause.generateRandomNodePattern(startNode, graphManager);
        }

        // 50% 概率从 NodeVariableManager 中获取随机节点变量
        if (randomly.getBoolean() && !nodeVariableManager.getNodeVariableMap().isEmpty()) {
            // 从变量管理器中随机选择一个节点变量
            List<String> variableKeys = new ArrayList<>(nodeVariableManager.getNodeVariableMap().keySet());
            String randomVariable = variableKeys.get(randomly.getInteger(0, variableKeys.size()));
            endNode = nodeVariableManager.getNodeVariable(randomVariable);
            endNodePattern = NodePatternClause.generateVariablePattern(randomVariable);
        } else {
            // 否则从现有图中的节点中随机选择
            endNode = graphManager.getNodes().get(randomly.getInteger(0, graphManager.getNodeNumber()));
            endNodePattern = NodePatternClause.generateRandomNodePattern(endNode, graphManager);
        }
        // 随机方向
        int direction = randomly.getInteger(0, 3);

        String relationshipVariable=null;
        // 随机关系变量名
        if(randomly.getBoolean()){
            relationshipVariable=graphManager.getAsVariableManager().addAlias(ExprUnknownVal.UNKNOWN_LIST);
        }

        // 获取关系类型
        List<String> typeIdentifiers = new ArrayList<>();
        List<CypherSchema.CypherRelationTypeInfo> relationTypes = graphManager.getSchema().getRelationTypes();

        if (!relationTypes.isEmpty()) {
            int numTypes = randomly.getInteger(0, 4); // 随机 0~3 个关系类型
            List<String> allRelationTypeNames = relationTypes.stream()
                    .map(CypherSchema.CypherRelationTypeInfo::getName)
                    .collect(Collectors.toList());

            // 随机选取关系类型名称
            for (int i = 0; i < numTypes; i++) {
                String selectedType = allRelationTypeNames.get(randomly.getInteger(0, allRelationTypeNames.size()));
                if (!typeIdentifiers.contains(selectedType)) {
                    typeIdentifiers.add(selectedType); // 确保类型不重复
                }
            }
        }

        // 随机长度量化
        LengthQuantifier lengthQuantifier;
        if (randomly.getInteger(0, 4) == 0) {
            lengthQuantifier = new LengthQuantifier(randomly.getInteger(0, 2));  // 随机定长 0-1
        } else {
            Integer lowerBound = randomly.getBoolean() ? randomly.getInteger(0, 1) : null;
            Integer upperBound = randomly.getInteger(lowerBound != null ? lowerBound  : 0, 7);
            lengthQuantifier = new LengthQuantifier(lowerBound, upperBound);
        }

        return new QuantifiedPathPatternClause(startNodePattern,endNodePattern,direction, relationshipVariable, typeIdentifiers, lengthQuantifier,pathVariable,shortestPath);
    }
}
