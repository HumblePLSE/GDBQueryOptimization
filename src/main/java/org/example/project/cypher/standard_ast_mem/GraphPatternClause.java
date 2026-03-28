/**
 * Context-Free Grammar for Graph Pattern in Cypher queries.
 * This defines the structure for a GraphPattern, which may consist of multiple PathPatterns
 * and an optional WHERE clause for filtering.
 * <p>
 * graphPattern ::=
 * pathPattern [ "," pathPattern ]* [ graphPatternWhereClause ]
 * <p>
 * graphPatternWhereClause ::=
 * "WHERE" booleanExpression
 * <p>
 * pathPattern ::=
 * [ pathVariableDeclaration ]
 * pathPatternExpression
 * <p>
 * pathVariableDeclaration ::=
 * pathVariable "="
 * <p>
 * pathPatternExpression ::=
 * parenthesizedPathPatternExpression | pathPatternPhrase
 * <p>
 * parenthesizedPathPatternExpression ::=
 * "("
 * [ subpathVariableDeclaration ]
 * pathPatternExpression
 * [ parenthesizedPathPatternWhereClause ]
 * ")"
 * <p>
 * subpathVariableDeclaration ::=
 * pathVariable "="
 * <p>
 * pathPatternPhrase ::=
 * [ simplePathPattern | quantifiedPathPattern ]+
 * <p>
 * simplePathPattern ::=
 * nodePattern
 * [ { relationshipPattern | quantifiedRelationship } nodePattern ]*
 * <p>
 * quantifiedPathPattern ::=
 * simplePathPattern "*"
 * <p>
 * parenthesizedPathPatternWhereClause ::=
 * "WHERE" booleanExpression
 * <p>
 * nodePattern ::=
 * "(" [ nodeVariable ] [ ":" nodeLabel ] [ nodeProperties ] ")"
 * <p>
 * relationshipPattern ::=
 * relationshipDirection [ "[" relationshipVariable ":" relationshipType [ relationshipProperties ] "]" ] relationshipDirection
 * <p>
 * quantifiedRelationship ::=
 * "[" relationshipVariable ":" relationshipType "*" range "]"
 * <p>
 * nodeVariable ::=
 * variable
 * <p>
 * nodeLabel ::=
 * label
 * <p>
 * nodeProperties ::=
 * "{" propertyMap "}"
 * <p>
 * relationshipDirection ::=
 * "<-" | "-" | "->"
 * <p>
 * relationshipVariable ::=
 * variable
 * <p>
 * relationshipType ::=
 * label
 * <p>
 * relationshipProperties ::=
 * "{" propertyMap "}"
 * <p>
 * range ::=
 * rangeStart [ ".." rangeEnd ]
 * <p>
 * rangeStart ::=
 * integer
 * <p>
 * rangeEnd ::=
 * integer
 * <p>
 * propertyMap ::=
 * propertyKey ":" propertyValue [ "," propertyKey ":" propertyValue ]*
 */


package org.example.project.cypher.standard_ast_mem;

import java.util.*;

import org.example.project.Randomly;
import org.example.project.cypher.gen.*;


/**
 * Represents a GraphPattern in Cypher queries.
 *
 * A GraphPattern consists of one or more PathPatterns and an optional WHERE clause:
 *
 * graphPattern ::=
 *     pathPattern [ "," pathPattern ]* [ graphPatternWhereClause ]
 */
public class GraphPatternClause extends Clause {

    private final List<PathPatternClause> pathPatterns; // 包含的 PathPatterns
    private final WhereClause whereClause;             // 可选的 WHERE 子句
    private final QuantifiedPathPatternClause quantifiedPathPatternClause;

    public GraphPatternClause(List<PathPatternClause> pathPatterns, WhereClause whereClause,QuantifiedPathPatternClause quantifiedPathPatternClause) {
        super("GraphPattern");
        this.pathPatterns = pathPatterns;
        this.whereClause = whereClause;
        this.quantifiedPathPatternClause=quantifiedPathPatternClause;
    }

    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();
        if(quantifiedPathPatternClause==null) {
            for (int i = 0; i < pathPatterns.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(pathPatterns.get(i).toCypher());
            }
            if (whereClause != null) {
                sb.append(" ").append(whereClause.toCypher());
            }
            return sb.toString();
        }
        else
        {
            sb.append(pathPatterns.get(0).toCypher());
            sb.append("--");
            sb.append(quantifiedPathPatternClause.toCypher());
            sb.append("--");
            sb.append((pathPatterns.get(1)).toCypher());
            return sb.toString();
        }

    }

    @Override
    public boolean validate() {
        return pathPatterns != null && !pathPatterns.isEmpty()
                && pathPatterns.stream().allMatch(PathPatternClause::validate)
                && (whereClause == null || whereClause.validate());
    }

    public static GraphPatternClause generateRandomGraphPattern(int maxPaths, GraphManager graphManager) {
        Randomly randomly = new Randomly();
        if (randomly.getInteger(0, 4) == 0) {
            List<PathPatternClause> pathPatterns = new ArrayList<>();
            Set<AbstractRelationship> visitedRelationships = new HashSet<>();
            pathPatterns.add( PathPatternClause.generateRandomPathPattern(graphManager, visitedRelationships,true));
            QuantifiedPathPatternClause quantifiedPathPatternClause = QuantifiedPathPatternClause.generateRandomVarLengthRelationship(graphManager);
            pathPatterns.add(PathPatternClause.generateRandomPathPattern(graphManager,visitedRelationships,false));
            return new GraphPatternClause(pathPatterns,null,quantifiedPathPatternClause);
        }
        Set<AbstractRelationship> visitedRelationships = new HashSet<>();
        int numPaths = randomly.getInteger(1, maxPaths + 3); // 随机生成 1 到 maxPaths 个路径
        List<PathPatternClause> pathPatterns = new ArrayList<>();
        for (int i = 0; i < numPaths; i++) {
            pathPatterns.add(PathPatternClause.generateRandomPathPattern(graphManager, visitedRelationships,true));
        }

        WhereClause whereClause=null;
        if(randomly.getBoolean()) {
            Map<String, Object> varToProperties = graphManager.extractVartoProperties();
            Map<String, Object> varToFunctions = graphManager.extractIdFunctionMap();
            Map<String, Object> aliasProperties = graphManager.extractAliasProperties();
            Map<String, Object> combinedProperties = new HashMap<>();
            combinedProperties.putAll(varToProperties); // 将第一个 Map 的内容放入
            combinedProperties.putAll(aliasProperties); // 将第二个 Map 的内容放入
            combinedProperties.putAll(varToFunctions);

            whereClause = WhereClause.generateRandomWhereClause(combinedProperties);
        }

       // WhereClause whereClause = WhereClause.generateRandomWhereClause(varToProperties);
        //WhereClause whereClause = Randomly.getBooleanWithSmallProbability() ? WhereClause.generateRandomWhereClause() : null;
        return new GraphPatternClause(pathPatterns, whereClause,null);
    }

    public static GraphPatternClause generateRandomCreateGraphPattern(int maxPaths, GraphManager graphManager) {
        Randomly randomly = new Randomly();
        Set<AbstractRelationship> visitedRelationships = new HashSet<>();
        int numPaths = randomly.getInteger(1, maxPaths + 1); // 随机生成 1 到 maxPaths 个路径
        List<PathPatternClause> pathPatterns = new ArrayList<>();
        for (int i = 0; i < numPaths; i++) {
            pathPatterns.add(PathPatternClause.generateRandomCreatePathPattern(graphManager, visitedRelationships));
        }
        WhereClause whereClause = null;
        //WhereClause whereClause = Randomly.getBooleanWithSmallProbability() ? WhereClause.generateRandomWhereClause() : null;
        return new GraphPatternClause(pathPatterns, whereClause,null);
    }

    public static Map<String, Object> extractVartoProperties(GraphManager graphManager) {
        Map<String, Object> varToProperties = new HashMap<>();

        // 遍历 NodeVariableManager 的每一项
        NodeVariableManager nodeVariableManager = graphManager.getNodeVariableManager();
        Map<String, AbstractNode> nodeVariables = nodeVariableManager.getNodeVariableMap();

        for (Map.Entry<String, AbstractNode> entry : nodeVariables.entrySet()) {
            String nodeVariable = entry.getKey();
            AbstractNode node = entry.getValue();

            // 提取节点的属性
            Map<String, Object> nodeProperties = node.getProperties();
            for (Map.Entry<String, Object> propertyEntry : nodeProperties.entrySet()) {
                String propertyKey = propertyEntry.getKey();
                Object propertyValue = propertyEntry.getValue();

                // 拼接变量名和属性键，加入 varToProperties
                String varKey = nodeVariable + "." + propertyKey;
                varToProperties.put(varKey, propertyValue);
            }

            // 如果需要提取节点的函数值（如 count(), labels()），可以在此扩展
            // 示例：
            /*varToProperties.put(nodeVariable + ".count()", node.count());
            varToProperties.put(nodeVariable + ".labels()", node.getLabels());*/
        }

        // 遍历 RelationshipVariableManager 的每一项
        RelationshipVariableManager relationshipVariableManager = graphManager.getRelationshipVariableManager();
        Map<String, AbstractRelationship> relationshipVariables = relationshipVariableManager.getRelationshipVariableMap();

        for (Map.Entry<String, AbstractRelationship> entry : relationshipVariables.entrySet()) {
            String relationshipVariable = entry.getKey();
            AbstractRelationship relationship = entry.getValue();

            // 提取关系的属性
            Map<String, Object> relationshipProperties = relationship.getProperties();
            for (Map.Entry<String, Object> propertyEntry : relationshipProperties.entrySet()) {
                String propertyKey = propertyEntry.getKey();
                Object propertyValue = propertyEntry.getValue();

                // 拼接变量名和属性键，加入 varToProperties
                String varKey = relationshipVariable + "." + propertyKey;
                varToProperties.put(varKey, propertyValue);
            }
        }

        return varToProperties;
    }


}
