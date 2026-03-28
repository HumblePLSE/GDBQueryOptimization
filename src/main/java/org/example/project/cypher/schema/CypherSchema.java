package org.example.project.cypher.schema;

import org.example.project.GlobalState;
import org.example.project.common.schema.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.example.project.cypher.ICypherSchema;
import org.example.project.cypher.ast.*;
import org.example.project.cypher.ast.ICypherType;
import org.example.project.cypher.ast.IExpression;
import org.example.project.cypher.ast.ILabel;
//import org.example.project.cypher.ast.analyzer.ICypherTypeDescriptor;
import org.example.project.cypher.standard_ast.CypherType;
//import org.example.project.cypher.standard_ast.CypherTypeDescriptor;
import org.example.project.common.schema.AbstractSchema;
import org.example.project.common.schema.AbstractTable;
import org.example.project.cypher.ast.IType;

public class CypherSchema extends AbstractSchema implements ICypherSchema {

    protected List<CypherLabelInfo> labels; //所有的Label信息
    protected List<CypherRelationTypeInfo> relationTypes; //所有的relationship type信息

    protected List<CypherPropertyInfo> properties;

    public CypherSchema( ) {
    }

    //todo complete
    public CypherSchema( List<CypherLabelInfo> labels,
                       List<CypherRelationTypeInfo> relationTypes,List<CypherPropertyInfo> properties) {


        this.labels = labels;
        this.relationTypes = relationTypes;
        this.properties = properties;

    }

    public List<CypherPropertyInfo> getAllPropertyInfo(){return properties;}

    public CypherSchema GenerateSchema(int schemaScale){
        List<CypherLabelInfo> randomLabels = generateRandomLabels(schemaScale);

        // 生成随机的 Relation Types
        List<CypherRelationTypeInfo> randomRelationTypes = generateRandomRelationTypes(schemaScale);

        // 生成随机的 Properties
        List<CypherPropertyInfo> randomProperties = generateRandomProperties(schemaScale);

        // 创建并返回新的 CypherSchema
        return new CypherSchema(randomLabels, randomRelationTypes, randomProperties);

    }

    public List<CypherLabelInfo> generateRandomLabels(int count) {
        // TODO: 实现生成随机 CypherLabelInfo 的逻辑
        List<CypherLabelInfo> labels = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String labelName = "l" + i; // 生成 label 名字，例如 l0, l1, ...
            labels.add(new CypherLabelInfo(labelName));
        }
        return labels;
    }

    public List<CypherRelationTypeInfo> generateRandomRelationTypes(int count) {
        // TODO: 实现生成随机 CypherRelationTypeInfo 的逻辑
        List<CypherRelationTypeInfo> relationTypes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String relationTypeName = "rt" + i; // 生成 relationType 名字，例如 rt0, rt1, ...
            relationTypes.add(new CypherRelationTypeInfo(relationTypeName));
        }
        return relationTypes;
    }

    public List<CypherPropertyInfo> generateRandomProperties(int count) {
        // TODO: 实现生成随机 CypherPropertyInfo 的逻辑
        List<CypherPropertyInfo> properties = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String key = "k" + i; // 生成 key，例如 k0, k1, ...
            CypherType type = CypherType.getRandomBasicType(); // 调用函数生成随机类型
            properties.add(new CypherPropertyInfo(key, type)); // 创建并添加到列表
        }
        properties.add(new CypherPropertyInfo("klist",CypherType.LIST));//debug
        return properties;
    }




    public boolean containsLabel(ILabel label){
        for(ILabelInfo labelInfo : labels){
            if (labelInfo.getName().equals(label.getName())){
                return true;
            }
        }
        return false;
    }

    public ILabelInfo getLabelInfo(ILabel label){
        for(ILabelInfo labelInfo : labels){
            if (labelInfo.getName().equals(label.getName())){
                return labelInfo;
            }
        }
        return null;
    }

    public boolean containsRelationType(IType relation){
        if(relation == null){
            return false;
        }
        for(IRelationTypeInfo relationInfo : relationTypes){
            if (relationInfo != null && relationInfo.getName().equals(relation.getName())){
                return true;
            }
        }
        return false;
    }

    public IRelationTypeInfo getRelationInfo(IType relation){
        if(relation == null){
            return null;
        }
        for(IRelationTypeInfo relationInfo : relationTypes){
            if (relationInfo != null && relationInfo.getName().equals(relation.getName())){
                return relationInfo;
            }
        }
        return null;
    }


    public List<CypherLabelInfo> getLabels(){
        return labels;
    }

    public List<CypherRelationTypeInfo> getRelationTypes(){
        return relationTypes;
    }

    public List<IPropertyInfo> getProperties() {
        return properties.stream()
                .map(property -> (IPropertyInfo) property)
                .collect(Collectors.toList());
    }//可升级为流式操作，但仅支持java16以上，需更改项目java配置


    public static class CypherRelationTypeInfo implements IRelationTypeInfo {
        private String name;
       // private List<IPropertyInfo> properties = new ArrayList<>();

        public CypherRelationTypeInfo(String name) {
            this.name = name;
            //this.properties = properties;
        }

        @Override
        public String getName() {
            return name;
        }


    }

    public static class CypherLabelInfo implements ILabelInfo {
        private String name;


        public CypherLabelInfo(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }


    }

    public static class CypherPropertyInfo implements IPropertyInfo, Comparable<CypherPropertyInfo>{
        private String key;
        private CypherType type;
        private boolean isOptional;
        private int freq;

        public CypherPropertyInfo(String key, CypherType type) {
            this.key = key;
            this.type = type;
            this.freq = 0;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public CypherType getType() {
            return type;
        }

        @Override
        public boolean isOptional() {
            return isOptional;
        }

        public int getFreq() {
            return freq;
        }

        public void addFreq() {
            this.freq++;
        }

        @Override
        public int compareTo(CypherPropertyInfo prop) {
            return this.freq - prop.freq;
        }

    }

    public static abstract class CypherFunctionInfo implements IFunctionInfo {
        private String name;
        private List<IParamInfo> params;
        private CypherType expectedReturnType;

        public CypherFunctionInfo(String name, CypherType expectedReturnType, IParamInfo ...params){
            this.name = name;
            this.params = Arrays.asList(params);
            this.expectedReturnType = expectedReturnType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<IParamInfo> getParams() {
            return params;
        }

        @Override
        public CypherType getExpectedReturnType() {
            return expectedReturnType;
        }
    }


    /*public enum CypherBuiltInFunctions implements IFunctionInfo{
        AVG("avg", "avg@number", CypherType.NUMBER, new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        MAX_NUMBER("max", "max@number", CypherType.NUMBER, new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        MAX_STRING("max", "max@string", CypherType.STRING, new CypherParamInfo(CypherType.STRING, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        MIN_NUMBER("min", "min@number", CypherType.NUMBER, new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        MIN_STRING("min", "min@string", CypherType.STRING, new CypherParamInfo(CypherType.STRING, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        COUNT("count", "count", CypherType.NUMBER, new CypherParamInfo(CypherType.ANY, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return new CypherTypeDescriptor(CypherType.NUMBER);
            }
        },
        PERCENTILE_COUNT_NUMBER("percentileCount", "percentileCount@number", CypherType.NUMBER,
                new CypherParamInfo(CypherType.NUMBER, false), new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        PERCENTILE_COUNT_STRING("percentileCount", "percentileCount@string", CypherType.NUMBER,
                new CypherParamInfo(CypherType.STRING, false), new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        PERCENTILE_DISC_NUMBER("percentileDisc", "percentileDisc@number", CypherType.NUMBER,
                new CypherParamInfo(CypherType.NUMBER, false), new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        PERCENTILE_DISC_STRING("percentileDisc", "percentileDisct@string", CypherType.NUMBER,
                new CypherParamInfo(CypherType.STRING, false), new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        ST_DEV("stDev", "stDev", CypherType.NUMBER, new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        ST_DEV_P("stDevP", "stDevP", CypherType.NUMBER, new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        SUM("sum", "sum", CypherType.NUMBER, new CypherParamInfo(CypherType.NUMBER, false)){
            @Override
            public ICypherTypeDescriptor calculateReturnType(List<IExpression> params) {
                return null;
            }
        },
        ;

        *//*CypherBuiltInFunctions(String name, String signature, CypherType expectedReturnType, IParamInfo... params){
            this.name = name;
            this.params = Arrays.asList(params);
            this.expectedReturnType = expectedReturnType;
        }*//*

        private String name, signature;
        private List<IParamInfo> params;
        private CypherType expectedReturnType;

        //@Override
        public String getName() {
            return name;
        }

       // @Override
        public String getSignature() {
            return signature;
        }

       // @Override
        public List<IParamInfo> getParams() {
            return params;
        }

       // @Override
        public CypherType getExpectedReturnType() {
            return expectedReturnType;
        }
    }
*/

   /* public static class CypherParamInfo implements IParamInfo{
        private boolean isOptionalLength;
        private CypherType paramType;

        public CypherParamInfo(CypherType type, boolean isOptionalLength){
            paramType = type;
            this.isOptionalLength = isOptionalLength;
        }

        @Override
        public boolean isOptionalLength() {
            return isOptionalLength;
        }

        @Override
        public CypherType getParamType() {
            return paramType;
        }
    }
*/
    @Override
    public List<ILabelInfo> getLabelInfos() {
        return labels.stream().map(l->l).collect(Collectors.toList());
    }

    @Override
    public List<IRelationTypeInfo> getRelationshipTypeInfos() {
        return relationTypes.stream().map(r->r).collect(Collectors.toList());
    }
}
