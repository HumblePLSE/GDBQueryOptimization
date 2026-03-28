package org.example.project.cypher.gen;

import org.example.project.Randomly;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages aliases used in Cypher queries.
 * It stores aliases, their associated targets, and provides functionality to manage them.
 */
public class AsVariableManager {

    public static class AliasTarget {
        private final Object target; // The target object (e.g., AbstractNode, AbstractRelationship, etc.)
        private final Class<?> type; // The class type of the target object

        public AliasTarget(Object target, Class<?> type) {
            this.target = target;
            this.type = type;
        }

        public Object getTarget() {
            return target;
        }

        public Class<?> getType() {
            return type;
        }
    }

    private final Map<String, AliasTarget> aliasMap; // Map to store alias and its target object
    private int aliasindex=0;

    public void setIndex(int index){aliasindex=index;}

    public AsVariableManager Copy() {
        AsVariableManager copy = new AsVariableManager();
        copy.aliasindex = this.aliasindex; // 复制计数器

        // 浅拷贝 Map 结构，共享 AliasTarget 对象
        copy.aliasMap.putAll(this.aliasMap);

        return copy;
    }

    public int getAliasindex() {
        return aliasindex;
    }

    public void addAliasindex(){aliasindex++;}

    public AsVariableManager() {
        this.aliasMap = new HashMap<>();
    }

    /**
     * Adds a new alias to the map.
     *
     * @param target The object to associate with the alias.
     */
    public String addAlias(Object target) {
        String alias="alias"+aliasindex;
        aliasindex++;
        if (aliasMap.containsKey(alias)) {
            throw new IllegalArgumentException("Alias '" + alias + "' already exists.");
        }

        aliasMap.put(alias, new AliasTarget(target, target.getClass()));
        return alias;
    }

    public void putAlias(String alias,Object target){
        aliasMap.put(alias, new AliasTarget(target, target.getClass()));
    }

    /**
     * Retrieves the target object associated with an alias.
     *
     * @param alias The alias name.
     * @return The target object or null if the alias does not exist.
     */
    public Object getTarget(String alias) {
        AliasTarget aliasTarget = aliasMap.get(alias);
        return aliasTarget != null ? aliasTarget.getTarget() : null;
    }

    /**
     * Retrieves the target object associated with an alias as a specific type.
     *
     * @param alias The alias name.
     * @param clazz The expected class type of the target.
     * @param <T>   The type parameter for the expected type.
     * @return The target object cast to the expected type.
     */
    public <T> T getTargetAs(String alias, Class<T> clazz) {
        AliasTarget aliasTarget = aliasMap.get(alias);
        if (aliasTarget != null && aliasTarget.getType().equals(clazz)) {
            return clazz.cast(aliasTarget.getTarget());
        }
        throw new IllegalArgumentException("Alias '" + alias + "' does not match the expected type: " + clazz.getName());
    }

    /**
     * Removes an alias from the map.
     *
     * @param alias The alias name to remove.
     */
    public void removeAlias(String alias) {
        aliasMap.remove(alias);
    }

    /**
     * Clears all aliases from the map.
     */
    public void cleanUpExpiredVariables() {
        aliasMap.clear();
        aliasindex=0;
    }

    /**
     * Retrieves a random alias of the specified type.
     *
     * @param clazz The class type to filter aliases.
     * @return A random alias of the specified type, or null if no such alias exists.
     */
    public String getRandomAlias(Class<?> clazz) {
        List<String> filteredAliases = aliasMap.entrySet().stream()
                .filter(entry -> entry.getValue().getType().equals(clazz))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (filteredAliases.isEmpty()) {
            return null; // No aliases of the specified type
        }

        Randomly randomly = new Randomly();
        return filteredAliases.get(randomly.getInteger(0, filteredAliases.size()));
    }

    /**
     * Retrieves all aliases as a Map.
     *
     * @return The complete alias map.
     */
    public Map<String, AliasTarget> getAllAliases() {
        return new HashMap<>(aliasMap);
    }

    public List<String> getAllVariableNames(){
        return new ArrayList<>(aliasMap.keySet());
    }

    public void retainVariables(List<String> retainedVariables) {
        aliasMap.keySet().retainAll(retainedVariables); // 仅保留传递的变量
    }

    /**
     * Retrieves all variable names associated with a specific target type.
     *
     * @param clazz The class type of the target object (e.g., AbstractNode, AbstractRelationship).
     * @return A list of variable names (aliases) associated with the given target type.
     */
    public List<String> getTargetTypeVariableNames(Class<?> clazz) {
        List<String> variableNames = new ArrayList<>();

        // Iterate through the aliasMap and filter based on the target type
        for (Map.Entry<String, AliasTarget> entry : aliasMap.entrySet()) {
            AliasTarget aliasTarget = entry.getValue();

            // If the type of the target matches the provided class, add the alias (variable name)
            /*if (aliasTarget.getType().equals(clazz)) {
                variableNames.add(entry.getKey());  // Add the alias name
            }*/
            if (clazz.isAssignableFrom(aliasTarget.getType())) {
                variableNames.add(entry.getKey());
            }
        }

        return variableNames;
    }

    public void addVariables(Map<String, AliasTarget> variables) {
        aliasMap.putAll(variables);
    }

}

