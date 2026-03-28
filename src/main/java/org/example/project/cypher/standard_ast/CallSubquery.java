package org.example.project.cypher.standard_ast;

import org.example.project.cypher.gen.*;
import org.example.project.Randomly;
import org.example.project.cypher.standard_ast.expr.ExprUnknownVal;

import java.util.*;

public class CallSubquery extends Subqueries {
    private final List<String> importedVariables; // CALL (var1, var2) 中的变量
    private final RootClause subQuery;             // 子查询结构体

    private final boolean useWildcard; //是否使用通配符传递所有变量

    public CallSubquery(List<String> importedVariables, RootClause subQuery, boolean useWildcard) {
        super("CALL");
        this.importedVariables = importedVariables;
        this.subQuery = subQuery;
        this.useWildcard = useWildcard;
    }

    @Override
    public String toCypher() {
        String imports = !importedVariables.isEmpty() ?
                String.join(", ", importedVariables) : "";
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("CALL");
        sb.append("{");
        if (useWildcard) sb.append("WITH * ");
        else if(imports!="") sb.append("WITH ").append(imports).append(" ");
        sb.append(subQuery.toCypher()).append("}");
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public boolean validate() {
        return true;
    }

    /**
     * 生成 CALL 子查询
     *
     * @param parentManager 父级上下文管理器
     */
    public static CallSubquery generateCallSubquery(GraphManager parentManager,Boolean hasWriting) {
        Randomly randomly = new Randomly();
        boolean useWildcard = randomly.getInteger(0, 5) == 0; // Randomly decide whether to use * (wildcard) 20%
        GraphManager SubGraphManager = parentManager.Copy();
        List<String> importedVariables = new ArrayList<>();
        /*AbstractNode node=new AbstractNode();
        SubGraphManager.getNodeVariableManager().generateNodeVariable(node);*/

        // ------------------------- 步骤1：确定要导入的变量 -------------------------
        if (!useWildcard) {
            /*// Include node variables
            NodeVariableManager nodeVariableManager = parentManager.getNodeVariableManager();
            List<String> nodeVariables = new ArrayList<>(nodeVariableManager.getAllNodeVariables());

            // Include relationship variables
            RelationshipVariableManager relationshipVariableManager = parentManager.getRelationshipVariableManager();
            List<String> relationshipVariables = new ArrayList<>(relationshipVariableManager.getAllRelationshipVariables());

            // Include path variables
            PathVariableManager pathVariableManager = parentManager.getPathVariableManager();
            List<String> pathVariables = new ArrayList<>(pathVariableManager.getAllPathVariables());

            AsVariableManager asVariableManager = parentManager.getAsVariableManager();
            List<String> aliasVariables = new ArrayList<>(asVariableManager.getAllVariableNames());

            // Randomly select a smaller subset of variables from each source
            List<String> selectedNodeVariables = randomlySelectSubset(nodeVariables, randomly);
            List<String> selectedRelationshipVariables = randomlySelectSubset(relationshipVariables, randomly);
            List<String> selectedPathVariables = randomlySelectSubset(pathVariables, randomly);
            List<String> selectedAsVariables = randomlySelectSubset(aliasVariables, randomly);

            importedVariables.addAll(selectedNodeVariables);
            importedVariables.addAll(selectedRelationshipVariables);
            importedVariables.addAll(selectedPathVariables);
            importedVariables.addAll(selectedAsVariables);

            SubGraphManager.getNodeVariableManager().retainVariables(selectedNodeVariables);
            SubGraphManager.getRelationshipVariableManager().retainVariables(selectedRelationshipVariables);
            SubGraphManager.getPathVariableManager().retainVariables(selectedPathVariables);
            SubGraphManager.getAsVariableManager().retainVariables(selectedAsVariables);*/

            NodeVariableManager nodeManager = SubGraphManager.getNodeVariableManager();
            for (Map.Entry<String, AbstractNode> entry : new ArrayList<>(nodeManager.getNodeVariableMap().entrySet())) {
                if (randomly.getInteger(0, 4) <= 2) { // 3/4概率移出作用域
                    nodeManager.removeNodeVariable(entry.getKey()); // 从父管理器移除
                } else {
                    importedVariables.add(entry.getKey()); // 保留在子查询作用域
                }
            }

            RelationshipVariableManager relManager = SubGraphManager.getRelationshipVariableManager();
            for (Map.Entry<String, AbstractRelationship> entry : new ArrayList<>(relManager.getRelationshipVariableMap().entrySet())) {
                if (randomly.getInteger(0, 4) <= 2) {
                    relManager.removeRelationshipVariable(entry.getKey());
                } else {
                    importedVariables.add(entry.getKey());
                }
            }

            PathVariableManager pathManager = SubGraphManager.getPathVariableManager();
            for (Map.Entry<String, AbstractPath> entry : new ArrayList<>(pathManager.getPathVariableMap().entrySet())) {
                if (randomly.getInteger(0, 4) <= 2) {
                    pathManager.removePathVariable(entry.getKey());
                } else {
                    importedVariables.add(entry.getKey());
                }
            }

            AsVariableManager aliasManager = SubGraphManager.getAsVariableManager();
            for (Map.Entry<String, AsVariableManager.AliasTarget> entry : new ArrayList<>(aliasManager.getAllAliases().entrySet())) {
                if (randomly.getInteger(0, 4) <= 2) {
                    aliasManager.removeAlias(entry.getKey());
                } else {
                    importedVariables.add(entry.getKey());
                }
            }

        }

        // ------------------------- 步骤2：生成子查询内容 -------------------------
        RootClause rootClause = RootClause.generateSubRootClause(SubGraphManager,hasWriting);
        System.out.println(rootClause.toCypher());

        // ------------------------- 步骤3：导出变量 -------------------------
        parentManager.getNodeVariableManager().addVariables(SubGraphManager.getNodeVariableManager().getNodeVariableMap());
        parentManager.getNodeVariableManager().setIndex(SubGraphManager.getNodeVariableManager().getNodeindex());
        parentManager.getRelationshipVariableManager().addVariables(SubGraphManager.getRelationshipVariableManager().getRelationshipVariableMap());
        parentManager.getRelationshipVariableManager().setIndex(SubGraphManager.getRelationshipVariableManager().getRelationshipIndex());
        parentManager.getPathVariableManager().addVariables(SubGraphManager.getPathVariableManager().getPathVariableMap());
        parentManager.getPathVariableManager().setIndex(SubGraphManager.getPathVariableManager().getPathIndex());
        parentManager.getAsVariableManager().addVariables(SubGraphManager.getAsVariableManager().getAllAliases());
        parentManager.getAsVariableManager().setIndex(SubGraphManager.getAsVariableManager().getAliasindex());


        return new CallSubquery(importedVariables, rootClause, useWildcard);
    }

    /**
     * 生成 CALL 子查询
     *
     * @param parentManager 父级上下文管理器
     */
    public static CallSubquery generateCallSubqueryNew(GraphManager parentManager) {
        Randomly randomly = new Randomly();
        boolean useWildcard = randomly.getInteger(0, 5) == 0; // Randomly decide whether to use * (wildcard) 20%

        List<String> importedVariables = new ArrayList<>();

        // ------------------------- 临时存储被移除的变量 -------------------------
        Map<String, AbstractNode> removedNodes = new HashMap<>();
        Map<String, AbstractRelationship> removedRels = new HashMap<>();
        Map<String, AbstractPath> removedPaths = new HashMap<>();
        Map<String, AsVariableManager.AliasTarget> removedAliases = new HashMap<>();

        // ------------------------- 步骤1：确定要导入的变量 -------------------------
        if (!useWildcard) {
            NodeVariableManager nodeManager = parentManager.getNodeVariableManager();
            for (Map.Entry<String, AbstractNode> entry : new ArrayList<>(nodeManager.getNodeVariableMap().entrySet())) {
                if (randomly.getInteger(0, 4) <= 3) { // 3/4概率移出作用域
                    removedNodes.put(entry.getKey(), entry.getValue());
                    nodeManager.removeNodeVariable(entry.getKey()); // 从父管理器移除
                } else {
                    importedVariables.add(entry.getKey()); // 保留在子查询作用域
                }
            }

            RelationshipVariableManager relManager = parentManager.getRelationshipVariableManager();
            for (Map.Entry<String, AbstractRelationship> entry : new ArrayList<>(relManager.getRelationshipVariableMap().entrySet())) {
                if (randomly.getInteger(0, 4) <= 3) {
                    removedRels.put(entry.getKey(), entry.getValue());
                    relManager.removeRelationshipVariable(entry.getKey());
                } else {
                    importedVariables.add(entry.getKey());
                }
            }

            PathVariableManager pathManager = parentManager.getPathVariableManager();
            for (Map.Entry<String, AbstractPath> entry : new ArrayList<>(pathManager.getPathVariableMap().entrySet())) {
                if (randomly.getInteger(0, 4) <= 3) {
                    removedPaths.put(entry.getKey(), entry.getValue());
                    pathManager.removePathVariable(entry.getKey());
                } else {
                    importedVariables.add(entry.getKey());
                }
            }

            AsVariableManager aliasManager = parentManager.getAsVariableManager();
            for (Map.Entry<String, AsVariableManager.AliasTarget> entry : new ArrayList<>(aliasManager.getAllAliases().entrySet())) {
                if (randomly.getInteger(0, 4) <= 3) {
                    removedAliases.put(entry.getKey(), entry.getValue());
                    aliasManager.removeAlias(entry.getKey());
                } else {
                    importedVariables.add(entry.getKey());
                }
            }
        } else {
            NodeVariableManager nodeManager = parentManager.getNodeVariableManager();
            for (Map.Entry<String, AbstractNode> entry : new ArrayList<>(nodeManager.getNodeVariableMap().entrySet())) {
                if (randomly.getInteger(0, 4) <= 0)  // 3/4概率移出作用域
                    importedVariables.add(entry.getKey()); // 保留在子查询作用域
            }

            RelationshipVariableManager relManager = parentManager.getRelationshipVariableManager();
            for (Map.Entry<String, AbstractRelationship> entry : new ArrayList<>(relManager.getRelationshipVariableMap().entrySet())) {
                if (randomly.getInteger(0, 4) <= 0) {
                    importedVariables.add(entry.getKey());
                }
            }

            PathVariableManager pathManager = parentManager.getPathVariableManager();
            for (Map.Entry<String, AbstractPath> entry : new ArrayList<>(pathManager.getPathVariableMap().entrySet())) {
                if (randomly.getInteger(0, 4) <= 0) {
                    importedVariables.add(entry.getKey());
                }
            }

            AsVariableManager aliasManager = parentManager.getAsVariableManager();
            for (Map.Entry<String, AsVariableManager.AliasTarget> entry : new ArrayList<>(aliasManager.getAllAliases().entrySet())) {
                if (randomly.getInteger(0, 4) <= 0) {
                    importedVariables.add(entry.getKey());
                }
            }
        }

        // ------------------------- 步骤2：生成子查询内容 -------------------------
        RootClause rootClause = RootClause.generateSubRootClause(parentManager,false);
        System.out.println(rootClause.toCypher());

        // ------------------------- 步骤3：导出变量 -------------------------
        if (!useWildcard) {
            parentManager.getNodeVariableManager().addVariables(removedNodes);
            parentManager.getRelationshipVariableManager().addVariables(removedRels);
            parentManager.getPathVariableManager().addVariables(removedPaths);
            parentManager.getAsVariableManager().addVariables(removedAliases);
        }

        return new CallSubquery(importedVariables, rootClause, useWildcard);
    }

    /**
     * Helper method to randomly select a subset of variables.
     *
     * @param variables The list of variables to select from.
     * @param randomly  A Randomly instance to use for randomization.
     * @return A randomly selected subset of variables.
     */
    private static List<String> randomlySelectSubset(List<String> variables, Randomly randomly) {
        List<String> subset = new ArrayList<>();
        int maxSelection = Math.min(variables.size(), randomly.getInteger(0, 3)); // Select up to 3 variables
        Collections.shuffle(variables); // Randomly shuffle the list
        for (int i = 0; i < maxSelection; i++) {
            subset.add(variables.get(i));
        }
        return subset;
    }

    // Getter 方法
    public List<String> getImportedVariables() {
        return importedVariables;
    }

    public RootClause getSubQuery() {
        return subQuery;
    }
}