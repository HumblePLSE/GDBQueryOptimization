package org.example.project.cypher;

import org.example.project.GlobalState;
import org.example.project.cypher.CypherQueryAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CypherExecutor {

    /**
     * Executes Cypher queries from a file, one per line.
     *
     * @param filePath     Path to the file containing Cypher queries.
     * @param globalState  Global state used for executing and logging queries.
     */
    public void executeQueriesFromFile(String filePath, GlobalState globalState) {
        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            System.err.println("Invalid file path: " + filePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int queryCount = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip empty lines or comments (assuming comments start with //)
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }

                CypherQueryAdapter query = new CypherQueryAdapter(line);

                try {
                    boolean success = query.executeAndLog(globalState);

                    if (success) {
                        globalState.getState().logCreateStatement(query);
                        queryCount++;
                        System.out.println("Successfully executed query: " + line);
                    } else {
                        System.err.println("Query execution failed: " + line);
                    }
                } catch (Exception e) {
                    System.err.println("Error executing query: " + line);
                    e.printStackTrace();
                    // Continue with the next query if an exception occurs
                }
            }

            System.out.println("Total executed queries: " + queryCount);
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
            e.printStackTrace();
        }
    }
}
