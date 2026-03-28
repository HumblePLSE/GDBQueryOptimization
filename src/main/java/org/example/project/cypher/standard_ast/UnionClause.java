package org.example.project.cypher.standard_ast;

/**
 * Represents a UNION clause in a Cypher query.
 * Allows combining the results of two or more queries.
 */
public class UnionClause extends Clause {

    private final boolean all; // Indicates if UNION ALL should be used

    /**
     * Constructor for UnionClause.
     *
     * @param all True if UNION ALL should be used, false for UNION.
     */
    public UnionClause(boolean all) {
        super("UNION");
        this.all = all;
    }

    /**
     * Converts the UnionClause into Cypher syntax.
     *
     * @return A string representing the UNION clause in Cypher syntax.
     */
    @Override
    public String toCypher() {
        return all ? "UNION ALL" : "UNION";
    }

    /**
     * Validates the UNION clause.
     * Always returns true, as the clause itself has no complex validation requirements.
     *
     * @return True, indicating the clause is valid.
     */
    @Override
    public boolean validate() {
        return true; // UNION clause is always valid by itself
    }

    /**
     * Gets the value of the all property.
     *
     * @return True if UNION ALL is used, false otherwise.
     */
    public boolean isAll() {
        return all;
    }
}
