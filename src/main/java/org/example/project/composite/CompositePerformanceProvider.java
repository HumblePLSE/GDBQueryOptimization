package org.example.project.composite;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.project.*;
import org.example.project.common.log.LoggableFactory;
import org.example.project.common.oracle.TestOracle;
import org.example.project.cypher.CypherConnection;
import org.example.project.cypher.CypherLoggableFactory;
import org.example.project.cypher.CypherProviderAdapter;
import org.example.project.cypher.CypherQueryAdapter;
import org.example.project.neo4j.Neo4jOptions;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CompositePerformanceProvider extends CypherProviderAdapter<CompositeOptions> {
    public CompositePerformanceProvider() {
        super( CompositeOptions.class);
    }

    @Override
    public CypherConnection createDatabase(GlobalState globalState) throws Exception {
        MainOptions mainOptions = globalState.getOptions();
        if (globalState.getDbmsSpecificOptions() instanceof CompositeOptions) {
            CompositeOptions Options = (CompositeOptions) globalState.getDbmsSpecificOptions();
            return createDatabaseWithOptions(mainOptions, Options);
        } else {
            throw new IllegalArgumentException("Expected CompositeOptions but got " + globalState.getDbmsSpecificOptions().getClass().getSimpleName());
        }
    }



    @Override
    public String getDBMSName() {
        return "org/example/gdsmith/performance";
    }

    @Override
    public LoggableFactory getLoggableFactory() {
        return new CypherLoggableFactory();
    }



    @Override
    public CompositeOptions generateOptionsFromConfig(JsonObject config) {
        return null;
    }

    @Override
    public CypherConnection createDatabaseWithOptions(MainOptions mainOptions, CompositeOptions specificOptions) throws Exception {
        List<CypherConnection> connections = new ArrayList<>();
        Gson gson = new Gson();
        try {
            FileReader fileReader = new FileReader(specificOptions.getConfigPath());
            JsonObject jsonObject = gson.fromJson(fileReader, JsonObject.class);
            Set<String> databaseNamesWithVersion = jsonObject.keySet();
            for(DatabaseProvider provider: Main.getDBMSProviders()){
                String databaseName = provider.getDBMSName().toLowerCase();
                MainOptions options = mainOptions;
                for(String nameWithVersion : databaseNamesWithVersion){
                    if(nameWithVersion.contains(provider.getDBMSName().toLowerCase())){
                        DBMSSpecificOptions command = ((CypherProviderAdapter)provider)
                                .generateOptionsFromConfig(jsonObject.getAsJsonObject(nameWithVersion));
                        connections.add(((CypherProviderAdapter)provider).createDatabaseWithOptions(options, command));
                    }
                }

            }
            System.out.println("success");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        CompositeConnection compositeConnection = new CompositeConnection(connections, mainOptions);
        return compositeConnection;
    }
}
