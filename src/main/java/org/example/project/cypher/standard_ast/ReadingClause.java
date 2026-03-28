package org.example.project.cypher.standard_ast;

import org.example.project.cypher.gen.GraphManager;

/**
 * Abstract class representing a generic reading clause in a Cypher query.
 */
public class ReadingClause extends Clause {

    private final MatchClause matchClause;       // MATCH 子句
    private final GraphPatternClause patternClause;   // PATTERN 子句


    public ReadingClause(MatchClause matchClause, GraphPatternClause patternClause) {
        super("Reading");
        this.matchClause = matchClause;
        this.patternClause = patternClause;

    }

    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();
        sb.append(matchClause.toCypher()).append(" ");
        sb.append(patternClause.toCypher());
        return sb.toString();
    }

    @Override
    public boolean validate() {
        return matchClause != null && matchClause.validate()
                && patternClause != null && patternClause.validate();
    }

    public static ReadingClause generateReadingClause(GraphManager graphManager) {
        // 使用生成器函数生成 Match_Clause 和 GraphPatternClause
        MatchClause matchClause = MatchClause.generateRandomMatchClause();
        GraphPatternClause graphPatternClause = GraphPatternClause.generateRandomGraphPattern(1,graphManager);

        return new ReadingClause(matchClause, graphPatternClause);
    }
}

