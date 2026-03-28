package org.example.project.cypher.standard_ast;

import org.example.project.cypher.gen.*;
import org.example.project.Randomly;
import org.example.project.cypher.standard_ast.expr.ExprUnknownVal;
import java.util.*;

/**
An EXISTS subquery can appear anywhere in a query that an expression is valid.

Any variable that is defined in the outside scope can be referenced inside the subquery’s own scope.

Variables introduced inside the subquery are not part of the outside scope and therefore cannot be accessed on the outside.
*/

public class ExistSubQuery extends Subqueries {
    private final RootClause existRootClause;

    public ExistSubQuery(RootClause existRootClause) {
        super("ExistSubQuery");
        this.existRootClause = existRootClause;
    }

    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();
        sb.append(" EXISTS {");

        sb.append("\n").append(existRootClause.toCypher()).append("\n}");
        return sb.toString();
    }

    @Override
    public boolean validate() {return true; }

    public static ExistSubQuery generateExistSubQuery(GraphManager parentManager) {
        GraphManager SubGraphManager = parentManager.Copy();
        RootClause existRootClause=RootClause.generateExistRootClause(SubGraphManager);
        return new ExistSubQuery(existRootClause);
    }

}