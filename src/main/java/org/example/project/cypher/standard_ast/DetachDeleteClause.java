package org.example.project.cypher.standard_ast;

import org.example.project.Randomly;
import org.example.project.cypher.gen.AbstractNode;
import org.example.project.cypher.gen.AbstractRelationship;
import org.example.project.cypher.gen.GraphManager;
import org.example.project.cypher.gen.NodeVariableManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a DETACH DELETE clause in a Cypher query.
 * Deletes a randomly selected node from the NodeVariableManager
 * provided by the GraphManager, along with all its relationships.
 */
public class DetachDeleteClause extends WritingClause {

    private List<String> nodeVariables; // List of node variables to delete

    public DetachDeleteClause(List<String> nodeVariables) {
        super("DETACH DELETE");
        this.nodeVariables = nodeVariables != null ? nodeVariables : new ArrayList<>();
    }

    /**
     * Randomly select a few node variables (up to 5) from the NodeVariableManager
     * and prepare them for deletion.
     */
    public static DetachDeleteClause generateRandomDetachDeleteClause(GraphManager graphManager) {
        Randomly randomly = new Randomly();
        NodeVariableManager nodeVariableManager = graphManager.getNodeVariableManager();

        // Get all node variables available
        List<String> allNodeVariables = new ArrayList<>(nodeVariableManager.getAllNodeVariables());

        // Randomly decide how many nodes to select (between 0 and 3)
        int numNodesToSelect = randomly.getInteger(0, 4);

        List<String> selectedNodeVariables = new ArrayList<>();

        // Randomly pick the node variables
        for (int i = 0; i < numNodesToSelect && i < allNodeVariables.size(); i++) {
            String selectedNode = allNodeVariables.get(randomly.getInteger(0, allNodeVariables.size()));
            AbstractNode node = nodeVariableManager.getNodeVariable(selectedNode);

            // Ensure the node exists
            if (node == null) {
                continue;
            }

            graphManager.decreaseNodeNum();

            selectedNodeVariables.add(selectedNode);

            // Remove the node from the GraphManager
            graphManager.getNodes().remove(node);
            nodeVariableManager.removeNodeVariable(selectedNode);

            // Remove relationships connected to the node
            if (node.getRelationships() != null) {
                List<AbstractRelationship> relationshipsToRemove = new ArrayList<>(node.getRelationships());

                for (AbstractRelationship relationship : relationshipsToRemove) {
                    // Remove the relationship from the GraphManager
                    graphManager.getRelationships().remove(relationship);

                    // Remove the relationship from the other connected node
                    AbstractNode otherNode = relationship.getFrom().equals(node)
                            ? relationship.getTo()
                            : relationship.getFrom();

                    if (otherNode != null) {
                        otherNode.getRelationships().remove(relationship);
                    }

                    // Remove the relationship from the RelationshipVariableManager
                    graphManager.getRelationshipVariableManager().removeRelationship(relationship);
                }

                // Clear all relationships from the current node
                node.getRelationships().clear();
            }
        }

        return new DetachDeleteClause(selectedNodeVariables);
    }


    /**
     * Convert the DETACH DELETE clause into Cypher syntax.
     * It will generate Cypher for deleting the selected node variables.
     *
     * @return A String representing the DETACH DELETE clause in Cypher syntax.
     */
    @Override
    public String toCypher() {
        if (nodeVariables.isEmpty()) {
            return ""; // No deletion to perform
        }
        return "DETACH DELETE " + String.join(", ", nodeVariables);
    }

    /**
     * Validate the DETACH DELETE clause.
     * A valid DETACH DELETE clause must have at least one selected node variable for deletion.
     *
     * @return True if at least one node variable is selected for deletion, false otherwise.
     */
    @Override
    public boolean validate() {
        return !nodeVariables.isEmpty();
    }
}
