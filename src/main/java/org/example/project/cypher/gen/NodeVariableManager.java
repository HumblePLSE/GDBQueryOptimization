package org.example.project.cypher.gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Manages node variables used in Cypher queries.
 * It generates node variables with sequential names (e.g., n0, n1, n2)
 * and manages their lifecycle during query construction.
 */
public class NodeVariableManager {

    private final Map<String, AbstractNode> nodeVariableMap; // Stores the node variables

    private final Map<AbstractNode, String> reverseNodeVariableMap;

    private int nodeindex; // Counter for generating unique node variable names

    public NodeVariableManager() {
        this.nodeVariableMap = new HashMap<>();
        this.reverseNodeVariableMap=new HashMap<>();
        this.nodeindex = 0; // Start from n0
    }

    public void setIndex(int index){nodeindex=index;}

    public NodeVariableManager Copy() {
        NodeVariableManager copy = new NodeVariableManager();
        copy.nodeindex = this.nodeindex; // 直接复制基本类型

        // 浅拷贝 Map 结构，但共享节点对象
        copy.nodeVariableMap.putAll(this.nodeVariableMap); // Map 结构独立，节点引用共享
        copy.reverseNodeVariableMap.putAll(this.reverseNodeVariableMap); // 同上

        return copy;
    }

    public Map<String,AbstractNode> getNodeVariableMap(){
        return nodeVariableMap;
    }
    public Map<AbstractNode,String> getReverseNodeVariableMap(){return reverseNodeVariableMap;}

    /**
     * Get all node variable names stored in the manager.
     *
     * @return A List of all node variable names.
     */
    public List<String> getAllNodeVariables() {
        // Collect all keys (node variable names) from the map
        return new ArrayList<>(nodeVariableMap.keySet());
    }

    /**
     * Generate a new node variable (n0, n1, n2, ...)
     * @param node The AbstractNode to associate with the new node variable
     * @return The generated node variable name
     */
    public String generateNodeVariable(AbstractNode node) {
        // Generate the node variable name based on the current count
        String nodeName = "n" + nodeindex;

        // Store the node with its variable name
        nodeVariableMap.put(nodeName, node);
        reverseNodeVariableMap.put(node, nodeName);

        // Increment the counter for the next node variable
        nodeindex++;

        return nodeName;
    }

    public String generateAliasVariable(AbstractNode node,int aliasindex){
        String alias="alias"+aliasindex;

        // Store the node with its variable name
        nodeVariableMap.put(alias, node);
        reverseNodeVariableMap.put(node, alias);

        // Increment the counter for the next alias
        // 目前的实现是针对于node，relatioship，path的变量存储在对应的variablemanager而非asvariablemanager中
        // aliasindex++在asvariablemanager中实现
        //aliasindex++;

        return alias;
    }

    /**
     * Get the AbstractNode associated with a node variable name.
     * @param nodeName The node variable name (e.g., n0, n1)
     * @return The AbstractNode associated with the node variable, or null if not found
     */
    public AbstractNode getNodeVariable(String nodeName) {
        return nodeVariableMap.get(nodeName);
    }

    /**
     * Get the node variable name associated with a given AbstractNode.
     * @param node The AbstractNode for which to find the variable name
     * @return The node variable name associated with the node, or null if not found
     */
    public String getVariableName(AbstractNode node) {
        return reverseNodeVariableMap.get(node);
    }

    /**
     * Remove a node variable and its associated AbstractNode.
     * @param nodeName The node variable name to remove
     */
    public void removeNodeVariable(String nodeName) {
        AbstractNode node = nodeVariableMap.remove(nodeName);
        if (node != null) {
            reverseNodeVariableMap.remove(node,nodeName);
        }
    }

    public void removeNodeVariables(List<String> variables){
        for(String variable:variables){
            removeNodeVariable(variable);
        }
    }

    /**
     * Clean up all expired node variables, typically at the end of a query's lifecycle.
     */
    public void cleanUpExpiredVariables() {
        nodeVariableMap.clear();
        reverseNodeVariableMap.clear();// Clear all node variables
        nodeindex=0;
    }

    /**
     * Get the current number of stored node variables.
     * @return The count of stored node variables
     */
    public int getNodeindex() {
        return nodeindex;
    }

    public void retainVariables(List<String> retainedVariables) {
        // 创建一个临时的逆向映射以避免并发修改异常
        Map<String, AbstractNode> retainedNodeVariableMap = new HashMap<>();
        Map<AbstractNode, String> retainedReverseNodeVariableMap = new HashMap<>();

        for (String variable : retainedVariables) {
            if (nodeVariableMap.containsKey(variable)) {
                AbstractNode node = nodeVariableMap.get(variable);
                retainedNodeVariableMap.put(variable, node);
                retainedReverseNodeVariableMap.put(node, variable);
            }
        }

        // 更新双向映射，仅保留指定的变量
        nodeVariableMap.clear();
        nodeVariableMap.putAll(retainedNodeVariableMap);

        reverseNodeVariableMap.clear();
        reverseNodeVariableMap.putAll(retainedReverseNodeVariableMap);
    }

    /**
     * 添加单个节点变量映射（不移除旧映射）
     * @param nodeName 变量名（如 "n0"）
     * @param node 对应的节点对象
     */
    public void addVariable(String nodeName, AbstractNode node) {
        nodeVariableMap.put(nodeName, node);
        reverseNodeVariableMap.put(node, nodeName);
    }

    /**
     * 批量添加节点变量映射（不移除旧映射）
     * @param variables 要添加的变量映射集合
     */
    public void addVariables(Map<String, AbstractNode> variables) {
        nodeVariableMap.putAll(variables);
        for (Map.Entry<String, AbstractNode> entry : variables.entrySet()) {
            reverseNodeVariableMap.put(entry.getValue(), entry.getKey());
        }
    }


}
