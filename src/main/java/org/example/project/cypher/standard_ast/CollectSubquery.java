package org.example.project.cypher.standard_ast;

import org.example.project.cypher.gen.GraphManager;
import org.example.project.cypher.standard_ast.expr.ExprUnknownVal;

import java.util.List;

public class CollectSubquery extends Subqueries {
    private final RootClause collectClause;  // 子查询的根语句（如 WHERE, MATCH 等）

    private final String alias;

    public CollectSubquery(RootClause collectClause,String alias) {
        super("COLLECT");
        this.collectClause = collectClause;
        this.alias=alias;
    }

    public String getAlias(){return  alias;}

    @Override
    public String toCypher() {
        if(alias=="") return "";
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("COLLECT{");
        sb.append(collectClause.toCypher());
        sb.append("} AS ");
        sb.append(alias);
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public boolean validate() {
       /* List<Clause> clauses = collectClause.getClauses();
        // 双重校验：1. 是否存在子句列表 2. 最后一个子句是否非空
        return clauses != null &&
                !clauses.isEmpty() &&
                clauses.get(clauses.size() - 1) != null;*/
        return true;
    }

    public static CollectSubquery generateCollectSubquery(GraphManager parentManager) {
        GraphManager subGraphManager = parentManager.Copy();  // 复制父查询的上下文
        RootClause collectClause = RootClause.generateCollectRootClause(subGraphManager);
        List<Clause> clauses = collectClause.getClauses();
        if(clauses.get(clauses.size() - 1) == null) return null;
        // 注册别名并清理其他变量
        String alias = parentManager.getAsVariableManager().addAlias(ExprUnknownVal.UNKNOWN_LIST);
        return new CollectSubquery(collectClause,alias);
    }



    // 可选：获取根语句（用于动态修改子查询结构）
    public RootClause getCollectClause() {
        return collectClause;
    }
}