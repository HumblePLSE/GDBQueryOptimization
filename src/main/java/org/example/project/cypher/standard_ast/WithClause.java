package org.example.project.cypher.standard_ast;

import java.util.*;

import org.example.project.cypher.gen.*;
import org.example.project.Randomly;
import org.example.project.cypher.schema.IPropertyInfo;
import org.example.project.cypher.standard_ast.expr.ExprUnknownVal;

/**
 * Represents a WITH clause in Cypher queries.
 * Supports carrying over context variables to the next query part.
 */
public class WithClause extends ProjectingClause {

    private final List<ContextVariable> contextVariables; // List of context variables, each with a name and optional alias.
    private final boolean useWildcard;          // Indicates if the wildcard (*) is used

    private final boolean distinct;

    private final CollectSubquery collectSubquery;

    private final WhereClause whereClause;

    private final ExistSubQuery existSubQuery;

    private final CountSubquery countSubQuery;

    private final List<ReadingSubClause> readingSubClauses; // List of reading sub-clauses like ORDER BY, LIMIT, SKIP, WHERE

    public WithClause(List<ContextVariable> contextVariables, boolean useWildcard, WhereClause whereClause, List<ReadingSubClause> readingSubClauses, boolean distinct, CollectSubquery collectSubquery,ExistSubQuery existSubQuery,CountSubquery countSubquery) {
        super("WITH");
        this.contextVariables = contextVariables != null ? contextVariables : new ArrayList<>();
        this.useWildcard = useWildcard;
        this.whereClause = whereClause;
        this.readingSubClauses = readingSubClauses;
        this.distinct = distinct;
        this.collectSubquery = collectSubquery;
        this.existSubQuery=existSubQuery;
        this.countSubQuery=countSubquery;
    }

    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();
        if (contextVariables.isEmpty()) return "WITH * ";
        sb.append("WITH ");

        if (distinct) sb.append("DISTINCT ");
        if (useWildcard) {
            sb.append("* ");  // Use wildcard if the flag is true
        }

        for (int i = 0; i < contextVariables.size(); i++) {
            if (i == 0 && useWildcard) sb.append(",");
            ContextVariable contextVariable = contextVariables.get(i);
            sb.append(contextVariable.getVariableName());  // Variable name

            if (contextVariable.getAsClause() != null) {
                sb.append(" ").append(contextVariable.getAsClause().toCypher());  // Add alias if present
            }

            if (i < contextVariables.size() - 1) {
                sb.append(", ");
            }
        }

        if (collectSubquery != null) sb.append(",").append(collectSubquery.toCypher());

        if (readingSubClauses != null) {//有必要？
            // Append the reading sub-clauses like ORDER BY, LIMIT, SKIP, WHERE
            for (ReadingSubClause subClause : readingSubClauses) {
                sb.append(" ").append(subClause.toCypher());
            }

        }

        boolean firstPredicate = true;
        if (whereClause != null) {
            if (firstPredicate) sb.append(" WHERE ");
            firstPredicate = false;
            sb.append(" ").append(whereClause.toCypher());
        }
        if (existSubQuery != null) {
            if (firstPredicate) sb.append(" WHERE ");
            if (!firstPredicate) sb.append(" AND ");
            firstPredicate = false;
            sb.append(" ").append(existSubQuery.toCypher());
        }
        if (countSubQuery != null) {
            if (firstPredicate) sb.append(" WHERE ");
            if (!firstPredicate) sb.append(" AND ");
            firstPredicate = false;
            sb.append(" ").append(countSubQuery.toCypher());
            sb.append(" >= 0 ");
        }


        return sb.toString();
    }

    @Override
    public boolean validate() {
        return useWildcard || !contextVariables.isEmpty();
    }

    public static WithClause generateRandomWithClauseNew(GraphManager graphManager) {
        Randomly randomly = new Randomly();
        boolean useWildcard = randomly.getInteger(0, 4) == 0; // Randomly decide whether to use * (wildcard) 25%

        boolean distinct = randomly.getInteger(0, 4) == 0;
        //useWildcard=false;//debug


        List<ContextVariable> contextVariables = new ArrayList<>();


        // Include node variables
        NodeVariableManager nodeVariableManager = graphManager.getNodeVariableManager();
        List<String> nodeVariables = new ArrayList<>(nodeVariableManager.getAllNodeVariables());

        // Include relationship variables
        RelationshipVariableManager relationshipVariableManager = graphManager.getRelationshipVariableManager();
        List<String> relationshipVariables = new ArrayList<>(relationshipVariableManager.getAllRelationshipVariables());

        // Include path variables
        PathVariableManager pathVariableManager = graphManager.getPathVariableManager();
        List<String> pathVariables = new ArrayList<>(pathVariableManager.getAllPathVariables());

        AsVariableManager asVariableManager = graphManager.getAsVariableManager();
        List<String> aliasVariables = new ArrayList<>(asVariableManager.getAllVariableNames());


        // If all variable sources are empty, fall back to wildcard
        if (nodeVariables.isEmpty() && relationshipVariables.isEmpty() && pathVariables.isEmpty() && aliasVariables.isEmpty()) {
            return new WithClause(null, true, null, null, distinct, null,null,null);  //
        }

        String collectAlias = new String();
        CollectSubquery collectSubquery = null;
        if (randomly.getInteger(0,5)==0) collectSubquery = CollectSubquery.generateCollectSubquery(graphManager);
        if (collectSubquery != null) {
            collectAlias = collectSubquery.getAlias();
            asVariableManager.removeAlias(collectAlias);
        }


        // Randomly select a smaller subset of variables from each source
        List<String> selectedNodeVariables = randomlySelectSubset(nodeVariables, randomly);
        List<String> selectedRelationshipVariables = randomlySelectSubset(relationshipVariables, randomly);
        List<String> selectedPathVariables = randomlySelectSubset(pathVariables, randomly);
        List<String> selectedAsVariables = randomlySelectSubset(aliasVariables, randomly);

        if (!useWildcard) asVariableManager.retainVariables(selectedAsVariables);
        contextVariables.addAll(createAliasesContextVariables(selectedAsVariables, graphManager));
        if (!useWildcard) {
            nodeVariableManager.retainVariables(selectedNodeVariables);
            relationshipVariableManager.retainVariables(selectedRelationshipVariables);
            pathVariableManager.retainVariables(selectedPathVariables);
        }

        List<String> removeNodeVariables = new ArrayList<>();
        List<String> removeRelationshipVariables = new ArrayList<>();
        //List<String> removePathVariables = new ArrayList<>();

        // 遍历所有节点变量
        for (String variable : selectedNodeVariables) {
            AsClause asClause = null;
            if (randomly.getBoolean()) {  // 50% 的概率生成别名
                asClause = AsClause.generateRandomAsClause(graphManager.getNodeVariableManager().getNodeVariable(variable), graphManager);
                removeNodeVariables.add(variable);
                //graphManager.getNodeVariableManager().removeNodeVariable(variable);
            }
            contextVariables.add(new ContextVariable(variable, asClause));  // 将生成的 ContextVariable 添加到列表
        }

        // 遍历所有关系变量
        for (String variable : selectedRelationshipVariables) {
            AsClause asClause = null;
            if (randomly.getBoolean()) {  // 50% 的概率生成别名
                // 生成随机别名并移除原始变量
                asClause = AsClause.generateRandomAsClause(graphManager.getRelationshipVariableManager().getRelationship(variable), graphManager);
                //graphManager.getRelationshipVariableManager().removeRelationshipVariable(variable);  // 删除原始变量
                removeRelationshipVariables.add(variable);
            }
            // 将生成的 ContextVariable 添加到列表
            contextVariables.add(new ContextVariable(variable, asClause));
        }


        for (String variable : selectedPathVariables) {
            AsClause asClause = null;
            if (randomly.getBoolean()) {  // 50% 的概率生成别名
                // 生成随机别名并移除原始变量
                asClause = AsClause.generateRandomAsClause(graphManager.getPathVariableManager().getPathVariable(variable), graphManager);
                if (!useWildcard) graphManager.getPathVariableManager().removePathVariable(variable);  // 删除原始变量
            }
            // 将生成的 ContextVariable 添加到列表
            contextVariables.add(new ContextVariable(variable, asClause));
        }

        // Combine the selected variables into contextVariables
        //contextVariables.addAll(createNodeContextVariables(selectedNodeVariables, graphManager));
        //contextVariables.addAll(createRelationshipContextVariables(selectedRelationshipVariables, graphManager));
        //contextVariables.addAll(createPathContextVariables(selectedPathVariables, graphManager));


        if (collectSubquery!=null) asVariableManager.putAlias(collectAlias, ExprUnknownVal.UNKNOWN_LIST);

        //System.out.println(collectSubquery.toCypher());

        List<ReadingSubClause> readingSubClauses = new ArrayList<>();

        // Optionally add ORDER BY, LIMIT, SKIP based on some conditions
        if (randomly.getBoolean()) {
            List<String> orderbyList = new ArrayList<>();
            orderbyList.addAll(extractRandomNodeAndRelationshipAttributes(contextVariables, graphManager));

            readingSubClauses.add(OrderByClause.generateRandomOrderByClause(orderbyList));
        }
        if (randomly.getBoolean()) {
            readingSubClauses.add(SkipClause.generateRandomSkipClause());
        }
        if (randomly.getBoolean()) {
            readingSubClauses.add(LimitClause.generateRandomLimitClause());
        }

        if (!useWildcard) {
            nodeVariableManager.removeNodeVariables(removeNodeVariables);
            relationshipVariableManager.removeRelationshipVariables(removeRelationshipVariables);
        }

        WhereClause whereClause = null;
        ExistSubQuery existSubQuery = null;
        CountSubquery countSubquery = null;
        //if (randomly.getBoolean()) {
        if (randomly.getInteger(0,100)<90) {//change probability
            Map<String, Object> varToProperties = graphManager.extractVartoProperties();
            Map<String, Object> varToFunctions = graphManager.extractIdFunctionMap();
            Map<String, Object> aliasProperties = graphManager.extractAliasProperties();
            Map<String, Object> combinedProperties = new HashMap<>();
            combinedProperties.putAll(varToProperties); // 将第一个 Map 的内容放入
            combinedProperties.putAll(aliasProperties); // 将第二个 Map 的内容放入
            combinedProperties.putAll(varToFunctions);

            whereClause = WhereClause.generateRandomWhereClause(combinedProperties);
        }

        if (randomly.getInteger(0, 7) == 0) {
            existSubQuery = ExistSubQuery.generateExistSubQuery(graphManager);
            //System.out.println(existSubQuery.toCypher());
        }
        if (randomly.getInteger(0, 7) == 0) {
            countSubquery = CountSubquery.generateCountSubquery(graphManager);
            //System.out.println(countSubquery.toCypher());
        }

        return new WithClause(contextVariables, useWildcard, whereClause, readingSubClauses, distinct, collectSubquery,existSubQuery,countSubquery); // Return the selected variables

    }

    /**
     * Generate a basic WithClause that carries over context variables.
     *
     * @param graphManager The GraphManager to get variables from.
     * @return A WithClause object.
     */
    public static WithClause generateRandomWithClause(GraphManager graphManager) {
        Randomly randomly = new Randomly();
        boolean useWildcard = randomly.getBoolean(); // Randomly decide whether to use * (wildcard)
        boolean distinct = randomly.getBoolean();
        useWildcard = false;//debug

        if (useWildcard) {
            return new WithClause(null, true, null, null, distinct, null,null,null); // Use wildcard *
        } else {
            List<ContextVariable> contextVariables = new ArrayList<>();

            // Include node variables
            NodeVariableManager nodeVariableManager = graphManager.getNodeVariableManager();
            List<String> nodeVariables = new ArrayList<>(nodeVariableManager.getAllNodeVariables());

            // Include relationship variables
            RelationshipVariableManager relationshipVariableManager = graphManager.getRelationshipVariableManager();
            List<String> relationshipVariables = new ArrayList<>(relationshipVariableManager.getAllRelationshipVariables());

            // Include path variables
            PathVariableManager pathVariableManager = graphManager.getPathVariableManager();
            List<String> pathVariables = new ArrayList<>(pathVariableManager.getAllPathVariables());

            AsVariableManager asVariableManager = graphManager.getAsVariableManager();
            List<String> aliasVariables = new ArrayList<>(asVariableManager.getAllVariableNames());


            // If all variable sources are empty, fall back to wildcard
            if (nodeVariables.isEmpty() && relationshipVariables.isEmpty() && pathVariables.isEmpty() && aliasVariables.isEmpty()) {
                return new WithClause(null, true, null, null, distinct, null,null,null);  // Use wildcard *
            }

            // Randomly select a smaller subset of variables from each source
            List<String> selectedNodeVariables = randomlySelectSubset(nodeVariables, randomly);
            List<String> selectedRelationshipVariables = randomlySelectSubset(relationshipVariables, randomly);
            List<String> selectedPathVariables = randomlySelectSubset(pathVariables, randomly);
            List<String> selectedAsVariables = randomlySelectSubset(aliasVariables, randomly);

            nodeVariableManager.retainVariables(selectedNodeVariables);
            relationshipVariableManager.retainVariables(selectedRelationshipVariables);
            pathVariableManager.retainVariables(selectedPathVariables);
            asVariableManager.retainVariables(selectedAsVariables);


            // Combine the selected variables into contextVariables
            contextVariables.addAll(createNodeContextVariables(selectedNodeVariables, graphManager));
            contextVariables.addAll(createRelationshipContextVariables(selectedRelationshipVariables, graphManager));
            contextVariables.addAll(createPathContextVariables(selectedPathVariables, graphManager));
            //contextVariables.addAll(createAliasesContextVariables(selectedAsVariables, graphManager));


            CollectSubquery collectSubquery = CollectSubquery.generateCollectSubquery(graphManager);

            List<ReadingSubClause> readingSubClauses = new ArrayList<>();

            // Optionally add ORDER BY, LIMIT, SKIP based on some conditions
            if (randomly.getBoolean())
            // if(1>0)//debug
            {
                List<String> orderbyList = new ArrayList<>();
                orderbyList.addAll(extractRandomNodeAndRelationshipAttributes(contextVariables, graphManager));

                readingSubClauses.add(OrderByClause.generateRandomOrderByClause(orderbyList));
            }
            if (randomly.getBoolean()) {
                readingSubClauses.add(SkipClause.generateRandomSkipClause());
            }
            if (randomly.getBoolean()) {
                readingSubClauses.add(LimitClause.generateRandomLimitClause());
            }


            return new WithClause(contextVariables, false, null, readingSubClauses, distinct, null,null,null); // Return the selected variables
        }
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
        int maxSelection = Math.min(variables.size(), randomly.getInteger(1, 3)); // Select up to 3 variables
        Collections.shuffle(variables); // Randomly shuffle the list
        for (int i = 0; i < maxSelection; i++) {
            subset.add(variables.get(i));
        }
        return subset;
    }

    //随机觉得是否对传递的节点变量生成别名
    private static List<ContextVariable> createNodeContextVariables(List<String> nodeVariables, GraphManager graphManager) {
        Randomly randomly = new Randomly();
        List<ContextVariable> contextVariables = new ArrayList<>();

        // 遍历所有节点变量
        for (String variable : nodeVariables) {
            AsClause asClause = null;
            if (randomly.getBoolean()) {  // 50% 的概率生成别名
                asClause = AsClause.generateRandomAsClause(graphManager.getNodeVariableManager().getNodeVariable(variable), graphManager);
                graphManager.getNodeVariableManager().removeNodeVariable(variable);
            }
            contextVariables.add(new ContextVariable(variable, asClause));  // 将生成的 ContextVariable 添加到列表
        }
        return contextVariables;
    }

    private static List<ContextVariable> createRelationshipContextVariables(List<String> relationshipVariables, GraphManager graphManager) {
        Randomly randomly = new Randomly();
        List<ContextVariable> contextVariables = new ArrayList<>();

        // 遍历所有关系变量
        for (String variable : relationshipVariables) {
            AsClause asClause = null;
            if (randomly.getBoolean()) {  // 50% 的概率生成别名
                // 生成随机别名并移除原始变量
                asClause = AsClause.generateRandomAsClause(graphManager.getRelationshipVariableManager().getRelationship(variable), graphManager);
                graphManager.getRelationshipVariableManager().removeRelationshipVariable(variable);  // 删除原始变量
            }
            // 将生成的 ContextVariable 添加到列表
            contextVariables.add(new ContextVariable(variable, asClause));
        }
        return contextVariables;
    }

    private static List<ContextVariable> createPathContextVariables(List<String> pathVariables, GraphManager graphManager) {
        Randomly randomly = new Randomly();
        List<ContextVariable> contextVariables = new ArrayList<>();

        // 遍历所有路径变量
        for (String variable : pathVariables) {
            AsClause asClause = null;
            if (randomly.getBoolean()) {  // 50% 的概率生成别名
                // 生成随机别名并移除原始变量
                asClause = AsClause.generateRandomAsClause(graphManager.getPathVariableManager().getPathVariable(variable), graphManager);
                graphManager.getPathVariableManager().removePathVariable(variable);  // 删除原始变量
            }
            // 将生成的 ContextVariable 添加到列表
            contextVariables.add(new ContextVariable(variable, asClause));
        }
        return contextVariables;
    }

    private static List<ContextVariable> createAliasesContextVariables(List<String> aliasVariables, GraphManager graphManager) {
        Randomly randomly = new Randomly();
        List<ContextVariable> contextVariables = new ArrayList<>();
        for (String alias : aliasVariables) {
            // 获取别名目标
            Object aliasTarget = graphManager.getAsVariableManager().getTarget(alias);
            AsClause asClause = null;
            if (randomly.getBoolean()) {
                asClause = AsClause.generateRandomAsClause(aliasTarget, graphManager); // 创建 AsClause
                graphManager.getAsVariableManager().removeAlias(alias);
            }
            contextVariables.add(new ContextVariable(alias, asClause)); // 添加到上下文变量
        }
        // Extract properties for nodes and relationships
        Map<String, Object> varToProperties = graphManager.extractVartoProperties();
        List<String> allProperties = new ArrayList<>(varToProperties.keySet());

        // Select a random subset of properties to create new aliases
        List<String> selectedProperties = randomlySelectSubset(allProperties, randomly);

        for (String propertyname : selectedProperties) {
            Object aliasTargrt = varToProperties.get(propertyname);
            AsClause asClause = AsClause.generateRandomAsClause(aliasTargrt, graphManager);
            contextVariables.add(new ContextVariable(propertyname, asClause)); // 添加到上下文变量
        }

        //select a random subset of node,relationship and path function to join asvariablemanager
        Map<String, Object> varToFunction = graphManager.extractTargetFunctionMap();
        List<String> allFunction = new ArrayList<>(varToFunction.keySet());

        // Select a random subset of properties to create new aliases
        List<String> selectedFunctions = randomlySelectSubset(allFunction, randomly);
        // selectedFunctions=allFunction;//debug

        for (String functionname : selectedFunctions) {
            Object aliasTargrt = varToFunction.get(functionname);
            AsClause asClause = AsClause.generateRandomAsClause(aliasTargrt, graphManager);
            contextVariables.add(new ContextVariable(functionname, asClause)); // 添加到上下文变量
        }

        return contextVariables;


    }


    /**
     * Helper method to extract node and relationship attributes from context variables.
     *
     * @param contextVariables The list of context variables.
     * @return A list of attributes to use in ORDER BY clause.
     */
    private static List<String> extractRandomNodeAndRelationshipAttributes(List<ContextVariable> contextVariables, GraphManager graphManager) {
        Randomly randomly = new Randomly();
        List<String> attributes = new ArrayList<>();

        // 从 schema 中获取所有可用的 properties
        List<IPropertyInfo> availableProperties = graphManager.getSchema().getProperties();
        Collections.shuffle(availableProperties);  // 随机打乱属性列表

        // 遍历所有的 contextVariable
        for (ContextVariable contextVariable : contextVariables) {
            AsClause asClause = contextVariable.getAsClause();

            // 如果有 asClause，则处理对应的 alias
            if (asClause != null) {
                String alias = asClause.getAsString();  // 获取 alias 名称
                Object aliasTarget = graphManager.getAsVariableManager().getTarget(alias);  // 通过 alias 获取 AliasTarget

                // 对节点进行处理
                if (aliasTarget instanceof AbstractNode) {
                    int selectPropertynum = randomly.getInteger(0, 3); // 随机选择属性数量

                    // 随机选择属性并拼接成 alias.propertyname
                    for (int i = 0; i < Math.min(selectPropertynum, availableProperties.size()); i++) {
                        IPropertyInfo property = availableProperties.get(i);
                        attributes.add(alias + "." + property.getKey());  // 拼接成 alias.propertyname 形式
                    }
                }
                // 对关系进行处理
                else if (aliasTarget instanceof AbstractRelationship) {
                    int selectPropertynum = randomly.getInteger(0, 3); // 随机选择属性数量

                    // 随机选择属性并拼接成 alias.propertyname
                    for (int i = 0; i < Math.min(selectPropertynum, availableProperties.size()); i++) {
                        IPropertyInfo property = availableProperties.get(i);
                        attributes.add(alias + "." + property.getKey());  // 拼接成 alias.propertyname 形式
                    }
                }
            }
        }

        // 获取所有的节点变量
        NodeVariableManager nodeVariableManager = graphManager.getNodeVariableManager();
        List<String> nodeVariables = nodeVariableManager.getAllNodeVariables();
        for (String nodeVariable : nodeVariables) {
            int selectPropertynum = randomly.getInteger(0, 3); // 随机选择属性数量
            // 随机选择属性并拼接成 nodeVariable.propertyname
            for (int i = 0; i < Math.min(selectPropertynum, availableProperties.size()); i++) {
                IPropertyInfo property = availableProperties.get(i);
                attributes.add(nodeVariable + "." + property.getKey());  // 拼接成 nodeVariable.propertyname 形式
            }
        }

        // 获取所有的关系变量
        RelationshipVariableManager relationshipVariableManager = graphManager.getRelationshipVariableManager();
        List<String> relationshipVariables = relationshipVariableManager.getAllRelationshipVariables();
        for (String relationshipVariable : relationshipVariables) {
            int selectPropertynum = randomly.getInteger(0, 3); // 随机选择属性数量
            // 随机选择属性并拼接成 relationshipVariable.propertyname
            for (int i = 0; i < Math.min(selectPropertynum, availableProperties.size()); i++) {
                IPropertyInfo property = availableProperties.get(i);
                attributes.add(relationshipVariable + "." + property.getKey());  // 拼接成 relationshipVariable.propertyname 形式
            }
        }

        return attributes;
    }


    // Getter and Setter for contextVariables and useWildcard
    public List<ContextVariable> getContextVariables() {
        return contextVariables;
    }

    public boolean isUseWildcard() {
        return useWildcard;
    }


    // Inner class to represent each context variable, with or without an alias
    public static class ContextVariable {
        private String variableName;
        private AsClause asClause;

        public ContextVariable(String variableName, AsClause asClause) {
            this.variableName = variableName;
            this.asClause = asClause;
        }

        public String getVariableName() {
            return variableName;
        }

        public AsClause getAsClause() {
            return asClause;
        }
    }
}
