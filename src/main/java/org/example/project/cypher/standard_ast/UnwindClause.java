package org.example.project.cypher.standard_ast;

import org.example.project.cypher.gen.AsVariableManager;
import org.example.project.cypher.gen.GraphManager;
import org.example.project.Randomly;
import org.example.project.cypher.gen.NodeVariableManager;
import org.example.project.cypher.gen.RelationshipVariableManager;
import org.example.project.cypher.standard_ast.expr.ExprUnknownVal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents an UNWIND clause in Cypher.
 */
public class UnwindClause extends ProjectingClause {
    private final String unwindList;  // UNWIND 的列表表达式（可以是变量名或显式列表）
    private final String alias;        // 解包后的别名

    public UnwindClause(String unwindList, String alias) {
        super("UNWIND");
        this.unwindList = unwindList;
        this.alias = alias;
    }

    @Override
    public String toCypher() {
        return (alias != null && !alias.isEmpty())
                ? "UNWIND " + unwindList + " AS " + alias
                : "UNWIND " + unwindList;
    }

    @Override
    public boolean validate() {
        return unwindList != null && !unwindList.isEmpty() &&
                alias != null && !alias.isEmpty(); // 确保别名是合法变量名
    }

    /**
     * 生成随机的 UNWIND 子句
     * @param graphManager 用于获取已有变量或生成列表
     * @return 随机生成的 UnwindClause
     */
    public static UnwindClause generateUnwindClause(GraphManager graphManager) {
        Randomly randomly = new Randomly();
        String listExpression;
        String alias = null;
        AsVariableManager asVariableManager = graphManager.getAsVariableManager();
        Map<String, Object> varToProperties = graphManager.extractVartoProperties();

        // 1. 合并两种来源的候选列表变量
        List<String> candidateListVariables = new ArrayList<>();

        // 来源1: asVariableManager 中的 List 类型别名
        candidateListVariables.addAll(asVariableManager.getTargetTypeVariableNames(List.class));

        // 来源2: varToProperties 中的 List 类型属性（如 n0.k1）
        List<String> listProperties = varToProperties.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof List)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        candidateListVariables.addAll(listProperties);

        // 2. 动态记录列表元素类型（用于后续 alias 类型选择）
        boolean isStringList = false;

        // 3. 随机选择列表来源
        if (!candidateListVariables.isEmpty() && randomly.getBoolean()) {
            // 从候选列表中选择一个变量
            listExpression = candidateListVariables.get(randomly.getInteger(0, candidateListVariables.size() ));

            // 推断变量对应的列表元素类型（假设非空列表）
            Object listObj = listExpression.contains(".") ?  // 判断是否为属性（如 n0.k1）
                    varToProperties.get(listExpression) :
                    asVariableManager.getTarget(listExpression);

            if (listObj instanceof List && !((List<?>) listObj).isEmpty()) {
                Object firstElement = ((List<?>) listObj).get(0);
                isStringList = (firstElement instanceof String);
            }

        } else {
            // 4. 生成显式列表（随机整数或字符串列表）
            boolean generateStringList = randomly.getBoolean();
            int listLength = randomly.getInteger(0, 4);
            StringBuilder sb = new StringBuilder("[");

            for (int i = 0; i < listLength; i++) {
                if (generateStringList) {
                    // 生成带单引号的 Cypher 字符串
                    sb.append("'").append(randomly.getString()).append("'");
                    isStringList = true;
                } else {
                    sb.append(randomly.getInteger(Integer.MIN_VALUE, Integer.MAX_VALUE));
                }
                if (i < listLength - 1) sb.append(",");
            }
            sb.append("]");
            listExpression = sb.toString();
        }

        // 5. 根据类型生成 alias
        //if (randomly.getBoolean()) {
            ExprUnknownVal aliasType = isStringList ?
                    ExprUnknownVal.UNKNOWN_STRING :
                    ExprUnknownVal.UNKNOWN_INTEGER;
            alias = asVariableManager.addAlias(aliasType);
        //}

        return new UnwindClause(listExpression, alias);
    }

    // Getter 方法（如果需要）
    public String getUnwindList() {
        return unwindList;
    }

    public String getAlias() {
        return alias;
    }
}