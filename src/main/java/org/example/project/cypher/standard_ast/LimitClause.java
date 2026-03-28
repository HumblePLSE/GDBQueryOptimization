package org.example.project.cypher.standard_ast;

import org.example.project.Randomly;

/**
 * Represents a LIMIT clause in a Cypher query.
 * LIMIT specifies the maximum number of rows to return.
 */
public class LimitClause extends ReadingSubClause {

    private final int limit; // The maximum number of rows to return

    /**
     * Constructor for LimitClause.
     * @param limit The maximum number of rows to return.
     */
    public LimitClause(int limit) {
        super("LIMIT");
        this.limit = limit;
    }

    /**
     * Generates a random LimitClause with a random limit value.
     * @return A randomly generated LimitClause.
     */
    public static LimitClause generateRandomLimitClause() {
        Randomly randomly = new Randomly();
        // Generate a random limit between 1 and 50
        int limit = randomly.getInteger(0, 51);
        return new LimitClause(limit);
    }

    /**
     * Converts the LIMIT clause to Cypher query syntax.
     * @return A String representing the Cypher syntax for this LIMIT clause.
     */
    @Override
    public String toCypher() {
        return "LIMIT " + limit;
    }

    /**
     * Validates the LIMIT clause to ensure it is well-formed.
     * A valid LIMIT clause must have a positive limit value.
     * @return True if the limit is valid, false otherwise.
     */
    @Override
    public boolean validate() {
        return limit >= 0;
    }

    /**
     * Get the limit value of this clause.
     * @return The limit value.
     */
    public int getLimit() {
        return limit;
    }
}

