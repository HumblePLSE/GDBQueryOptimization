package org.example.project.cypher.gen;

import java.util.*;

/**
 * Manages relationship variables used in Cypher queries.
 * It generates relationship variables with sequential names (e.g., r0, r1, r2)
 * and manages their lifecycle during query construction.
 *
 * This manager ensures that relationship variables are uniquely generated for each relationship
 * and can be associated with a specific AbstractRelationship type. It supports
 * adding, removing, and cleaning up relationship variables as the query progresses.
 */
public class RelationshipVariableManager {

    private final Map<String, AbstractRelationship> relationshipVariableMap; // Stores the relationship variables
    private int relationshipIndex; // Counter for generating unique relationship variable names

    public RelationshipVariableManager() {
        this.relationshipVariableMap = new HashMap<>();
        this.relationshipIndex = 0; // Start from r0
    }

    public void setIndex(int index){relationshipIndex=index;}

    public RelationshipVariableManager Copy() {
        RelationshipVariableManager copy = new RelationshipVariableManager();
        copy.relationshipIndex = this.relationshipIndex; // 复制基本类型

        // 浅拷贝 Map 结构，共享关系对象
        copy.relationshipVariableMap.putAll(this.relationshipVariableMap);

        return copy;
    }

    /**
     * Get all relationship variable names stored in the manager.
     *
     * @return A List of all relationship variable names.
     */
    public List<String> getAllRelationshipVariables() {
        // Collect all keys (relationship variable names) from the map
        return new ArrayList<>(relationshipVariableMap.keySet());
    }

    /**
     * Generate a new relationship variable (r0, r1, r2, ...)
     * @param relationship The relationship to associate with the new relationship variable
     * @return The generated relationship variable name
     */
    public String generateRelationshipVariable(AbstractRelationship relationship) {
        // Generate the relationship variable name based on the current count
        String relationshipVariable = "r" + relationshipIndex;

        // Store the relationship type with its variable name
        relationshipVariableMap.put(relationshipVariable, relationship);

        // Increment the counter for the next relationship variable
        relationshipIndex++;

        return relationshipVariable;
    }

    public String generateAliasVariable(AbstractRelationship relationship,int aliasindex){
        String alias="alias"+aliasindex;

        // Store the node with its variable name
        relationshipVariableMap.put(alias, relationship);

        // Increment the counter for the next alias
        // 目前的实现是针对于node，relatioship，path的变量存储在对应的variablemanager而非asvariablemanager中
        // aliasindex++在asvariablemanager中实现
        //aliasindex++;

        return alias;
    }



    /**
     * Get the relationship type associated with a relationship variable name.
     * @param relationshipVariable The relationship variable name (e.g., r0, r1)
     * @return The relationship type associated with the relationship variable, or null if not found
     */
    public AbstractRelationship getRelationship(String relationshipVariable) {
        return relationshipVariableMap.get(relationshipVariable);
    }

    /**
     * Remove a relationship variable and its associated relationship type.
     * @param relationshipVariable The relationship variable name to remove
     */
    public void removeRelationshipVariable(String relationshipVariable) {
        relationshipVariableMap.remove(relationshipVariable);
    }

    public void removeRelationshipVariables(List<String> relationshipVariables){
        for(String relationshipVariable:relationshipVariables){
            removeRelationshipVariable(relationshipVariable);
        }
    }

    /**
     * Clean up all expired relationship variables, typically at the end of a query's lifecycle.
     * This is useful for clearing any variables that are no longer needed or that belong to an expired query.
     */
    public void cleanUpExpiredVariables() {

        relationshipVariableMap.clear(); // Clear all relationship variables
        relationshipIndex=0;
    }

    /**
     * Get the current number of stored relationship variables.
     * @return The count of stored relationship variables
     */
    public int getRelationshipIndex() {
        return relationshipIndex;
    }


    public java.util.Map<String, AbstractRelationship> getRelationshipVariableMap() {
        return relationshipVariableMap;
    }

    public void retainVariables(List<String> retainedVariables) {
        relationshipVariableMap.keySet().retainAll(retainedVariables); // 仅保留传递的变量
    }

    /**
     * 根据给定的 AbstractRelationship 对象删除对应的条目
     *
     * @param relationship 要删除的 AbstractRelationship 对象
     */
    public void removeRelationship(AbstractRelationship relationship) {
        if (relationship == null) {
            return; // 如果传入的 relationship 为 null，直接返回
        }

        // 遍历 relationshipVariableMap 查找并删除对应的条目
        for (Iterator<Map.Entry<String, AbstractRelationship>> iterator = relationshipVariableMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, AbstractRelationship> entry = iterator.next();
            // 比较 AbstractRelationship 对象，如果相同，则移除
            if (entry.getValue().equals(relationship)) {
                iterator.remove();
                break; // 删除后退出循环，避免不必要的遍历
            }
        }
    }

    public void addVariables(Map<String, AbstractRelationship> variables) {
        relationshipVariableMap.putAll(variables);
    }
}
