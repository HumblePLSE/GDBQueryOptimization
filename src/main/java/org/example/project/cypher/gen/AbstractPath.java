package org.example.project.cypher.gen;

import java.util.List;

public class AbstractPath {

    private int id; // 用于标识路径对象的唯一标识符
    private String pathName; // 用于标识路径的名称或标识符

    // 构造函数
    public AbstractPath(int id, String pathName) {
        this.id = id;
        this.pathName = pathName;
    }

    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    // 重写 toString 方法，便于输出
    @Override
    public String toString() {
        return "AbstractPath{" +
                "id=" + id +
                ", pathName='" + pathName + '\'' +
                '}';
    }

    public enum PathFunction {
        LENGTH("length", Integer.class), // 返回路径的长度（关系数量）
        //START_NODE("startNode", AbstractNode.class), // 返回路径的起始节点
        //END_NODE("endNode", AbstractNode.class), // 返回路径的终止节点
        //RELATIONSHIPS("relationships", List.class), // 返回路径中的关系集合
        //NODES("nodes", List.class), // 返回路径中的节点集合
        MAX("max", AbstractPath.class), // 返回具有最大属性值的路径
        MIN("min", AbstractPath.class), // 返回具有最小属性值的路径
        COUNT("count", Integer.class); // 统计路径的数量

        private final String functionName;
        private final Class<?> returnType;

        PathFunction(String functionName, Class<?> returnType) {
            this.functionName = functionName;
            this.returnType = returnType;
        }

        public String getFunctionName() {
            return functionName;
        }

        public Class<?> getReturnType() {
            return returnType;
        }
    }

}
