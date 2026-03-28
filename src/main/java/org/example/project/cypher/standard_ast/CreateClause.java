package org.example.project.cypher.standard_ast;
import org.example.project.cypher.gen.GraphManager;
/**
 * Represents a CREATE clause in a Cypher query.
 */
public class CreateClause extends WritingClause {

    private final GraphPatternClause patternClause; // GraphPattern 子句

    public CreateClause(GraphPatternClause patternClause) {
        super("CREATE");
        this.patternClause = patternClause;
    }

    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();
        sb.append(clauseType).append(" ");
        sb.append(patternClause.toCypher()); // 调用 GraphPatternClause 的 toCypher 方法
        return sb.toString();
    }

    @Override
    public boolean validate() {
        return patternClause != null && patternClause.validate(); // 验证 GraphPatternClause 的有效性
    }

    /**
     * Generates a random CreateClause using a GraphPatternClause.
     * @param graphManager The GraphManager for generating graph patterns.
     * @return A randomly generated CreateClause.
     */
    public static CreateClause generateRandomCreateClause(GraphManager graphManager) {
        // 利用 GraphPatternClause 的生成函数生成一个随机的图模式
        GraphPatternClause patternClause = GraphPatternClause.generateRandomCreateGraphPattern(1, graphManager);
        return new CreateClause(patternClause);
    }
}
