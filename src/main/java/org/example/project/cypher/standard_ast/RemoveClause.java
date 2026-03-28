package org.example.project.cypher.standard_ast;

import org.apache.commons.lang3.tuple.Pair;
import org.example.project.cypher.gen.*;
import org.example.project.Randomly;
import org.example.project.cypher.schema.CypherSchema;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class RemoveClause extends WritingClause {

    // List to hold node variable and property (nodeVariable, propertyKey)
    private List<Pair<String, String>> nodeVariableProperties;

    private List<Pair<String, List<String>>> nodeVariableLabels;

    private List<Pair<String, String>> relationshipVariableProperties;

    public RemoveClause(List<Pair<String, String>> nodeVariableProperties,List<Pair<String, List<String>>> nodeVariableLabels,List<Pair<String, String>> relationshipVariableProperties) {
        super("REMOVE");
        this.nodeVariableProperties = nodeVariableProperties;
        this.nodeVariableLabels = nodeVariableLabels;
        this.relationshipVariableProperties = relationshipVariableProperties;
    }

    /**
     * Adds a node variable with a property to the removal list.
     *
     * @param nodeVariable The node variable to remove.
     * @param propertyKey  The property key to remove.
     */
    public void addNodeVariableProperty(String nodeVariable, String propertyKey) {
        nodeVariableProperties.add(Pair.of(nodeVariable, propertyKey));
    }


    /**
     * Generates a Cypher string for the REMOVE clause.
     * This string includes properties and labels to remove from nodes and relationships.
     *
     * @return The Cypher REMOVE clause string.
     */
    @Override
    public String toCypher() {
        // Check if all lists are empty, return an empty string
        if (nodeVariableProperties.isEmpty() && nodeVariableLabels.isEmpty() && relationshipVariableProperties.isEmpty()) {
            return ""; // No REMOVE clause needed
        }

        StringBuilder sb = new StringBuilder("REMOVE ");

        // Remove node properties
        for (Pair<String, String> pair : nodeVariableProperties) {
            if (sb.length() > 7) sb.append(", "); // Add a comma if there are already entries
            sb.append(pair.getLeft()).append(".").append(pair.getRight());
        }

        // Remove relationship properties
        for (Pair<String, String> pair : relationshipVariableProperties) {
            if (sb.length() > 7) sb.append(", "); // Add a comma if there are already entries
            sb.append(pair.getLeft()).append(".").append(pair.getRight());
        }

        // Remove node labels
        for (Pair<String, List<String>> pair : nodeVariableLabels) {
            if (!pair.getRight().isEmpty()) {
                if (sb.length() > 7) sb.append(", "); // Add a comma if there are already entries
                sb.append(pair.getLeft()).append(":").append(String.join(":", pair.getRight()));
            }
        }

        return sb.toString();
    }

    /**
     * Validates the REMOVE clause.
     * Ensures that there is at least one property or label to remove.
     *
     * @return True if there is at least one property or label to remove, false otherwise.
     */
    @Override
    public boolean validate() {
        return !nodeVariableProperties.isEmpty() || !nodeVariableLabels.isEmpty() || !relationshipVariableProperties.isEmpty();
    }


    public static RemoveClause generateRandomRemoveClause(GraphManager graphManager) {
        Randomly randomly = new Randomly();

        List<Pair<String,String>> nodeVariableProperties=graphManager.getRandomNodeVariableProperties();
        List<Pair<String,String>> relationshipVariableProperties=graphManager.getRandomRelationshipVariableProperties();
        List<Pair<String,List<String>>> nodeVariableLabels=graphManager.getRandomNodeVariableLabels();

        graphManager.removeNodeProperties(nodeVariableProperties);
        graphManager.removeRelationshipProperties(relationshipVariableProperties);
        graphManager.removeNodeLabels(nodeVariableLabels);

        // 创建 RemoveClause 实例，用于存储删除操作
        RemoveClause removeClause = new RemoveClause(nodeVariableProperties,nodeVariableLabels,relationshipVariableProperties);

        return removeClause;

        /*// Remove node labels
        for (Pair<String, List<String>> pair : nodeVariableLabels) {
            AbstractNode node = nodeVariableManager.getNodeVariable(pair.getLeft());
            if (node != null) {
                for (String label : pair.getRight()) {
                    node.removeLabel(label);
                }
            }
        }*/

    }
}
