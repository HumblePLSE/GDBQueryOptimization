package org.example.project.cypher.standard_ast;
import org.example.project.Randomly;
import org.example.project.cypher.gen.AbstractRelationship;
import org.example.project.cypher.gen.GraphManager;
import org.example.project.cypher.gen.PathVariableManager;

import java.util.Set;

/**
 * Represents a PathPattern in Cypher queries.
 *
 * pathPattern ::= [ pathVariableDeclaration ] simplePathPattern
 */
public class PathPatternClause extends Clause {

    private final String pathVariable;       // 可选的路径变量
    private final SimplePathPatternClause simplePathPattern; // 简单路径模式
    //private final String shortestPath;   //shortestPath函数

    public PathPatternClause(String pathVariable, SimplePathPatternClause simplePathPattern) {
        super("PathPattern");
        this.pathVariable = pathVariable;
        this.simplePathPattern = simplePathPattern;
    }

    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();
        if (pathVariable != null) {
            sb.append(pathVariable).append(" = ");
        }

        sb.append(simplePathPattern.toCypher());

        return sb.toString();
    }

    @Override
    public boolean validate() {
        return simplePathPattern != null && simplePathPattern.validate();
    }

    public static PathPatternClause generateRandomPathPattern(GraphManager graphManager, Set<AbstractRelationship> visitedRelationships,boolean PossiblePathVariable) {
        Randomly randomly=new Randomly();
        String pathVariable = null;
        if(randomly.getInteger(0,4)==0&&PossiblePathVariable){
            PathVariableManager pathVariableManager=graphManager.getPathVariableManager();
            pathVariable=pathVariableManager.generatePathVariable();
        }
        String shortestPath=null;


        SimplePathPatternClause simplePathPattern = SimplePathPatternClause.generateRandomSimplePathPattern(graphManager,visitedRelationships);
        return new PathPatternClause(pathVariable, simplePathPattern);
    }

    public static PathPatternClause generateRandomCreatePathPattern(GraphManager graphManager, Set<AbstractRelationship> visitedRelationships) {
        Randomly randomly=new Randomly();
        String pathVariable = randomly.getInteger(0,4)==0 ?  graphManager.getPathVariableManager().generatePathVariable(): null;
        SimplePathPatternClause simplePathPattern = SimplePathPatternClause.generateCreatePathPattern(graphManager,visitedRelationships);
        return new PathPatternClause(pathVariable, simplePathPattern);
    }
}
