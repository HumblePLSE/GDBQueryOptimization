package org.example.project.cypher.standard_ast;

import org.example.project.cypher.gen.GraphManager;

public class CountSubquery extends Subqueries {
    private final RootClause countClause; // 子查询的根语句（如 WHERE, MATCH 等）

    public CountSubquery(RootClause countClause) {
        super("COUNT");
        this.countClause=countClause;
    }


    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();
        //sb.append("\n");
        sb.append(" COUNT{");
        sb.append(countClause.toCypher());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean validate() {
        return true;
    }

    public static CountSubquery generateCountSubquery(GraphManager parentManager)
    {
        GraphManager SubGraphManager = parentManager.Copy();
        RootClause CountClause=RootClause.generateCountRootClause(SubGraphManager);
        return new CountSubquery(CountClause);

    }
}
