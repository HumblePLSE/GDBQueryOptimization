package org.example.project.cypher.standard_ast;

import org.example.project.Randomly;

/**
 * Represents a SKIP clause in Cypher queries.
 * The SKIP clause specifies the number of rows to skip in the result set.
 */
public class SkipClause extends ReadingSubClause {

    private final int skipCount; // Number of rows to skip

    public SkipClause(int skipCount) {
        super("SKIP");
        this.skipCount = skipCount;
    }

    @Override
    public String toCypher() {
        return "SKIP " + skipCount;
    }

    @Override
    public boolean validate() {
        return skipCount >= 0; // Ensure the skip count is non-negative
    }

    /**
     * Generates a random SkipClause with a non-negative skip count.
     * @return A randomly generated SkipClause.
     */
    public static SkipClause generateRandomSkipClause() {
        Randomly randomly = new Randomly();
        int skipCount;
        if(randomly.getInteger(0,10)<9) skipCount=0;
        else skipCount = randomly.getInteger(0, 10); // Generate a skip count between 0 and 9
        return new SkipClause(skipCount);
    }

    /**
     * Get the skip count for this clause.
     * @return The number of rows to skip.
     */
    public int getSkipCount() {
        return skipCount;
    }
}
