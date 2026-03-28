package org.example.project.cypher.standard_ast;

import org.example.project.cypher.ast.Direction;
import org.example.project.cypher.gen.AbstractRelationship;
import org.example.project.cypher.gen.GraphManager;
import org.example.project.cypher.ast.IClause;
import org.example.project.Randomly;
import org.example.project.cypher.gen.AbstractNode;
import org.example.project.cypher.gen.NodeVariableManager;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Represents a Simple Path Pattern in Cypher queries.
 *
 * simplePathPattern ::= nodePattern [ { relationshipPattern nodePattern }* ]
 *
 * Context-free grammar for simplePathPattern:
 * simplePathPattern ::= nodePattern [ { relationshipPattern nodePattern }* ]
 *
 * where:
 * - nodePattern is a node represented by its labels and properties.
 * - relationshipPattern represents the relationship between two nodes.
 *
 * A Simple Path Pattern in a Cypher query consists of a series of nodes and relationships.
 * The path starts from a randomly selected node and is extended through relationships in random directions (from, to, any).
 * The path terminates when either no more relationships are found or a random direction 'none' is chosen to stop the path extension.
 */
public class SimplePathPatternClause extends Clause {

    private final NodePatternClause startNodePattern;  // The starting node's pattern
    private final List<RelationshipNodePair> leftPath; // 左侧路径
    private final List<RelationshipNodePair> rightPath; // 右侧路径
    // List of RelationshipNodePairs (relationship and connected nodes)

    /**
     * Constructor to create a SimplePathPatternClause.
     *
     * @param startNodePattern The pattern for the starting node.
     * path A list of RelationshipNodePair representing the path from start node to end node.
     */
    public SimplePathPatternClause(NodePatternClause startNodePattern, List<RelationshipNodePair> leftpath,List<RelationshipNodePair> rightPath) {
        super("SimplePathPattern");
        this.startNodePattern = startNodePattern;
        this.leftPath = leftpath;
        this.rightPath=rightPath;
    }

    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();

        // 添加左侧路径
        for (int i = leftPath.size() - 1; i >= 0; i--) {
            RelationshipNodePair pair = leftPath.get(i);
            sb.append(pair.node.toCypher()).append(" ").append(pair.relationship.toCypher()).append(" ");
        }

        // 添加起始节点
        sb.append(startNodePattern.toCypher()).append(" ");

        // 添加右侧路径
        for (RelationshipNodePair pair : rightPath) {
            sb.append(pair.relationship.toCypher()).append(" ").append(pair.node.toCypher()).append(" ");
        }

        return sb.toString().trim();//.trim()?
    }


    @Override
    public boolean validate() {
        return startNodePattern.validate() &&
                leftPath.stream().allMatch(pair -> pair.node.validate() && pair.relationship.validate()) &&
                rightPath.stream().allMatch(pair -> pair.node.validate() && pair.relationship.validate());
    }


    /**
     * Generates a random SimplePathPatternClause using the GraphManager.
     * This method randomly selects a starting node, expands the path in random directions
     * (either 'from', 'to', 'any', or 'none'), and generates a valid Cypher path pattern.
     *
     * @param graphManager The GraphManager to access nodes and relationships.
     * @return A randomly generated SimplePathPatternClause.
     */
    public static SimplePathPatternClause generateRandomSimplePathPattern(GraphManager graphManager,Set<AbstractRelationship> visitedRelationships) {
        // 随机选择起始节点
        AbstractNode startNode;
        Randomly randomly = new Randomly();

        // 获取 GraphManager 中的 NodeVariableManager
        NodeVariableManager nodeVariableManager = graphManager.getNodeVariableManager();

        NodePatternClause startNodePattern;
        // 50% 概率从 NodeVariableManager 中获取随机节点变量
        //if (randomly.getBoolean() && !nodeVariableManager.getNodeVariableMap().isEmpty()) {
        if ((randomly.getInteger(0,10)>=2) && !nodeVariableManager.getNodeVariableMap().isEmpty()) {//change probability
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
        List<RelationshipNodePair> leftPath = new ArrayList<>(); // 左侧路径
        List<RelationshipNodePair> rightPath = new ArrayList<>(); // 右侧路径

        AbstractNode currentNode = startNode;
        AbstractRelationship relationship = null;
        AbstractNode nextNode = null;


        int pathlength=1;
        //左侧拓展
        while (true) {
            String direction = getRandomDirection(0); // 随机选择方向

            if(pathlength>=7) direction="none";
            if (direction.equals("none")) {
                break; // 终止拓展
            }

            pathlength++;
            List<AbstractRelationship> relationships;
            if (direction.equals("any")) {
                relationships = currentNode.getRelationships();
            } else if (direction.equals("from")) {
                relationships = currentNode.getRelationshipsfrom();
            } else if (direction.equals("to")) {
                relationships = currentNode.getRelationshipsto();
            } else {
                relationships = new ArrayList<>();
            }

            // 过滤已访问的关系
            relationships = relationships.stream()
                    .filter(rel -> !visitedRelationships.contains(rel))
                    .collect(Collectors.toList());

            if (relationships.isEmpty()) {
                break;
            }

            relationship = relationships.get(randomly.getInteger(0, relationships.size()));
            visitedRelationships.add(relationship);

            nextNode = relationship.getFrom().equals(currentNode) ? relationship.getTo() : relationship.getFrom();

            if (nextNode == null) {
                break;
            }


            // 检查下一个节点是否有已存储的变量
            String nodeVariable = nodeVariableManager.getVariableName(nextNode);

            NodePatternClause nextNodePattern;
            if (nodeVariable != null && randomly.getInteger(0, 100) < 90) {//changr probabilty, before 70%
                // 以 80% 的概率使用变量生成 NodePattern
                nextNodePattern = NodePatternClause.generateVariablePattern(nodeVariable);
               // System.out.println(nextNodePattern.toCypher());
            } else {
                // 否则随机生成一个新的 NodePattern
                nextNodePattern = NodePatternClause.generateRandomNodePattern(nextNode, graphManager);
                //System.out.println(nextNodePattern.toCypher());
            }

            String direc = relationship.getFrom().equals(currentNode) ? "left" : "right";
            RelationshipPatternClause relationshipPatternClause=RelationshipPatternClause.generateRandomRelationshipPattern(relationship,direc,graphManager);
           // System.out.println(relationshipPatternClause.toCypher());
            leftPath.add(new RelationshipNodePair(relationshipPatternClause, nextNodePattern));

            currentNode = nextNode;
            nextNode=null;
            relationship=null;
        }

        // 右侧拓展
        currentNode = startNode;
        nextNode=null;
        relationship=null;
        while (true) {
            String direction = getRandomDirection(0);

            //while ((direction.equals("none")&&pathlength<=1)) direction=getRandomDirection(0);//控制路径长度>1

            if(pathlength>=7) direction="none";
            if (direction.equals("none")) {
                break;
            }
            pathlength++;

            List<AbstractRelationship> relationships;
            if (direction.equals("any")) {
                relationships = currentNode.getRelationships();
            } else if (direction.equals("from")) {
                relationships = currentNode.getRelationshipsfrom();
            } else if (direction.equals("to")) {
                relationships = currentNode.getRelationshipsto();
            } else {
                relationships = new ArrayList<>();
            }

            // 过滤已访问的关系
            relationships = relationships.stream()
                    .filter(rel -> !visitedRelationships.contains(rel))
                    .collect(Collectors.toList());

            if (relationships.isEmpty()) {
                break;
            }

            relationship = relationships.get(randomly.getInteger(0, relationships.size()));
            visitedRelationships.add(relationship);

            nextNode = relationship.getFrom().equals(currentNode) ? relationship.getTo() : relationship.getFrom();

            if (nextNode == null) {
                break;
            }

            // 检查下一个节点是否有已存储的变量
            String nodeVariable = nodeVariableManager.getVariableName(nextNode);

            NodePatternClause nextNodePattern;
            if (nodeVariable != null && randomly.getInteger(0, 100) < 90) {//change probabilty
                nextNodePattern = NodePatternClause.generateVariablePattern(nodeVariable);
            } else {
                nextNodePattern = NodePatternClause.generateRandomNodePattern(nextNode, graphManager);
            }

            String direc = relationship.getFrom().equals(currentNode) ? "right" : "left";
            rightPath.add(new RelationshipNodePair(RelationshipPatternClause.generateRandomRelationshipPattern(relationship, direc, graphManager), nextNodePattern));

            currentNode = nextNode;
            nextNode=null;
            relationship=null;
        }

        return new SimplePathPatternClause(startNodePattern, leftPath, rightPath);
    }

    public static SimplePathPatternClause generateCreatePathPattern(GraphManager graphManager, Set<AbstractRelationship> visitedRelationships) {
        // 随机选择起始节点
        AbstractNode startNode;
        Randomly randomly = new Randomly();

        // 获取 GraphManager 中的 NodeVariableManager
        NodeVariableManager nodeVariableManager = graphManager.getNodeVariableManager();
        NodePatternClause startNodePattern;
        boolean existstartnode;

        // 50% 概率从 NodeVariableManager 中获取随机节点变量
        if (randomly.getBoolean() && !nodeVariableManager.getNodeVariableMap().isEmpty()) {
            // 从变量管理器中随机选择一个节点变量
            List<String> variableKeys = new ArrayList<>(nodeVariableManager.getNodeVariableMap().keySet());
            String randomVariable = variableKeys.get(randomly.getInteger(0, variableKeys.size()));
            startNode = nodeVariableManager.getNodeVariable(randomVariable);
            startNodePattern = NodePatternClause.generateVariablePattern(randomVariable);
            existstartnode=true;
        } else {
            // 否则随机生成一个节点
            startNode = graphManager.randomColorNode();
            graphManager.getNodes().add(startNode);
            graphManager.addNodeNum();
            startNodePattern = NodePatternClause.generateFullNodePattern(startNode, graphManager);
            existstartnode=false;
        }

        List<RelationshipNodePair> path = new ArrayList<>(); // 单侧路径

        AbstractNode currentNode = startNode;
        AbstractRelationship relationship = null;
        AbstractNode nextNode = null;

        int pathlength=1;
        // 向右侧拓展路径
        while (true) {
            String direction;
            if(pathlength==1&&existstartnode){
                direction=randomly.fromOptions("from", "to");
            }
            else {
                direction = getRandomDirection(1); // 随机选择方向
            }

            if (direction.equals("none")) {
                break; // 终止拓展
            }
            pathlength++;

            // 创建新的关系
            relationship = graphManager.randomColorRelationship();
            graphManager.getRelationships().add(relationship);
            if (relationship == null) {
                break;
            }

            // 创建下一个节点模式
            NodePatternClause nextNodePattern;
            if (randomly.getBoolean() && !nodeVariableManager.getNodeVariableMap().isEmpty()) {
                // 从变量管理器中随机选择一个节点变量
                List<String> variableKeys = new ArrayList<>(nodeVariableManager.getNodeVariableMap().keySet());
                String randomVariable = variableKeys.get(randomly.getInteger(0, variableKeys.size()));
                nextNode = nodeVariableManager.getNodeVariable(randomVariable);
                nextNodePattern = NodePatternClause.generateVariablePattern(randomVariable);
            } else {
                // 否则随机生成一个节点
                nextNode = graphManager.randomColorNode();
                graphManager.getNodes().add(nextNode);
                graphManager.addNodeNum();
                nextNodePattern = NodePatternClause.generateFullNodePattern(nextNode, graphManager);
            }

            // 根据方向设置关系方向
            if (direction.equals("from")) {
                relationship.setFrom(currentNode);
                relationship.setTo(nextNode);
            } else {
                relationship.setFrom(nextNode);
                relationship.setTo(currentNode);
            }

            // 将关系和节点添加到 GraphManager
            graphManager.getRelationships().add(relationship);
            currentNode.addRelationship(relationship);
            nextNode.addRelationship(relationship);

            // 生成关系模式
            String relationshipDirection = direction.equals("from") ? "right" : "left";
            RelationshipPatternClause relationshipPatternClause = RelationshipPatternClause.generateFullRelationshipPattern(
                    relationship, relationshipDirection, graphManager
            );
            path.add(new RelationshipNodePair(relationshipPatternClause, nextNodePattern));

            // 更新当前节点
            currentNode = nextNode;
        }

        return new SimplePathPatternClause(startNodePattern,  new ArrayList<>(),path);
    }


    /**
     * Randomly selects a direction for path extension.
     * Returns "from", "to", or "any" based on random selection.
     */
    private static String getRandomDirection(int directionchoice) {
        Randomly randomly = new Randomly();
        int rand = randomly.getInteger(directionchoice,6); // 0 = any, 1 = to, 2 = from, 3 = none
        switch (rand) {
            case 0:
                return "any";
            case 1:
                return "to";
            case 2:
                return "from";
            default:
                return "none";
        }
    }

    /**
     * Helper class to represent a relationship-node pair in a path.
     */
    public static class RelationshipNodePair {
        public final RelationshipPatternClause relationship;
        public final NodePatternClause node;

        public RelationshipNodePair(RelationshipPatternClause relationship, NodePatternClause node) {
            this.relationship = relationship;
            this.node = node;
        }
    }
}
