package org.example.project.cypher.standard_ast;

import org.example.project.Randomly;
import org.example.project.cypher.gen.GraphManager;
import org.example.project.cypher.gen.NodeVariableManager;
import org.example.project.cypher.gen.RelationshipVariableManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a DELETE clause in a Cypher query.
 * Deletes a randomly selected relationship or node from the provided managers.
 */
public class DeleteClause extends WritingClause {

    private final List<String> nodeVariables;
    private final List<String> relationshipVariables;


    public DeleteClause(List<String> nodeVariables, List<String> relationshipVariables) {
        super("DELETE");
        this.nodeVariables = nodeVariables;
        this.relationshipVariables = relationshipVariables;
    }

    /**
     * Randomly select a relationship variable or node variable for deletion.
     * This method selects a node without a relationship or a relationship variable
     * for deletion with specific probabilities.
     */
    public static DeleteClause generateRandomDeleteClause(GraphManager graphManager) {
        Randomly randomly = new Randomly();

        NodeVariableManager nodeVariableManager = graphManager.getNodeVariableManager();
        RelationshipVariableManager relationshipVariableManager = graphManager.getRelationshipVariableManager();

        List<String> nodeVariables = new ArrayList<>();
        List<String> relationshipVariables = new ArrayList<>();

        // Randomly select node variables that don't have relationships
        for (String nodeVariable : nodeVariableManager.getAllNodeVariables()) {
            // Check if the node is not connected to any relationship
            boolean hasRelationship = nodeVariableManager.getNodeVariable(nodeVariable).hasRelationship();

            // If the node has no relationships, and we randomly decide, add it to nodeVariables
            if (!hasRelationship && randomly.getBoolean()) {
                graphManager.decreaseNodeNum();
                nodeVariables.add(nodeVariable);
                nodeVariableManager.removeNodeVariable(nodeVariable);
                graphManager.getNodes().remove(nodeVariableManager.getNodeVariable(nodeVariable));
            }
        }

        // Randomly select relationship variables for deletion
        for (String relationshipVariable : relationshipVariableManager.getAllRelationshipVariables()) {
            // With a 1/3 probability, add this relationship variable to the deletion list
            if (randomly.getBoolean() && randomly.getInteger(1, 4) == 1) {
                relationshipVariables.add(relationshipVariable);
                relationshipVariableManager.removeRelationshipVariable(relationshipVariable);
                graphManager.getRelationships().remove(relationshipVariableManager.getRelationship(relationshipVariable));
            }
        }
        /*        // If neither nodeVariables nor relationshipVariables are selected, return an empty DeleteClause
        if (nodeVariables.isEmpty() && relationshipVariables.isEmpty()) {
            return new DeleteClause(new ArrayList<>(), new ArrayList<>());
        }*/

        return new DeleteClause(nodeVariables, relationshipVariables);
    }


    /**
     * Convert the DELETE clause into Cypher syntax.
     * It will generate Cypher for deleting the selected node or relationship variables.
     *
     * @return A String representing the DELETE clause in Cypher syntax.
     */
    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();

        if(nodeVariables.isEmpty()&&relationshipVariables.isEmpty()) return sb.toString();
        sb.append("DELETE ");

        // If there are node variables, add them to the DELETE clause
        if (!nodeVariables.isEmpty()) {
            sb.append(String.join(", ", nodeVariables));
        }

        // If there are relationship variables, add them to the DELETE clause
        if (!relationshipVariables.isEmpty()) {
            if (!nodeVariables.isEmpty()) {
                sb.append(",");  // Ensure there's space if we didn't already add node variables
            }
            sb.append(String.join(", ", relationshipVariables));
        }

        return sb.toString();
    }

    /**
     * Validate the DELETE clause.
     * The DELETE clause is valid if it has at least one node or relationship selected for deletion.
     *
     * @return True if the clause has valid deletions, false otherwise.
     */
    @Override
    public boolean validate() {
        return !nodeVariables.isEmpty() || !relationshipVariables.isEmpty();
    }
}
