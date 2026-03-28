package org.example.project.cypher.gen;

import org.apache.commons.lang3.tuple.Pair;
import org.example.project.GlobalState;
import org.example.project.MainOptions;
import org.example.project.Randomly;
import org.example.project.cypher.CypherQueryAdapter;
import org.example.project.cypher.ICypherSchema;
import org.example.project.cypher.schema.CypherSchema;
import org.example.project.cypher.schema.ILabelInfo;
import org.example.project.cypher.schema.IPropertyInfo;
import org.example.project.cypher.schema.IRelationTypeInfo;
import org.example.project.cypher.standard_ast.CypherType;
import org.example.project.cypher.standard_ast.expr.ExprUnknownVal;
import org.opencypher.gremlin.translation.ir.model.As;


import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class GraphManager {
    private List<AbstractNode> nodes = new ArrayList<>();
    private List<AbstractRelationship> relationships = new ArrayList<>();
    private CypherSchema schema;
    private MainOptions options;

    //private Map<IPropertyInfo, List<Object>> propertyValues = new HashMap<>();

    private NodeVariableManager nodeVariableManager = new NodeVariableManager();

    private RelationshipVariableManager relationshipVariableManager=new RelationshipVariableManager();

    private PathVariableManager pathVariableManager=new PathVariableManager();

    private AsVariableManager asVariableManager=new AsVariableManager();

    private int presentNodeID = 0;

    private int presentRelationshipID=0;
    public void addNodeNum(){
        NodeNumber++;
    }

    public void decreaseNodeNum(){ NodeNumber--;}

    public CypherSchema getSchema(){return schema;}
    private int NodeNumber ;

    private Randomly randomly = new Randomly();

    public GraphManager(MainOptions options){

        this.options = options;
        this.NodeNumber = options.getMaxNodeNum();
        this.schema = new CypherSchema().GenerateSchema(options.getMaxNodeNum()/10);
    }

    // 新增的构造函数（参数列表需匹配所有字段）
    private GraphManager(
            List<AbstractNode> nodes,
            List<AbstractRelationship> relationships,
            CypherSchema schema,
            MainOptions options,
            NodeVariableManager nodeVarManager,
            RelationshipVariableManager relVarManager,
            PathVariableManager pathVarManager,
            AsVariableManager asVarManager,
            int presentNodeID,
            int presentRelationshipID,
            int nodeNumber,
            Randomly randomly
    ) {
        this.nodes = new ArrayList<>(nodes);
        this.relationships = new ArrayList<>(relationships);
        this.schema = schema;
        this.options = options;
        //this.propertyValues = new HashMap<>(propertyValues);
        this.nodeVariableManager = nodeVarManager;
        this.relationshipVariableManager = relVarManager;
        this.pathVariableManager = pathVarManager;
        this.asVariableManager = asVarManager;
        this.presentNodeID = presentNodeID;
        this.presentRelationshipID = presentRelationshipID;
        this.NodeNumber = nodeNumber;
        this.randomly = randomly;
    }

    public GraphManager Copy() {

        // 创建新实例
        return new GraphManager(
                nodes,
                relationships,
                this.schema, // 假设 CypherSchema 是不可变的
                this.options, // 假设 MainOptions 是不可变的
                this.nodeVariableManager.Copy(),
                this.relationshipVariableManager.Copy(),
                this.pathVariableManager.Copy(),
                this.asVariableManager.Copy(),
                this.presentNodeID, // 基本类型直接复制
                this.presentRelationshipID,
                this.NodeNumber,
                new Randomly() // 如果 Randomly 有状态，需要重新实例化或深拷贝
        );
    }


    public NodeVariableManager getNodeVariableManager(){
        return nodeVariableManager;
    }

    public RelationshipVariableManager getRelationshipVariableManager(){
        return relationshipVariableManager;
    }

    public PathVariableManager getPathVariableManager(){return pathVariableManager;}

    public AsVariableManager getAsVariableManager(){return asVariableManager;}
    public void initialVariableManager()
    {
        nodeVariableManager.cleanUpExpiredVariables();
        relationshipVariableManager.cleanUpExpiredVariables();
        pathVariableManager.cleanUpExpiredVariables();
        asVariableManager.cleanUpExpiredVariables();
    }

    public int getNodeNumber(){return NodeNumber;}

    public List<CypherQueryAdapter> generateCreateGraphQueries(){
         //List<CypherQueryAdapter> results = new ArrayList<>(generateIndexQueries());
        List<CypherQueryAdapter> results = new ArrayList<>();
        Randomly randomly1=new Randomly();
        int relationshipNum = randomly1.getInteger(0,NodeNumber*5);//debug

        AbstractNode node = new AbstractNode();
        for(int i = 0; i < NodeNumber; i++){
            if(randomly1.getInteger(0,3)!=0||i==0) node = randomColorNode();//10%概率节点和上次一样
            else{
                node.setId(presentNodeID);
                node.setProperty("id",presentNodeID);
                presentNodeID++;
            }
             nodes.add(node);
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE ");
            sb.append("(n0 ");
            node.getLabelInfos().forEach(
                l->{
                    sb.append(":").append(l.getName());
                }
            );
            printProperties(sb, node.getProperties());
            sb.append(")");
            results.add(new CypherQueryAdapter(sb.toString()));
        }


        AbstractRelationship relationship=new AbstractRelationship();
        for(int i = 0; i < relationshipNum; i++){
            AbstractNode n0 = nodes.get(randomly.getInteger(0, nodes.size()));
            AbstractNode n1;
            if (randomly1.getInteger(0,10)==0) n1=n0;//10%概率创建自环
            else n1 = nodes.get(randomly.getInteger(0, nodes.size()));
            if(randomly1.getInteger(0,3)!=0||i==0) relationship = randomColorRelationship();//50%概率关系和上次一样
            else {
                relationship.setId(presentRelationshipID);
                relationship.setProperty("id",presentRelationshipID);
                presentRelationshipID++;
            }
            n0.addRelationship(relationship);
            n1.addRelationship(relationship);
            relationship.setFrom(n0);
            relationship.setTo(n1);
            relationships.add(relationship);
            StringBuilder sb = new StringBuilder();
            sb.append("MATCH ");
            sb.append("(n0 {id : ").append(""+n0.getId()).append("})");
            sb.append(", ");
            sb.append("(n1 {id : ").append(""+n1.getId()).append("})");

            sb.append(" MERGE");
            sb.append("(n0)-[r ").append(":").append(relationship.getType().getName());
            printProperties(sb, relationship.getProperties());
            sb.append("]->(n1)");

            //System.out.println(sb.toString());


            results.add(new CypherQueryAdapter(sb.toString()));
        }

        return results;
    }

    public List<AbstractNode> getNodes(){
        return nodes;
    }

    public List<AbstractRelationship> getRelationships(){
        return relationships;
    }

    private void printProperties(StringBuilder sb, Map<String, Object> properties){
        if(properties.size() != 0){
            sb.append("{");
            boolean first = true;
            for(Map.Entry<String, Object> pair : properties.entrySet()){
                if(!first){
                    sb.append(", ");
                }
                first = false;
                sb.append(pair.getKey());
                sb.append(" : ");
                if(pair.getValue() instanceof String){
                    sb.append("\"").append(pair.getValue()).append("\"");
                }
                else if(pair.getValue() instanceof Number){
                    sb.append(pair.getValue());
                }
                else if(pair.getValue() instanceof Boolean){
                    sb.append(pair.getValue());
                }
                else if(pair.getValue() instanceof LocalDate){
                    sb.append("date('").append(pair.getValue()).append("')");

                }
                else if(pair.getValue() instanceof List){
                    sb.append("[");
                    boolean firstElement = true;
                    for (Object element : (List<?>) pair.getValue()) {
                        if (!firstElement) {
                            sb.append(", ");
                        }
                        firstElement = false;

                        if (element instanceof String) {
                            sb.append("\"").append(element).append("\"");
                        } else if (element instanceof Number || element instanceof Boolean) {
                            sb.append(element);
                        } else if (element instanceof LocalDate) {
                            sb.append("date('").append(element).append("')");
                        } else {
                            sb.append("'UNSUPPORTED_ELEMENT_TYPE'"); // 兜底处理
                        }
                    }
                    sb.append("]");

                }
            }
            sb.append("}");
        }
    }


    private Object generateValue(IPropertyInfo propertyInfo) {
        switch (propertyInfo.getType()) {
            case NUMBER:
                return randomly.getInteger(Integer.MIN_VALUE, Integer.MAX_VALUE);
            case BOOLEAN:
                return randomly.getInteger(0, 2) == 0;
            case STRING:
                return randomly.getString();//仅支持大小写字母和数字
            case DATE:
                return randomly.getdata();
            case LIST:
                return randomly.getList();
            default:
                throw new IllegalArgumentException("Unsupported property type: " + propertyInfo.getType());
        }
    }

    public List<CypherQueryAdapter> generateIndexQueries() {
        List<CypherQueryAdapter> indexQueries = new ArrayList<>();
        Randomly randomly = new Randomly();

        // 遍历Schema中的所有标签
        for (ILabelInfo label : schema.getLabelInfos()) {
            // 50%概率跳过该标签
            if (randomly.getInteger(0,100)<70) continue;

            // 获取该标签下可索引的属性（字符串或数值类型）
            List<IPropertyInfo> indexableProps =  schema.getProperties().stream()
                    .filter(p -> {
                        CypherType type = p.getType();
                        return type == CypherType.STRING || type == CypherType.NUMBER;
                    })
                    .collect(Collectors.toList());;


            if (indexableProps.isEmpty()) continue;

            // 随机选择1-3个属性（至少1个）
            int propCount = randomly.getInteger(1, Math.min(1, indexableProps.size()) + 1);//debug
            List<IPropertyInfo> selectedProps = new ArrayList<>();

            Collections.shuffle(indexableProps);
            for(int i=0;i<propCount;i++){
                selectedProps.add(indexableProps.get(i));
            }

            // 确定索引类型
            boolean allString = selectedProps.stream()
                    .allMatch(p -> p.getType() == CypherType.STRING);
            String indexType = allString ? "TEXT" : "RANGE";

            // 构建索引语句
            StringBuilder sb = new StringBuilder("CREATE ");
            if (selectedProps.size() > 1) {
                sb.append("COMPOSITE ");
            }
            sb.append(indexType).append(" INDEX IF NOT EXISTS FOR (n:")
                    .append(label.getName()).append(") ON (");

            // 拼接属性列表
            StringJoiner propJoiner = new StringJoiner(", ");
            selectedProps.forEach(p -> propJoiner.add("n." + p.getKey()));
            sb.append(propJoiner).append(")");

            indexQueries.add(new CypherQueryAdapter(sb.toString()));
        }

        // 遍历Schema中的所有关系类型
        for (IRelationTypeInfo relationTypeInfo : schema.getRelationshipTypeInfos()) {
            // 50%概率跳过该标签
            if (randomly.getInteger(0,100)<70) continue;

            // 获取该标签下可索引的属性（字符串或数值类型）
            List<IPropertyInfo> indexableProps =  schema.getProperties().stream()
                    .filter(p -> {
                        CypherType type = p.getType();
                        return type == CypherType.STRING || type == CypherType.NUMBER;
                    })
                    .collect(Collectors.toList());;


            if (indexableProps.isEmpty()) continue;

            // 随机选择1-3个属性（至少1个）
            int propCount = randomly.getInteger(1, Math.min(1, indexableProps.size()) + 1);//debug
            List<IPropertyInfo> selectedProps = new ArrayList<>();

            Collections.shuffle(indexableProps);
            for(int i=0;i<propCount;i++){
                selectedProps.add(indexableProps.get(i));
            }

            // 确定索引类型
            boolean allString = selectedProps.stream()
                    .allMatch(p -> p.getType() == CypherType.STRING);
            String indexType = allString ? "TEXT" : "RANGE";

            // 构建索引语句
            StringBuilder sb = new StringBuilder("CREATE ");
            if (selectedProps.size() > 1) {
                sb.append("COMPOSITE ");
            }
            sb.append(indexType).append(" INDEX IF NOT EXISTS FOR (n:")
                    .append(relationTypeInfo.getName()).append(") ON (");

            // 拼接属性列表
            StringJoiner propJoiner = new StringJoiner(", ");
            selectedProps.forEach(p -> propJoiner.add("n." + p.getKey()));
            sb.append(propJoiner).append(")");

            indexQueries.add(new CypherQueryAdapter(sb.toString()));
        }

        return indexQueries;
    }


    public AbstractNode randomColorNode(){

        List<ILabelInfo> availableLabels = schema.getLabelInfos();
        // 随机选择 label 的数量
        int labelNum = randomly.getInteger(0,availableLabels.size()+1);
        // 随机选择若干个 label
        List<ILabelInfo> selectedLabels = getRandomLabels(availableLabels, labelNum);
        // 创建节点并设置随机的 label 和属性
        AbstractNode node = new AbstractNode();
        node.setLabelInfos(selectedLabels);
        generateProperties(node); // 为节点生成属性
        return node;
    }

    private List<ILabelInfo> getRandomLabels(List<ILabelInfo> labels, int count) {
        if (count > labels.size()) {
            throw new IllegalArgumentException("Requested more labels than available");
        }

        // 创建一个新列表用于保存结果
        List<ILabelInfo> selectedLabels = new ArrayList<>(labels);
        Collections.shuffle(selectedLabels); // 随机打乱列表
        return selectedLabels.subList(0, count); // 选择前 count 个 label
    }

    public AbstractRelationship randomColorRelationship() {
        // 从 schema 中获取所有可用的关系类型
        List<IRelationTypeInfo> availableRelationTypes = schema.getRelationshipTypeInfos();

        // 随机选择一个关系类型
        int relationTypeIndex = randomly.getInteger(0, availableRelationTypes.size());
        IRelationTypeInfo selectedRelationType = availableRelationTypes.get(relationTypeIndex);

        // 创建关系并设置随机的类型
        AbstractRelationship relationship = new AbstractRelationship();
        relationship.setType(selectedRelationType);

        // 为关系生成属性
        generateProperties(relationship);

        return relationship;
    }

    private void generateProperties(AbstractNode abstractNode) {
        Map<String, Object> result = new HashMap<>();

        // 生成唯一 ID 属性
        result.put("id", presentNodeID);
        abstractNode.setId(presentNodeID);
        presentNodeID++;

        // 从 schema 中获取所有可用的 properties
        List<IPropertyInfo> availableProperties = schema.getProperties();

        // 随机选择属性并生成值
        for (IPropertyInfo propertyInfo : availableProperties) {
            // 70% 的概率添加该属性
            if (randomly.getInteger(0, 100) < 70) {
                result.put(propertyInfo.getKey(), generateValue(propertyInfo));
            }
        }

        // 将生成的属性设置到节点
        abstractNode.setProperties(result);
    }

    private void generateProperties(AbstractRelationship relationship) {
        Map<String, Object> result = new HashMap<>();
        // 生成唯一 ID 属性
        result.put("id", presentRelationshipID);
        relationship.setId(presentRelationshipID);
        presentRelationshipID++;

        // 从 schema 中获取所有可用的属性
        List<IPropertyInfo> availableProperties = schema.getProperties();

        // 随机选择属性并生成值
        for (IPropertyInfo propertyInfo : availableProperties) {
            if (randomly.getInteger(0, 100) < 95) { // 95% 概率分配属性
                result.put(propertyInfo.getKey(), generateValue(propertyInfo));
            }
        }

        // 设置属性到关系中
        relationship.setProperties(result);
    }

    public List<Pair<String, String>> getRandomNodeVariableProperties()
    {
        Randomly randomly = new Randomly();
        List<Pair<String, String>> nodeVariableProperties=new ArrayList<>();

        // 1. Select random node properties to remove
        int nodeVariablePropertiesNum = randomly.getInteger(0, 3);  // 随机选择删除的节点属性数
        List<String> nodeVariables = nodeVariableManager.getAllNodeVariables();
        List<IPropertyInfo> propertyInfoList = schema.getProperties();
        List<String> keys = new ArrayList<>();

        // 提取所有属性的 key
        for (IPropertyInfo propertyInfo : propertyInfoList) {
            keys.add(propertyInfo.getKey());
        }

        // 随机选择 nodeVariablesNum 个节点变量
        for (int i = 0; i < nodeVariablePropertiesNum && i < nodeVariables.size(); i++) {
            String nodeVariable = nodeVariables.get(randomly.getInteger(0, nodeVariables.size() ));  // 随机选一个节点变量
            if (!keys.isEmpty()) {
                // 随机选择一个属性
                String propertyKey = keys.get(randomly.getInteger(0, keys.size() ));
                nodeVariableProperties.add(Pair.of(nodeVariable,propertyKey));
            }
        }
        return nodeVariableProperties;
    }

    public List<Pair<String, String>> getRandomRelationshipVariableProperties()
    {
        Randomly randomly = new Randomly();
        List<Pair<String, String>> relationshipVariableProperties=new ArrayList<>();

        int relationshipVariablePropertiesNum = randomly.getInteger(0, 3);  // 随机选择删除的节点属性数
        List<String> relationshipVariables = relationshipVariableManager.getAllRelationshipVariables();
        List<IPropertyInfo> propertyInfoList = schema.getProperties();
        List<String> keys = new ArrayList<>();

        // 提取所有属性的 key
        for (IPropertyInfo propertyInfo : propertyInfoList) {
            keys.add(propertyInfo.getKey());
        }

        // 随机选择 nodeVariablesNum 个节点变量
        for (int i = 0; i < relationshipVariablePropertiesNum && i < relationshipVariables.size(); i++) {
            String nodeVariable = relationshipVariables.get(randomly.getInteger(0, relationshipVariables.size() ));  // 随机选一个节点变量
            if (!keys.isEmpty()) {
                // 随机选择一个属性
                String propertyKey = keys.get(randomly.getInteger(0, keys.size() ));
                relationshipVariableProperties.add(Pair.of(nodeVariable,propertyKey));
            }
        }
        return relationshipVariableProperties;
    }

    public List<Pair<String, List<String>>> getRandomNodeVariableLabels()
    {
        Randomly randomly = new Randomly();
        List<Pair<String, List<String>>> nodeVariableLabels=new ArrayList<>();
        List<String> labelNames=new ArrayList<>();

        // Select random node variable
        int nodeVariablePropertiesNum = randomly.getInteger(0, 3);  // 随机选择删除的节点属性数
        List<String> nodeVariables = nodeVariableManager.getAllNodeVariables();
        List<ILabelInfo> labelList = schema.getLabelInfos();

        // 提取所有属性的 key
        for (ILabelInfo labelInfo : labelList) {
            labelNames.add(labelInfo.getName());
        }

        // 随机选择 nodeVariablesNum 个节点变量
        for (int i = 0; i < nodeVariablePropertiesNum && i < nodeVariables.size(); i++) {
            String nodeVariable = nodeVariables.get(randomly.getInteger(0, nodeVariables.size() ));  // 随机选一个节点变量
            List<String> selectedLabels=new ArrayList<>();
            // 随机选择若干个标签
            Collections.shuffle(labelNames);
            int labelNum=randomly.getInteger(0,Math.min(3,labelNames.size()+1));
            for(int j=0;j<labelNum;j++){
                selectedLabels.add(labelNames.get(j));
            }
            if(!selectedLabels.isEmpty()) nodeVariableLabels.add(Pair.of(nodeVariable,selectedLabels));
        }
        return nodeVariableLabels;
    }

    public void removeNodeProperties(List<Pair<String, String>> nodeProperties){
        for(Pair<String,String> nodeProperty:nodeProperties){
            AbstractNode node = nodeVariableManager.getNodeVariable(nodeProperty.getLeft());
            if (node != null) {
                node.removeProperty(nodeProperty.getRight());
            }
        }
    }

    public void removeRelationshipProperties(List<Pair<String, String>> relationshipProperties){
        for(Pair<String, String> relationshipProperty: relationshipProperties){
            AbstractRelationship relationship = relationshipVariableManager.getRelationship(relationshipProperty.getLeft());
            if (relationship != null) {
                relationship.removeProperty(relationshipProperty.getRight());
            }
        }

    }

    public void removeNodeLabels(List<Pair<String, List<String>>> nodeVariableLabels){
        for(Pair<String, List<String>> nodeLabels:nodeVariableLabels){
            AbstractNode node=nodeVariableManager.getNodeVariable(nodeLabels.getLeft());
            node.removeLabels(nodeLabels.getRight());
        }
    }

    /**
     * Gets random properties for setting in a SET clause.
     * This method traverses NodeVariableManager, RelationshipVariableManager, and AsVariableManager to fetch properties.
     *
     * @return A list of randomly selected properties.
     */
    public List<String> getRandomPropertiesToSet() {
        List<String> randomProperties = new ArrayList<>();

        // 从 schema 中获取所有可用的属性
        List<IPropertyInfo> availableProperties = schema.getProperties();

        // 1. Select properties from NodeVariableManager
        List<String> nodeVariables = nodeVariableManager.getAllNodeVariables();
        int numNodeProperties = randomly.getInteger(0, 3);
        for (int i = 0; i < numNodeProperties && i < nodeVariables.size(); i++) {
            String nodeVariable = nodeVariables.get(randomly.getInteger(0, nodeVariables.size() ));
            IPropertyInfo nodeProperty = availableProperties.get(randomly.getInteger(0, availableProperties.size() ));
            Object value;
            AbstractNode node=nodeVariableManager.getNodeVariable(nodeVariable);
            if(randomly.getInteger(1,4)==1) {
                value=null;
                node.removeProperty(nodeProperty.getKey());
            }
            else {
                value=generateValue(nodeProperty);
                node.setProperty(nodeProperty.getKey(),value);
            }
            randomProperties.add(nodeVariable + "." + nodeProperty.getKey()+" = "+convertToCypherValue(value)); // add property in "variable.property=value" format
        }

        // 2. Select properties from RelationshipVariableManager
        List<String> relationshipVariables = relationshipVariableManager.getAllRelationshipVariables();
        int numRelationshipProperties = randomly.getInteger(0, 3);
        for (int i = 0; i < numRelationshipProperties && i < relationshipVariables.size(); i++) {
            String relationshipVariable = relationshipVariables.get(randomly.getInteger(0, relationshipVariables.size() ));
            IPropertyInfo relationshipProperty = availableProperties.get(randomly.getInteger(0, availableProperties.size() ));
            Object value;
            AbstractRelationship relationship=relationshipVariableManager.getRelationship(relationshipVariable);
            if(randomly.getInteger(1,4)==1) {
                value=null;
                relationship.removeProperty(relationshipProperty.getKey());
            }

            else {
                value=generateValue(relationshipProperty);
                relationship.setProperty(relationshipProperty.getKey(),value);
            }
            randomProperties.add(relationshipVariable + "." + relationshipProperty.getKey()+" = "+convertToCypherValue(value)); // add property in "variable.property=value" format
        }

       /* // 3. Select properties from AsVariableManager (for node and relationship objects)

        // Separate handling for node aliases and relationship aliases
        // 处理节点别名
        List<String> nodeAliases = asVariableManager.getTargetTypeVariableNames(AbstractNode.class);
        int numNodeAliasProperties = randomly.getInteger(0, 3);
        for (int i = 0; i < numNodeAliasProperties && i < nodeAliases.size(); i++) {
            String aliasVariable = nodeAliases.get(randomly.getInteger(0, nodeAliases.size()));
            IPropertyInfo aliasProperty = availableProperties.get(randomly.getInteger(0, availableProperties.size()));
            Object value;
            AbstractNode targetNode = asVariableManager.getTargetAs(aliasVariable, AbstractNode.class);
            if (randomly.getInteger(1, 4) == 1) {
                value = null;
                targetNode.removeProperty(aliasProperty.getKey());
            } else {
                value = generateValue(aliasProperty);
                targetNode.setProperty(aliasProperty.getKey(), value);
            }
            randomProperties.add(aliasVariable + "." + aliasProperty.getKey() + " = " + convertToCypherValue(value)); // add property in "variable.property=value" format
        }

        // 处理关系别名
        List<String> relationshipAliases = asVariableManager.getTargetTypeVariableNames(AbstractRelationship.class);
        int numRelationshipAliasProperties = randomly.getInteger(0, 3);
        for (int i = 0; i < numRelationshipAliasProperties && i < relationshipAliases.size(); i++) {
            String aliasVariable = relationshipAliases.get(randomly.getInteger(0, relationshipAliases.size()));
            IPropertyInfo aliasProperty = availableProperties.get(randomly.getInteger(0, availableProperties.size()));
            Object value;
            AbstractRelationship targetRelationship = asVariableManager.getTargetAs(aliasVariable, AbstractRelationship.class);
            if (randomly.getInteger(1, 4) == 1) {
                value = null;
                targetRelationship.removeProperty(aliasProperty.getKey());
            } else {
                value = generateValue(aliasProperty);
                targetRelationship.setProperty(aliasProperty.getKey(), value);
            }
            randomProperties.add(aliasVariable + "." + aliasProperty.getKey() + " = " + convertToCypherValue(value)); // add property in "variable.property=value" format
        }*/

        return randomProperties;
    }


    /**
     * Converts an object to its corresponding Cypher representation as a string.
     *
     * @param value The object to convert.
     * @return The Cypher-compatible string representation.
     */
    public String convertToCypherValue(Object value) {
        if (value == null) {
            return "NULL"; // In case of null, Cypher represents it as NULL
        }

        if (value instanceof Number) {
            return value.toString(); // Directly return the string representation of the number
        }

        if (value instanceof Boolean) {
            return value.toString().toUpperCase(); // Convert boolean to uppercase (Cypher uses true/false)
        }

        if (value instanceof LocalDate) {
            // Format the date as date('YYYY-MM-DD')

            return "date('" + value.toString() + "')";
        }

        if (value instanceof String) {
            // For strings, wrap the value in double quotes
            return "\"" + value.toString() + "\"";
        }
        if (value instanceof ArrayList) {
            ArrayList<?> list = (ArrayList<?>) value;
            List<String> elements = new ArrayList<>();
            for (Object elem : list) {
                elements.add(convertToCypherValue(elem)); // 递归转换
            }
            return "[" + String.join(", ", elements) + "]";
        }

        // If the value is of an unsupported type, throw an exception or return as is
        throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
    }

    //提取来自于节点和关系的属性值
    public Map<String, Object> extractVartoProperties() {
        Map<String, Object> varToProperties = new HashMap<>();

        // 遍历 NodeVariableManager 的每一项
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

        /*// 遍历 AsVariableManager 的每一项
        Map<String, AsVariableManager.AliasTarget> aliasVariables = asVariableManager.getAllAliases();

        for (Map.Entry<String, AsVariableManager.AliasTarget> entry : aliasVariables.entrySet()) {
            String aliasVariable = entry.getKey();
            AsVariableManager.AliasTarget aliasTarget = entry.getValue();

            // 检查 AliasTarget 的类型
            if (aliasTarget.getType() == Integer.class || aliasTarget.getType() == String.class
                    || aliasTarget.getType() == Boolean.class) {
                // 将别名变量和目标值加入 varToProperties
                varToProperties.put(aliasVariable, aliasTarget.getTarget());
            }
            *//*if(aliasTarget.getType()==ExprUnknownVal.class){
                (ExprUnknownVal)aliasTarget.getAssociatedClass()==Integer.class
            }*//*
        }*/

        return varToProperties;
    }

    //提取来自节点，关系，路径的函数值
    /*public Map<String, Object> extractTargetFunctionMap() {
        Map<String, Object> functionMap = new HashMap<>();

        // 处理节点变量
        for (String nodeVariable : nodeVariableManager.getAllNodeVariables()) {
            for (AbstractNode.NodeFunction function : AbstractNode.NodeFunction.values()) {
                String functionKey = function.getFunctionName() + "(" + nodeVariable + ")";
                Object unknownValue = createUnknownValue(function.getReturnType());
                functionMap.put(functionKey, unknownValue);
            }
        }

        // 处理关系变量
        for (String relationshipVariable : relationshipVariableManager.getAllRelationshipVariables()) {
            for (AbstractRelationship.RelationshipFunction function : AbstractRelationship.RelationshipFunction.values()) {
                String functionKey = function.getFunctionName() + "(" + relationshipVariable + ")";
                Object unknownValue = createUnknownValue(function.getReturnType());
                functionMap.put(functionKey, unknownValue);
            }
        }

        // 处理路径变量
        for (String pathVariable : pathVariableManager.getAllPathVariables()) {
            for (AbstractPath.PathFunction function : AbstractPath.PathFunction.values()) {
                String functionKey = function.getFunctionName() + "(" + pathVariable + ")";
                Object unknownValue = createUnknownValue(function.getReturnType());
                functionMap.put(functionKey, unknownValue);
            }
        }

        return functionMap;
    }*/

    public Map<String, Object> extractTargetFunctionMap() {
        Map<String, Object> functionMap = new HashMap<>();

        // 处理节点变量
        for (String nodeVariable : nodeVariableManager.getAllNodeVariables()) {
            for (AbstractNode.NodeFunction function : AbstractNode.NodeFunction.values()) {
                // Memgraph 不支持对节点类型使用 MAX/MIN 聚合
                if ("memgraph".equalsIgnoreCase(options.getTargetDb()) &&
                        (function == AbstractNode.NodeFunction.MAX || function == AbstractNode.NodeFunction.MIN)) {
                    continue;
                }
                String functionKey = function.getFunctionName() + "(" + nodeVariable + ")";
                Object unknownValue = createUnknownValue(function.getReturnType());
                functionMap.put(functionKey, unknownValue);
            }
        }

        // 处理关系变量
        for (String relationshipVariable : relationshipVariableManager.getAllRelationshipVariables()) {
            for (AbstractRelationship.RelationshipFunction function : AbstractRelationship.RelationshipFunction.values()) {
                // Memgraph 不支持对关系类型使用 MAX/MIN 聚合
                if ("memgraph".equalsIgnoreCase(options.getTargetDb()) &&
                        (function == AbstractRelationship.RelationshipFunction.MAX || function == AbstractRelationship.RelationshipFunction.MIN)) {
                    continue;
                }
                String functionKey = function.getFunctionName() + "(" + relationshipVariable + ")";
                Object unknownValue = createUnknownValue(function.getReturnType());
                functionMap.put(functionKey, unknownValue);
            }
        }

        // 处理路径变量
        for (String pathVariable : pathVariableManager.getAllPathVariables()) {
            for (AbstractPath.PathFunction function : AbstractPath.PathFunction.values()) {
                // Memgraph 不支持对路径类型使用 MAX/MIN 聚合
                if ("memgraph".equalsIgnoreCase(options.getTargetDb()) &&
                        (function == AbstractPath.PathFunction.MAX || function == AbstractPath.PathFunction.MIN)) {
                    continue;
                }
                String functionKey = function.getFunctionName() + "(" + pathVariable + ")";
                Object unknownValue = createUnknownValue(function.getReturnType());
                functionMap.put(functionKey, unknownValue);
            }
        }

        return functionMap;
    }

    /**
     * 根据函数的返回类型生成未知值。
     *
     * @param returnType 函数的返回类型。
     * @return 对应的未知值。
     */
    private Object createUnknownValue(Class<?> returnType) {
        if (returnType.equals(Integer.class)) {
            return ExprUnknownVal.UNKNOWN_INTEGER;
        } else if (returnType.equals(String.class)) {
            return ExprUnknownVal.UNKNOWN_STRING;
        } else if (returnType.equals(AbstractNode.class)) {
            return ExprUnknownVal.UNKNOWN_NODE;
        } else if (returnType.equals(AbstractRelationship.class)) {
            return ExprUnknownVal.UNKNOWN_RELATIONSHIP;
        } else if (returnType.equals(AbstractPath.class)) {
            return ExprUnknownVal.UNKNOWN_PATH;
        }else if (returnType.equals(List.class)) {
            return ExprUnknownVal.UNKNOWN_LIST;
        } else {
            return null; // 对于未知类型返回 null
        }
    }

    //提取来自asvariablemanager的可用属性值
    public Map<String, Object> extractAliasProperties(){

        Map<String, Object> results=new HashMap<>();
        // 遍历 AsVariableManager 的每一项
        Map<String, AsVariableManager.AliasTarget> aliasVariables = asVariableManager.getAllAliases();

        for (Map.Entry<String, AsVariableManager.AliasTarget> entry : aliasVariables.entrySet()) {
            String aliasVariable = entry.getKey();
            AsVariableManager.AliasTarget aliasTarget = entry.getValue();

            // 检查 AliasTarget 的类型
            if (aliasTarget.getType() == Integer.class || aliasTarget.getType() == String.class
                    || aliasTarget.getType() == Boolean.class) {
                // 将别名变量和目标值加入 varToProperties
                results.put(aliasVariable, aliasTarget.getTarget());
            }
            if (aliasTarget.getType() == ExprUnknownVal.class) {
                ExprUnknownVal unknownVal = (ExprUnknownVal) aliasTarget.getTarget();
                if (unknownVal.getAssociatedClass() == Integer.class) {
                    results.put(aliasVariable, unknownVal);
                }
            }
        }
            return results;

    }

    //提取节点或关系的id函数
    public Map<String, Object> extractIdFunctionMap() {
        Map<String, Object> functionMap = new HashMap<>();

        // 处理节点变量
        for (String nodeVariable : nodeVariableManager.getAllNodeVariables()) {

                String functionKey = "id(" + nodeVariable + ")";
                Object unknownValue = ExprUnknownVal.UNKNOWN_INTEGER;
                functionMap.put(functionKey, unknownValue);

        }

        // 处理关系变量
        for (String relationshipVariable : relationshipVariableManager.getAllRelationshipVariables()) {
            String functionKey = "id(" + relationshipVariable + ")";
            Object unknownValue = ExprUnknownVal.UNKNOWN_INTEGER;
            functionMap.put(functionKey, unknownValue);
        }

        return functionMap;
    }







}
