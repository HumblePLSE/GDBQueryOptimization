package org.example.project.cypher.standard_ast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.example.project.Randomly;
/**
 * Represents an ORDER BY clause in a Cypher query.
 * ORDER BY specifies the sort order for the query results.
 */
public class OrderByClause extends ReadingSubClause {

    /**
     * A single entry in the ORDER BY clause, consisting of a variable name and a sort direction.
     */
    public static class OrderByEntry {
        private final String variable;
        private final boolean ascending; // true for ASC, false for DESC

        public OrderByEntry(String variable, boolean ascending) {
            this.variable = variable;
            this.ascending = ascending;
        }

        /**
         * Converts this entry to Cypher syntax.
         * @return Cypher representation of the entry.
         */
        public String toCypher() {
            return variable + (ascending ? " ASC" : " DESC");
        }

        public String getVariable() {
            return variable;
        }

        public boolean isAscending() {
            return ascending;
        }
    }

    private final List<OrderByEntry> entries;

    /**
     * Constructor for OrderByClause.
     * @param entries The list of entries specifying the sort order.
     */
    public OrderByClause(List<OrderByEntry> entries) {
        super("ORDER BY");
        this.entries = entries;
    }

    /**
     * Converts the ORDER BY clause to Cypher query syntax.
     * @return A String representing the Cypher syntax for this ORDER BY clause.
     */
    @Override
    public String toCypher() {
        if(entries.isEmpty()) return "";
        return "ORDER BY " + entries.stream()
                .map(OrderByEntry::toCypher)
                .collect(Collectors.joining(", "));
    }

    /**
     * Validates the ORDER BY clause to ensure it is well-formed.
     * A valid ORDER BY clause must have at least one entry with non-null variables.
     * @return True if the clause is valid, false otherwise.
     */
    @Override
    public boolean validate() {
        return entries != null && !entries.isEmpty() && entries.stream().allMatch(e -> e.getVariable() != null && !e.getVariable().isEmpty());
    }

    /**
     * Generates a random OrderByClause with random variables and sort directions.
     * @return A randomly generated OrderByClause.
     */
    public static OrderByClause generateRandomOrderByClause(List<String> availableVariables) {
        Randomly randomly = new Randomly();

        List<OrderByEntry> entries = new ArrayList<>();

        for (int i = 0; i < availableVariables.size(); i++) {
            String variable = availableVariables.get(i);
            boolean ascending = randomly.getBoolean(); // Randomly decide sort direction
            entries.add(new OrderByEntry(variable, ascending));
        }

        return new OrderByClause(entries);
    }

    public List<OrderByEntry> getEntries() {
        return entries;
    }
}
