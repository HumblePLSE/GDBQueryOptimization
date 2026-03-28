package org.example.project.cypher.gen;

import org.example.project.Randomly;
import org.example.project.cypher.gen.assertion.BooleanAssertion;
import org.example.project.cypher.gen.assertion.ComparisonAssertion;
import org.example.project.cypher.gen.assertion.ExpressionAssertion;
import org.example.project.cypher.gen.assertion.StringMatchingAssertion;
import org.example.project.cypher.standard_ast.CypherType;
import org.example.project.cypher.ast.IExpression;
import org.example.project.cypher.standard_ast.expr.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RandomExpressionGenerator {

    private Map<String, Object> varToProperties;

    private Randomly randomly = new Randomly();

    public RandomExpressionGenerator(Map<String, Object> varToProperties) {
        this.varToProperties = varToProperties;
    }

    private IExpression generateBooleanConst(BooleanAssertion booleanAssertion) {
        if (booleanAssertion != null) {
            return new ConstExpression(booleanAssertion.getValue());
        }
        return new ConstExpression(randomly.getInteger(0, 100) < 50);
    }

    public IExpression generateCondition(int depth) {
        return booleanExpression(depth, new BooleanAssertion(true));
    }

    /*public IExpression generateListWithBasicType(int depth, CypherType type){
        Randomly randomly = new Randomly();
        int randomNum = randomly.getInteger(1,4);
        List<IExpression> expressions = new ArrayList<>();
        for(int i = 0; i < randomNum; i++){
            //todo 更复杂的列表生成
            expressions.add(basicTypeExpression(depth, type));
        }
        return new CreateListExpression(expressions);
    }*/

    private IExpression basicTypeExpression(int depth, CypherType type) {
        switch (type) {
            case BOOLEAN:
                return booleanExpression(depth, null);
            case STRING:
                return stringExpression(depth, null);
            case NUMBER:
                return numberExpression(depth, null);
            default:
                return null;
        }
    }

    private IExpression generateUseVar(Class<?> javaType, ExpressionAssertion assertion) {
        Randomly randomly = new Randomly();
        List<IExpression> availableExpressions = new ArrayList<>();

        // 遍历 varToProperties，找到符合类型的变量
        for (Map.Entry<String, Object> entry : varToProperties.entrySet()) {
            String variable = entry.getKey();
            Object value = entry.getValue();

            // 检查值是否符合目标 Java 类型
            if (javaType.isInstance(value)) {
                // 如果 assertion 存在，则检查值是否满足 assertion 的条件
                if (assertion == null || assertion.check(value)) {
                    availableExpressions.add(new VariableExpression(variable, value));
                }
            }

            if (value instanceof ExprUnknownVal) {
                ExprUnknownVal unknownValue = (ExprUnknownVal) value;
                if(unknownValue.getAssociatedClass()==javaType){
                    if (assertion == null || assertion.check(value)) {
                        availableExpressions.add(new VariableExpression(variable, value));
                    }
                }
            }
        }

        // 如果没有找到可用的变量，返回一个常量表达式作为默认值
        if (availableExpressions.isEmpty()) {
            return generateDefaultConst(javaType, assertion);
        }

        // 从可用表达式中随机选择一个
        //return availableExpressions.get(randomly.getInteger(0, availableExpressions.size()));
        int index=randomly.getInteger(0, availableExpressions.size());
        return availableExpressions.get(index);
    }

    private IExpression generateDefaultConst(Class<?> javaType, ExpressionAssertion assertion) {
        Randomly randomly = new Randomly();

        // Handle Integer (Number) type
        if (javaType.equals(Integer.class)) {
            if (assertion instanceof ComparisonAssertion) {
                ComparisonAssertion comparison = (ComparisonAssertion) assertion;
                if(comparison.getLeftOp()==ExprUnknownVal.UNKNOWN_INTEGER)
                {
                    return new ConstExpression(randomly.getInteger(Integer.MIN_VALUE, Integer.MAX_VALUE));
                }
                int leftOp = (int) comparison.getLeftOp();
                BinaryComparisonExpression.BinaryComparisonOperation operation = comparison.getOperation();
                if (!comparison.trueTarget()) {
                    operation = operation.reverse();
                }

                switch (operation) {
                    case EQUAL:
                        return new ConstExpression(leftOp);
                    case HIGHER:
                        return leftOp > Integer.MIN_VALUE
                                ? new ConstExpression(randomly.getInteger(Integer.MIN_VALUE, leftOp))
                                : new ConstExpression(leftOp);
                    case HIGHER_OR_EQUAL:
                        return leftOp > Integer.MIN_VALUE
                                ? new ConstExpression(randomly.getInteger(Integer.MIN_VALUE, leftOp + 1))
                                : new ConstExpression(leftOp);
                    case SMALLER:
                        return leftOp < Integer.MAX_VALUE
                                ? new ConstExpression(randomly.getInteger(leftOp + 1, Integer.MAX_VALUE))
                                : new ConstExpression(leftOp);
                    case SMALLER_OR_EQUAL:
                        return leftOp < Integer.MAX_VALUE
                                ? new ConstExpression(randomly.getInteger(leftOp, Integer.MAX_VALUE))
                                : new ConstExpression(leftOp);
                    case NOT_EQUAL: {
                        int randomValue = randomly.getInteger(Integer.MIN_VALUE, Integer.MAX_VALUE);
                        return new ConstExpression(randomValue != leftOp ? randomValue : randomValue + 1);
                    }
                }
            }
            return new ConstExpression(randomly.getInteger(Integer.MIN_VALUE, Integer.MAX_VALUE));
        }

        // Handle String type
        if (javaType.equals(String.class)) {
            if (assertion instanceof StringMatchingAssertion) {
                StringMatchingAssertion stringAssertion = (StringMatchingAssertion) assertion;
                String targetString = (String) stringAssertion.getString();
                StringMatchingExpression.StringMatchingOperation operation = stringAssertion.getOperation();
                boolean isTarget = stringAssertion.isTarget();
                String candidate;

                switch (operation) {
                    case CONTAINS:
                        if (isTarget) {
                            return new ConstExpression(randomly.getString() + targetString + randomly.getString());
                        }
                        for (int i = 0; i < 50; i++) {
                            candidate = randomly.getString();
                            if (!candidate.contains(targetString)) {
                                return new ConstExpression(candidate);
                            }
                        }
                        return new ConstExpression(randomly.getString());

                    case STARTS_WITH:
                        if (isTarget) {
                            // 生成一个以目标字符串开头的随机字符串
                            return new ConstExpression(targetString + randomly.getString());
                        }
                        for (int i = 0; i < 50; i++) {
                            candidate = randomly.getString();
                            if (!candidate.startsWith(targetString)) {
                                return new ConstExpression(candidate);
                            }
                        }
                        return new ConstExpression(randomly.getString());

                    case ENDS_WITH:
                        if (isTarget) {
                            // 生成一个以目标字符串结尾的随机字符串
                            return new ConstExpression(randomly.getString() + targetString);
                        }
                        for (int i = 0; i < 50; i++) {
                            candidate = randomly.getString();
                            if (!candidate.endsWith(targetString)) {
                                return new ConstExpression(candidate);
                            }
                        }
                        return new ConstExpression(randomly.getString());
                }

            }

            if (assertion instanceof ComparisonAssertion) {
                ComparisonAssertion comparisonAssertion = (ComparisonAssertion) assertion;
                String leftOp = (String) comparisonAssertion.getLeftOp();
                BinaryComparisonExpression.BinaryComparisonOperation operation = comparisonAssertion.getOperation();
                if (!comparisonAssertion.trueTarget()) {
                    operation = operation.reverse();
                }
                String candidate;

                switch (operation) {
                    case EQUAL:
                        return new ConstExpression(leftOp);
                    case NOT_EQUAL:
                        for (int i = 0; i < 50; i++) {
                            candidate = randomly.getString();
                            if (!candidate.equals(leftOp)) {
                                return new ConstExpression(candidate);
                            }
                        }
                        return new ConstExpression(randomly.getString() + "_diff");
                    case SMALLER: // 左侧 < 右侧
                        for (int i = 0; i < 50; i++) {
                            candidate = randomly.getString();
                            if (candidate.compareTo(leftOp) > 0) { // candidate > leftOp
                                return new ConstExpression(candidate);
                            }
                        }
                        return new ConstExpression(randomly.getString());
                    case SMALLER_OR_EQUAL: // 左侧 <= 右侧
                        for (int i = 0; i < 50; i++) {
                            candidate = randomly.getString();
                            if (candidate.compareTo(leftOp) >= 0) { // candidate >= leftOp
                                return new ConstExpression(candidate);
                            }
                        }
                        return new ConstExpression(leftOp);
                    case HIGHER: // 左侧 > 右侧
                        for (int i = 0; i < 50; i++) {
                            candidate = randomly.getString();
                            if (candidate.compareTo(leftOp) < 0) { // candidate < leftOp
                                return new ConstExpression(candidate);
                            }
                        }
                        return new ConstExpression(randomly.getString());
                    case HIGHER_OR_EQUAL: // 左侧 >= 右侧
                        for (int i = 0; i < 50; i++) {
                            candidate = randomly.getString();
                            if (candidate.compareTo(leftOp) <= 0) { // candidate <= leftOp
                                return new ConstExpression(candidate);
                            }
                        }
                        return new ConstExpression(leftOp);
                }
            }

            // Fallback to random string if no specific assertion is provided
            return new ConstExpression(randomly.getString());
        }

        // Handle Boolean type
        if (javaType.equals(Boolean.class)) {
            boolean defaultValue = assertion instanceof BooleanAssertion
                    ? ((BooleanAssertion) assertion).getValue()
                    : randomly.getInteger(0, 100) < 50;
            return new ConstExpression(defaultValue);
        }

        // Handle LocalDate type
        if (javaType.equals(java.time.LocalDate.class)) {
            return new ConstExpression(java.time.LocalDate.now()); // 默认返回当前日期
        }

        // For unsupported types, return a null constant
        return new ConstExpression(null);
    }


    private IExpression booleanExpression(int depth, BooleanAssertion booleanAssertion) {
        Randomly randomly = new Randomly();
        int expressionChoice = randomly.getInteger(0, 100);
       //expressionChoice=45;//debug
        if (depth == 0 || expressionChoice < 10) {
            //深度用尽，快速收束，对于BOOLEAN而言： 返回true/false，返回boolean类型property，返回boolean变量引用
            int randomNum = randomly.getInteger(0, 100);
            if (randomNum < 20) {
                return generateDefaultConst(Boolean.class, booleanAssertion);
            }
            return generateUseVar(Boolean.class, booleanAssertion);
            //todo Is_NULL的单独处理逻辑
        }

        //尚有深度

        boolean target = booleanAssertion == null ? randomly.getInteger(0, 100) < 50 : booleanAssertion.getValue();

        if (expressionChoice < 20) {
            IExpression numberExpr = numberExpression(depth - 1, null);
            if (numberExpr.getValue() == null) {
                return BinaryComparisonExpression.randomComparison(numberExpr, numberExpression(depth - 1, null));
            }

            BinaryComparisonExpression.BinaryComparisonOperation op = BinaryComparisonExpression.randomOperation();
            return new BinaryComparisonExpression(numberExpr, numberExpression(depth - 1,
                    new ComparisonAssertion(op, numberExpr.getValue(), target)), op);
        }
        if (expressionChoice < 30) {
            IExpression stringExpr = stringExpression(depth - 1, null);
            if (stringExpr.getValue() == null) {
                return BinaryComparisonExpression.randomComparison(stringExpr, stringExpression(depth - 1, null));
            }

            BinaryComparisonExpression.BinaryComparisonOperation op = BinaryComparisonExpression.randomOperation();
            return new BinaryComparisonExpression(stringExpr, stringExpression(depth - 1,
                    new ComparisonAssertion(op, stringExpr.getValue(), target)), op);
        }
        if (expressionChoice < 40) {
            IExpression stringExpr = stringExpression(depth - 1, null);
            if (stringExpr.getValue() == null) {
                return StringMatchingExpression.randomMatching(stringExpr, stringExpression(depth - 1, null));
            }

            StringMatchingExpression.StringMatchingOperation op = StringMatchingExpression.randomOperation();
            return new StringMatchingExpression(stringExpression(depth - 1,
                    new StringMatchingAssertion(op, stringExpr.getValue(), target)),stringExpr,  op);
        }
        if (expressionChoice < 50) {
            return new SingleLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(!target)), SingleLogicalExpression.SingleLogicalOperation.NOT);
        }

        BinaryLogicalExpression.BinaryLogicalOperation op = BinaryLogicalExpression.randomOp();
        switch (op) {
            case AND:
                if (target) {
                    return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(true)),
                            booleanExpression(depth - 1, new BooleanAssertion(true)),
                            op);
                } else {
                    if (randomly.getInteger(0, 100) < 50) {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(false)),
                                booleanExpression(depth - 1, null),
                                op);
                    } else {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, null),
                                booleanExpression(depth - 1, new BooleanAssertion(false)),
                                op);
                    }
                }
            case OR:
                if (target) {
                    if (randomly.getInteger(0, 100) < 50) {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(true)),
                                booleanExpression(depth - 1, null),
                                op);
                    } else {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, null),
                                booleanExpression(depth - 1, new BooleanAssertion(true)),
                                op);
                    }
                } else {
                    return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(false)),
                            booleanExpression(depth - 1, new BooleanAssertion(false)),
                            op);
                }
            case XOR:
                if (target) {
                    if (randomly.getInteger(0, 100) < 50) {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(true)),
                                booleanExpression(depth - 1, new BooleanAssertion(false)),
                                op);
                    } else {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(false)),
                                booleanExpression(depth - 1, new BooleanAssertion(true)),
                                op);
                    }
                } else {
                    if (randomly.getInteger(0, 100) < 50) {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(false)),
                                booleanExpression(depth - 1, new BooleanAssertion(false)),
                                op);
                    } else {
                        return new BinaryLogicalExpression(booleanExpression(depth - 1, new BooleanAssertion(true)),
                                booleanExpression(depth - 1, new BooleanAssertion(true)),
                                op);
                    }
                }
            default:
                throw new RuntimeException();
        }
    }

    private IExpression stringExpression(int depth, ExpressionAssertion expressionAssertion) {
        Randomly randomly = new Randomly();
        int expressionChoice = randomly.getInteger(0, 100);

        // 深度用尽或者随机选择简单表达式
        if (depth == 0 || expressionChoice < 70) {
            int randomNum = randomly.getInteger(0, 100);
            if (randomNum < 20) {
                return generateDefaultConst(String.class, expressionAssertion);
            }
            return generateUseVar(String.class, expressionAssertion);
        }

        // 生成更复杂的表达式
        IExpression left = stringExpression(depth - 1, null);
        Object leftValue = left.getValue();

        if (leftValue == null) {
            // 如果左值未知，直接拼接两个新的字符串表达式
            return new StringCatExpression(left, stringExpression(depth - 1, null));
        } else {
            if (expressionAssertion instanceof StringMatchingAssertion) {
                StringMatchingAssertion assertion = (StringMatchingAssertion) expressionAssertion;
                if (assertion.isTarget()) {
                    // 生成满足断言的表达式
                    switch (assertion.getOperation()) {
                        case STARTS_WITH:
                            return new StringCatExpression(stringExpression(depth - 1, expressionAssertion),
                                    stringExpression(depth - 1, null));
                        case ENDS_WITH:
                            return new StringCatExpression(stringExpression(depth - 1, null),
                                    stringExpression(depth - 1, expressionAssertion));
                        case CONTAINS:
                            Object stringObj = assertion.getString();
                            if (stringObj == null) {
                                return new StringCatExpression(stringExpression(depth - 1, null),
                                        stringExpression(depth - 1, null));
                            }
                            String string = (String) stringObj;
                            int randNum = randomly.getInteger(0, 100);
                            if (randNum < 30) {
                                return new StringCatExpression(stringExpression(depth - 1, null),
                                        stringExpression(depth - 1, expressionAssertion));
                            } else if (randNum < 60 || string.isEmpty()) {
                                return new StringCatExpression(stringExpression(depth - 1, expressionAssertion),
                                        stringExpression(depth - 1, null));
                            } else {
                                int index = randomly.getInteger(0, string.length());
                                String first = string.substring(0, index);
                                String second = string.substring(index);
                                return new StringCatExpression(stringExpression(depth - 1,
                                        new StringMatchingAssertion(StringMatchingExpression.StringMatchingOperation.ENDS_WITH, first, true)),
                                        stringExpression(depth - 1,
                                                new StringMatchingAssertion(StringMatchingExpression.StringMatchingOperation.STARTS_WITH, second, true)));
                            }
                    }
                } else {
                    // 不满足目标的简单拼接
                    return new StringCatExpression(stringExpression(depth - 1, null), stringExpression(depth - 1, null));
                }
            }
            // 默认生成简单拼接表达式
            return new StringCatExpression(stringExpression(depth - 1, null), stringExpression(depth - 1, null));
        }
    }


    private IExpression numberExpression(int depth, ComparisonAssertion comparisonAssertion) {
        Randomly randomly = new Randomly();
        int expressionChoice = randomly.getInteger(0, 100);
        if (depth == 0 || expressionChoice < 70) {
            //深度用尽，快速收束，对于string而言： 返回随机字符串，返回string类型property，返回string变量引用
            int randomNum = randomly.getInteger(0, 100);
            if (randomNum < 20) {
                return generateDefaultConst(Integer.class, comparisonAssertion);
            }
            return generateUseVar(Integer.class, comparisonAssertion);
        }
        return generateDefaultConst(Integer.class, comparisonAssertion);
        //return BinaryNumberExpression.randomBinaryNumber(numberExpression(depth - 1), numberExpression(depth - 1));
    }

}
