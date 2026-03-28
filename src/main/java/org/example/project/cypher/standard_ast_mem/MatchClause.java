package org.example.project.cypher.standard_ast_mem;

import org.example.project.Randomly;

/**
 * Represents a MATCH or OPTIONAL MATCH clause in a Cypher query.
 */
public class MatchClause extends Clause {

    private final boolean optional; // 是否为 OPTIONAL MATCH

    public MatchClause(boolean optional) {
        super("Match");
        this.optional = optional;
    }

    @Override
    public String toCypher() {
        return optional ? "OPTIONAL MATCH" : "MATCH";
    }

    @Override
    public boolean validate() {
        return true;
    }

    public static MatchClause generateRandomMatchClause(boolean optional) {
        Randomly randomly = new Randomly();
        return new MatchClause(optional);
    }


}


