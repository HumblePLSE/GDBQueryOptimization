package org.example.project.cypher.standard_ast;

import org.example.project.cypher.gen.GraphManager;

/**
 * MergeClause class, a concrete implementation of ReadingWritingClause.
 * This class generates the Cypher MERGE clause.
 */
public class MergeClause extends ReadingWritingClause {

    private final GraphPatternClause patternClause;   // PATTERN 子句

    public MergeClause(GraphPatternClause patternClause) {
        super("MERGE");  // "MERGE" is the clause type
        this.patternClause=patternClause;
    }

    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();
        sb.append("MERGE ");
        sb.append(patternClause.toCypher());
        return sb.toString();
    }

    @Override
    public boolean validate() {
        return patternClause != null && patternClause.validate();
    }

    public static MergeClause generateRandomMergeClause(GraphManager graphManager) {
        // 使用生成器函数生成 GraphPatternClause

        GraphPatternClause graphPatternClause = GraphPatternClause.generateRandomCreateGraphPattern(1,graphManager);

        return new MergeClause(graphPatternClause);
    }
}
