package org.example.project.cypher.standard_ast;

import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.example.project.Randomly;
import org.example.project.cypher.ast.IExpression;
import org.example.project.cypher.gen.RandomExpressionGenerator;
import org.example.project.cypher.standard_ast.expr.CypherExpression;

import java.util.Map;

public class WhereClause extends Clause {

    private final IExpression expression;

    public WhereClause(IExpression expression) {
        super("Where");
        this.expression = expression;
    }

    @Override
    public String toCypher() {
        return expression.toCypher();
    }

    @Override
    public boolean validate() {
        return true;
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
