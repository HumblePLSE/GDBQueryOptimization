package org.example.project.cypher.standard_ast;

import org.example.project.cypher.gen.GraphManager;
import org.example.project.cypher.schema.IPropertyInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a SET clause in a Cypher query.
 * It sets properties for nodes or relationships in the query.
 */
public class SetClause extends WritingClause {

    private List<String> variableProperties;  // List of variable properties to set

    public SetClause(List<String> variableProperties) {
        super("SET");
        this.variableProperties=variableProperties;
    }

    /**
     * Generates a random SET clause using properties from the GraphManager.
     * This method randomly selects properties to be set and creates a SET clause.
     *
     * @param graphManager The GraphManager to get properties from.
     * @return A SetClause object containing randomly selected properties.
     */
    public static SetClause generateRandomSetClause(GraphManager graphManager) {
        List<String> randomProperties = graphManager.getRandomPropertiesToSet();  // Assuming this method will return random properties
        return new SetClause(randomProperties);
    }

    /**
     * Convert the SET clause into Cypher syntax.
     * The SET clause will look like: SET nodeVariable.property = value
     *
     * @return A String representing the Cypher syntax for the SET clause.
     */
    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();
        if(variableProperties.isEmpty()) return sb.toString();
        sb.append("SET ");

        // Join all properties in the format "nodeVariable.property = value"
        for (int i = 0; i < variableProperties.size(); i++) {
            String property = variableProperties.get(i);
            sb.append(property);
            if (i < variableProperties.size() - 1) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    /**
     * Validate the SET clause.
     * The clause is valid if it has at least one property to set.
     *
     * @return True if there are properties to set, false otherwise.
     */
    @Override
    public boolean validate() {
        return !variableProperties.isEmpty();
    }
}
