package org.example.project.cypher.gen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages path variables used in Cypher queries.
 * It generates path variables with sequential names (e.g., p0, p1, p2)
 * and stores them in a map with path names as keys and AbstractPath objects as values.
 */
public class PathVariableManager {

    private final Map<String, AbstractPath> pathVariables; // Stores path variable names and their corresponding AbstractPath objects
    private int pathIndex; // Counter for generating unique path variable names

    public PathVariableManager() {
        this.pathVariables = new HashMap<>();
        this.pathIndex = 0; // Start from p0
    }

    public void setIndex(int index){pathIndex=index;}

    public int getPathIndex(){return pathIndex;}

    public PathVariableManager Copy() {
        PathVariableManager copy = new PathVariableManager();
        copy.pathIndex = this.pathIndex; // 复制基本类型计数器

        // 浅拷贝 Map 结构，共享路径对象
        copy.pathVariables.putAll(this.pathVariables);

        return copy;
    }

    /**
     * Get all path variable names stored in the manager.
     *
     * @return A List of all path variable names.
     */
    public List<String> getAllPathVariables() {
        // Collect all keys (path variable names) from the map
        return pathVariables.keySet().stream().collect(Collectors.toList());
    }

    /**
     * Generate a new path variable (p0, p1, p2, ...).
     * Creates a new AbstractPath and stores it in the map.
     * @return The generated path variable name.
     */
    public String generatePathVariable() {
        // Generate the path variable name based on the current count
        String pathVariable = "p" + pathIndex;

        // Create a new AbstractPath object
        AbstractPath newPath = new AbstractPath(pathIndex, pathVariable);

        // Add the path variable and its AbstractPath object to the map
        pathVariables.put(pathVariable, newPath);

        // Increment the counter for the next path variable
        pathIndex++;

        return pathVariable;
    }

    public String generateAliasVariable(AbstractPath path,int aliasindex){
        String alias="alias"+aliasindex;

        // Store the node with its variable name
        pathVariables.put(alias, path);

        // Increment the counter for the next alias
        // 目前的实现是针对于node，relatioship，path的变量存储在对应的variablemanager而非asvariablemanager中
        // aliasindex++在asvariablemanager中实现
        //aliasindex++;

        return alias;
    }

    /**
     * Get the AbstractPath object associated with a path variable name.
     * @param pathVariable The name of the path variable.
     * @return The corresponding AbstractPath object, or null if it doesn't exist.
     */
    public AbstractPath getPathVariable(String pathVariable) {
        return pathVariables.get(pathVariable);
    }

    /**
     * Get all generated path variables (AbstractPath objects).
     * @return A list of all AbstractPath objects.
     */
    public List<AbstractPath> getPathVariables() {
        return pathVariables.values().stream().collect(Collectors.toList());
    }

    /**
     * Clean up all path variables, typically at the end of a query's lifecycle.
     */
    public void cleanUpExpiredVariables() {
        pathVariables.clear(); // Clear all path variables
        pathIndex = 0; // Reset the path index
    }

    public java.util.Map<String, AbstractPath> getPathVariableMap() {
        return pathVariables;
    }

    /**
     * Get the current number of stored path variables.
     * @return The count of stored path variables.
     */
    public int getPathVariableCount() {
        return pathVariables.size();
    }

    /**
     * Retain the specified path variables, removing any that are not in the retained list.
     * @param retainedVariables A list of path variables to retain.
     */
    public void retainVariables(List<String> retainedVariables) {
        pathVariables.entrySet().removeIf(entry -> !retainedVariables.contains(entry.getKey()));
    }

    /**
     * Removes a path variable from the manager by its variable name.
     * @param pathVariable The name of the path variable to remove.
     */
    public void removePathVariable(String pathVariable) {
        pathVariables.remove(pathVariable); // Remove the path variable from the map
    }

    public void addVariables(Map<String, AbstractPath> variables) {
        pathVariables.putAll(variables);

    }
}
