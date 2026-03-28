package org.example.project.cypher.standard_ast;

import org.example.project.cypher.gen.GraphManager;

/**
 * Represents a FINISH clause in Cypher.
 */
public class FinishClause extends ProjectingClause {

    public FinishClause() {
        super("FINISH");
    }

    @Override
    public String toCypher() {
        return "FINISH ";
    }

    @Override
    public boolean validate() {
        return true;
    }

    /**
     * Generate a random FinishClause.
     * @param graphManager The GraphManager to be used for random generation.
     * @return A randomly generated FinishClause.
     */
    public static FinishClause generateFinishClause(GraphManager graphManager) {

        return new FinishClause();
    }
}
