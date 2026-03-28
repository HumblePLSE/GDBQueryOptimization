package org.example.project.cypher.standard_ast_mem;

import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.example.project.AbstractAction;
import org.example.project.Randomly;
import org.example.project.cypher.ast.IExpression;
import org.example.project.cypher.gen.AbstractNode;
import org.example.project.cypher.gen.AbstractRelationship;
import org.example.project.cypher.gen.GraphManager;
import org.example.project.cypher.gen.RandomExpressionGenerator;
import org.example.project.cypher.standard_ast.expr.CypherExpression;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WhereClause extends Clause {

    private   IExpression expression;
    private  SimplePathPatternClause Patternexpression = null;
    public WhereClause(IExpression expression) {
        super("Where");
        this.expression = expression;
    }
    public WhereClause(SimplePathPatternClause expression) {
        super("Where");
        this.Patternexpression = expression;
    }

    @Override
    public String toCypher() {
        if (Patternexpression != null)
            return "WHERE " + Patternexpression.toCypher();
        else
            return "WHERE "+expression.toCypher();
    }

    @Override
    public boolean validate() {
        return true;
    }
    public static WhereClause generateRandomWhereClauseChose(GraphManager graphManager,Map<String, Object> varToProperties){
        List<AbstractNode> nodesWithRelationships = graphManager.getNodes().stream()
                .filter(node -> !node.getRelationships().isEmpty())
                .collect(Collectors.toList());
        //如果不为空可以模式匹配
        if (!nodesWithRelationships.isEmpty())
            return WhereClause.generateRandomWhereClauseByPattern(graphManager);
        return WhereClause.generateRandomWhereClause(varToProperties);
    }
    public static WhereClause generateRandomWhereClauseByPattern(GraphManager graphManager){
        Set<AbstractRelationship> visitedRelationships = new HashSet<>();
        SimplePathPatternClause simplePathPattern = SimplePathPatternClause.generateRandomSimplePathPatternInWhere(graphManager.Copy(),visitedRelationships);
        System.out.println(simplePathPattern.toCypher());
        return new WhereClause(simplePathPattern);
    }
    public static WhereClause generateRandomWhereClause(Map<String, Object> varToProperties) {
        Randomly randomly = new Randomly();
        int depth = randomly.getInteger(0, 5);
        //depth = 1;
        RandomExpressionGenerator expressionGenerator = new RandomExpressionGenerator(varToProperties);
        IExpression expression1 = expressionGenerator.generateCondition(depth);
        System.out.println(expression1.toCypher());//debug
        return new WhereClause(expression1);

    }
    /*public static WhereClause generateEmptyWhereClause(){
        return new WhereClause(new BooleanExpression(true),true)*/
}
