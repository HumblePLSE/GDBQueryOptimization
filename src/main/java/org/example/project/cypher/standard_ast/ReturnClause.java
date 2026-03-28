package org.example.project.cypher.standard_ast;

import org.example.project.Randomly;
import org.example.project.cypher.gen.*;
import org.example.project.cypher.standard_ast.expr.ExprUnknownVal;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a RETURN clause in a Cypher query.
 */
public class ReturnClause extends ProjectingClause {

    private final List<String> returnVariables; // List of variables to return

    private final boolean distinct;

    private final boolean useWildcard;

    private final List<ReadingSubClause> readingSubClauses; // List of reading sub-clauses like ORDER BY, LIMIT, SKIP, WHERE

    public ReturnClause(List<String> returnVariables, boolean distinct, boolean useWildcard, List<ReadingSubClause> readingSubClauses) //
    {
        super("RETURN");
        this.returnVariables = returnVariables;
        this.distinct = distinct;
        this.useWildcard = useWildcard;
        this.readingSubClauses = readingSubClauses;
    }

    @Override
    public String toCypher() {
        if (returnVariables.isEmpty()) {
            //return new FinishClause().toCypher(); // If no variables to return, return a finish clause
            Randomly randomly = new Randomly();
            String s = randomly.getString();
            String identifier = randomly.getRandomCypherIdentifier();
            return "RETURN " + "max(\"" + s + "\")" + " AS " + identifier;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("RETURN ");
        if (distinct) sb.append("DISTINCT ");
        if (useWildcard) sb.append("* ");
        else {
            sb.append(String.join(", ", returnVariables));
            if (readingSubClauses != null) {//有必要？

                for (ReadingSubClause subClause : readingSubClauses) {
                    sb.append(" ").append(subClause.toCypher());
                }

            }
        }
        return sb.toString();
    }

    @Override
    public boolean validate() {
        return !returnVariables.isEmpty(); // Validate that there are variables to return
    }

    /**
     * Generates a random RETURN clause.
     * It selects variables from NodeVariableManager, RelationshipVariableManager, PathVariableManager,
     * AsVariableManager, varToProperties, and varToFunctions.
     *
     * @param graphManager The GraphManager to retrieve variables from.
     * @return A randomly generated ReturnClause.
     */
    public static ReturnClause generateRandomReturnClause(GraphManager graphManager) {
        Randomly randomly = new Randomly();
        boolean distinct = false;
        boolean useWildcard = false;
        NodeVariableManager nodeVariableManager = graphManager.getNodeVariableManager();
        RelationshipVariableManager relationshipVariableManager = graphManager.getRelationshipVariableManager();
        PathVariableManager pathVariableManager = graphManager.getPathVariableManager();

        List<String> returnVariables = new ArrayList<>();

        // Randomly select variables from NodeVariableManager
        List<String> nodeVariables = new ArrayList<>(nodeVariableManager.getAllNodeVariables());
        Collections.shuffle(nodeVariables);
        int numNodeVariables = randomly.getInteger(0, Math.min(3, nodeVariables.size() + 1));
        returnVariables.addAll(nodeVariables.subList(0, numNodeVariables));

        // Randomly select variables from RelationshipVariableManager
        List<String> relationshipVariables = new ArrayList<>(relationshipVariableManager.getAllRelationshipVariables());
        Collections.shuffle(relationshipVariables);
        int numRelationshipVariables = randomly.getInteger(0, Math.min(3, relationshipVariables.size() + 1));
        returnVariables.addAll(relationshipVariables.subList(0, numRelationshipVariables));

        // Randomly select variables from PathVariableManager
        List<String> pathVariables = new ArrayList<>(pathVariableManager.getAllPathVariables());
        Collections.shuffle(pathVariables);
        int numPathVariables = randomly.getInteger(0, Math.min(3, pathVariables.size() + 1));
        returnVariables.addAll(pathVariables.subList(0, numPathVariables));

        // Randomly select variables from AsVariableManager
        Map<String, AsVariableManager.AliasTarget> aliasVariables = graphManager.getAsVariableManager().getAllAliases();
        List<String> aliasKeys = new ArrayList<>(aliasVariables.keySet());
        Collections.shuffle(aliasKeys);
        int numAliasVariables = randomly.getInteger(0, Math.min(3, aliasKeys.size() + 1));
        returnVariables.addAll(aliasKeys.subList(0, numAliasVariables));

        // Randomly select variables from varToProperties
        Map<String, Object> varToProperties = graphManager.extractVartoProperties();
        List<String> propertyKeys = new ArrayList<>(varToProperties.keySet());
        Collections.shuffle(propertyKeys);
        int numPropertyKeys = randomly.getInteger(0, Math.min(3, propertyKeys.size() + 1));
        returnVariables.addAll(propertyKeys.subList(0, numPropertyKeys));

        // Randomly select variables from varToFunctions
        Map<String, Object> varToFunctions = graphManager.extractTargetFunctionMap();
        List<String> functionKeys = new ArrayList<>(varToFunctions.keySet());
        Collections.shuffle(functionKeys);
        int numFunctionKeys = randomly.getInteger(0, Math.min(3, functionKeys.size() + 1));
        returnVariables.addAll(functionKeys.subList(0, numFunctionKeys));

        CollectSubquery collectSubquery=null;
        if(randomly.getInteger(0,5)==0) collectSubquery=CollectSubquery.generateCollectSubquery(graphManager);
        //System.out.println(collectSubquery.toCypher());
        if(collectSubquery!=null) returnVariables.add(collectSubquery.toCypher());

        if (randomly.getInteger(0, 5) == 0) distinct = true;
        if (randomly.getInteger(0, 5) == 0) useWildcard = true;

        List<ReadingSubClause> readingSubClauses = new ArrayList<>();
        if (randomly.getBoolean()) {
            readingSubClauses.add(SkipClause.generateRandomSkipClause());
        }
        if (randomly.getBoolean()) {
            readingSubClauses.add(LimitClause.generateRandomLimitClause());
        }

        // Return the generated ReturnClause
        return new ReturnClause(returnVariables, distinct, useWildcard, readingSubClauses);
    }

    /**
     * Generates a random SUB_RETURN clause.
     * It selects variables from NodeVariableManager, RelationshipVariableManager, PathVariableManager,
     * AsVariableManager, varToProperties, and varToFunctions.
     *
     * @param graphManager The GraphManager to retrieve variables from.
     * @return A randomly generated ReturnClause.
     */
    public static ReturnClause generateRandomSubReturnClause(GraphManager graphManager) {
        Randomly randomly = new Randomly();
        boolean distinct = false;
        boolean useWildcard = false;

        if (randomly.getInteger(0, 5) == 0) distinct = true;
        //if (randomly.getInteger(0, 5) == 0) useWildcard = true;
        NodeVariableManager nodeVariableManager = graphManager.getNodeVariableManager();
        RelationshipVariableManager relationshipVariableManager = graphManager.getRelationshipVariableManager();
        PathVariableManager pathVariableManager = graphManager.getPathVariableManager();
        AsVariableManager asVariableManager = graphManager.getAsVariableManager();

        List<String> returnVariables = new ArrayList<>();



        Map<String,Object> newAlias=new HashMap<>();
        // =============== 属性/函数处理（强制生成别名） ===============

        // Randomly select variables from varToProperties
        Map<String, Object> varToProperties = graphManager.extractVartoProperties();
        List<String> propertyKeys = new ArrayList<>(varToProperties.keySet());
        Collections.shuffle(propertyKeys);
        int numPropertyKeys = randomly.getInteger(0, Math.min(3, propertyKeys.size() + 1));
        List<String> selectedProperties = propertyKeys.subList(0, numPropertyKeys);
        for (int i = 0; i < numPropertyKeys; i++) {
            Object target=varToProperties.get(selectedProperties.get(i));
            newAlias.put(selectedProperties.get(i),target);
            //String alias=asVariableManager.addAlias(target);
            //returnVariables.add(selectedProperties.get(i)+" AS "+alias);
        }

        // =============== 函数处理 ===============
        Map<String, Object> varToFunctions = graphManager.extractTargetFunctionMap();
        List<String> functionKeys = new ArrayList<>(varToFunctions.keySet());
        Collections.shuffle(functionKeys);
        int numFunctionKeys = randomly.getInteger(0, Math.min(3, functionKeys.size() + 1));
        List<String> selectedFunctions = functionKeys.subList(0, numFunctionKeys);
        for (String func : selectedFunctions) {
            Object target = varToFunctions.get(func);
            //String alias = asVariableManager.addAlias(target);
            //returnVariables.add(func + " AS " + alias); // 示例：count(n) AS alias2
            newAlias.put(func,target);
        }

        // Randomly select variables from NodeVariableManager
        List<String> nodeVariables = new ArrayList<>(nodeVariableManager.getAllNodeVariables());
        Collections.shuffle(nodeVariables);
        int numNodeVariables = randomly.getInteger(0, Math.min(3, nodeVariables.size() + 1));
        List<String> selectedNodeVariables = nodeVariables.subList(0, numNodeVariables);
        List<String> selectedAlias=new ArrayList<>();//用于储存要保留的变量别名
        for (int i = 0; i < numNodeVariables; i++) {
            AbstractNode target=nodeVariableManager.getNodeVariable(selectedNodeVariables.get(i));
            String alias=nodeVariableManager.generateNodeVariable(target);
            selectedAlias.add(alias);//要保留的变量名
            //nodeVariableManager.removeNodeVariable(selectedNodeVariables.get(i));
            returnVariables.add(selectedNodeVariables.get(i)+" AS "+alias);
        }
        //returnVariables.addAll(selectedNodeVariables);
        if(!useWildcard) nodeVariableManager.retainVariables(selectedAlias);

        // Randomly select variables from RelationshipVariableManager
        List<String> relVariables = new ArrayList<>(relationshipVariableManager.getAllRelationshipVariables());
        Collections.shuffle(relVariables);
        int numRel = randomly.getInteger(0, Math.min(3, relVariables.size() + 1));
        List<String> selectedRels = relVariables.subList(0, numRel);
        selectedAlias.clear();
        for (int i = 0; i < numRel; i++) {
            AbstractRelationship target=relationshipVariableManager.getRelationship(selectedRels.get(i));
            String alias=relationshipVariableManager.generateRelationshipVariable(target);
            //nodeVariableManager.removeNodeVariable(selectedNodeVariables.get(i));
            selectedAlias.add(alias);
            returnVariables.add(selectedRels.get(i)+" AS "+alias);
        }
        //returnVariables.addAll(selectedRels);
        if(!useWildcard) relationshipVariableManager.retainVariables(selectedAlias); // 保留选中关系

        // Randomly select variables from PathVariableManager
        List<String> pathVariables = new ArrayList<>(pathVariableManager.getAllPathVariables());
        Collections.shuffle(pathVariables);
        int numPath = randomly.getInteger(0, Math.min(3, pathVariables.size() + 1));
        List<String> selectedPaths = pathVariables.subList(0, numPath);
        selectedAlias.clear();
        for (int i = 0; i < numPath; i++) {
            AbstractPath target=pathVariableManager.getPathVariable(selectedPaths.get(i));
            String alias=pathVariableManager.generatePathVariable();
            //nodeVariableManager.removeNodeVariable(selectedNodeVariables.get(i));
            selectedAlias.add(alias);
            returnVariables.add(selectedPaths.get(i)+" AS "+alias);
        }
        //returnVariables.addAll(selectedPaths);
        if(!useWildcard)pathVariableManager.retainVariables(selectedAlias); // 保留选中路径

        // Randomly select variables from AsVariableManager
        Map<String, AsVariableManager.AliasTarget> aliases = asVariableManager.getAllAliases();
        List<String> aliasKeys = new ArrayList<>(aliases.keySet());
        Collections.shuffle(aliasKeys);
        int numAlias = randomly.getInteger(0, Math.min(3, aliasKeys.size() + 1));
        List<String> selectedAliases = aliasKeys.subList(0, numAlias);
        selectedAlias.clear();
        for (int i = 0; i < numAlias; i++) {
            Object target=asVariableManager.getTarget(selectedAliases.get(i));
            String alias=asVariableManager.addAlias(target);
            selectedAlias.add(alias);

            //nodeVariableManager.removeNodeVariable(selectedNodeVariables.get(i));
            returnVariables.add(selectedAliases.get(i)+" AS "+alias);
        }
        //returnVariables.addAll(selectedAliases);
        if(!useWildcard) asVariableManager.retainVariables(selectedAlias); // 保留选中别名

        for(Map.Entry<String,Object> entry:newAlias.entrySet()){
            String alias=asVariableManager.addAlias(entry.getValue());
            returnVariables.add(entry.getKey()+" AS "+alias);
        }

        List<ReadingSubClause> readingSubClauses = new ArrayList<>();
        if (randomly.getBoolean()) {
            readingSubClauses.add(SkipClause.generateRandomSkipClause());
        }
        if (randomly.getBoolean()) {
            readingSubClauses.add(LimitClause.generateRandomLimitClause());
        }

        // Return the generated ReturnClause
        return new ReturnClause(returnVariables, distinct, useWildcard, readingSubClauses);
    }

    public static ReturnClause generateRandomCollectReturnClause(GraphManager graphManager) {
        Randomly randomly = new Randomly();

        NodeVariableManager nodeVM = graphManager.getNodeVariableManager();
        RelationshipVariableManager relVM = graphManager.getRelationshipVariableManager();
        PathVariableManager pathVM = graphManager.getPathVariableManager();
        AsVariableManager asVM = graphManager.getAsVariableManager();
        Map<String, Object> varToProperties = graphManager.extractVartoProperties();
        Map<String, Object> varToFunctions = graphManager.extractTargetFunctionMap();

        List<String> returnVariables = new ArrayList<>();
        List<ReadingSubClause> readingSubClauses = new ArrayList<>();

        // 随机选择来源类型 (0-节点, 1-关系, 2-路径, 3-函数, 4-属性, 5-别名)
        int collectChoice = randomly.getInteger(0, 5);
        String collectExpression = null;
        Object collectedTarget = null;

        switch (collectChoice) {
            case 0: // 节点变量
                List<String> nodes = new ArrayList<>(nodeVM.getAllNodeVariables());
                if (!nodes.isEmpty()) {
                    String nodeVar = nodes.get(randomly.getInteger(0, nodes.size()));
                    collectExpression = nodeVar;
                    collectedTarget = nodeVM.getNodeVariable(nodeVar);
                    nodeVM.cleanUpExpiredVariables();

                }
                break;

            case 1: // 关系变量
                List<String> rels = new ArrayList<>(relVM.getAllRelationshipVariables());
                if (!rels.isEmpty()) {
                    String relVar = rels.get(randomly.getInteger(0, rels.size()));
                    collectExpression = relVar;
                    collectedTarget = relVM.getRelationship(relVar);
                    relVM.cleanUpExpiredVariables();// 保留选中关系
                }
                break;

            case 2: // 路径变量
                List<String> paths = new ArrayList<>(pathVM.getAllPathVariables());
                if (!paths.isEmpty()) {
                    String pathVar = paths.get(randomly.getInteger(0, paths.size()));
                    collectExpression = pathVar;
                    collectedTarget = pathVM.getPathVariable(pathVar);
                    pathVM.cleanUpExpiredVariables();
                }
                break;


            case 3: // 属性表达式
                List<String> properties = new ArrayList<>(varToProperties.keySet());
                if (!properties.isEmpty()) {
                    String prop = properties.get(randomly.getInteger(0, properties.size()));
                    collectExpression = prop ;
                    collectedTarget = varToProperties.get(prop);
                }
                break;

            case 4: // 已存在别名
                Map<String, AsVariableManager.AliasTarget> aliases = asVM.getAllAliases();
                List<String> aliasKeys = new ArrayList<>(aliases.keySet());
                if (!aliasKeys.isEmpty()) {
                    String alias = aliasKeys.get(randomly.getInteger(0, aliasKeys.size()));
                    collectExpression = alias;
                    collectedTarget = aliases.get(alias).getTarget();
                    asVM.cleanUpExpiredVariables();
                }
                break;
        }

        // 候选池为空时的保底逻辑
        if (collectExpression == null) {
            return null;
        }

        returnVariables.add(collectExpression);

        // 可选分页子句
        if (randomly.getBoolean()) {
            readingSubClauses.add(SkipClause.generateRandomSkipClause());
        }
        if (randomly.getBoolean()) {
            readingSubClauses.add(LimitClause.generateRandomLimitClause());
        }

        return new ReturnClause(returnVariables, false, false, readingSubClauses);
    }


}
