package org.example.project.composite;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.project.*;
import org.example.project.common.log.LoggableFactory;
import org.example.project.cypher.*;
import org.example.project.neo4j.Neo4jOptions;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CompositeProvider extends CypherProviderAdapter<CompositeOptions> {
    public CompositeProvider() {
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
        return "composite";
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

            /*// 提取所有版本号并存入 mainOptions
            List<String> versions = new ArrayList<>();
            for(String nameWithVersion : databaseNamesWithVersion) {
                if(nameWithVersion.contains("@")) {
                    String[] parts = nameWithVersion.split("@");
                    if(parts.length == 2) {
                        versions.add(parts[1]);
                    }
                }
            }
            mainOptions.setAvailableVersions(versions);*/

            for(String nameWithVersion : databaseNamesWithVersion){
                for(DatabaseProvider provider: Main.getDBMSProviders()){
                    String databaseName = provider.getDBMSName().toLowerCase();
                    MainOptions options = mainOptions;
                    if(nameWithVersion.contains(databaseName)){
                        FileWriter fw = null;
                        try {
                            File f = new File("./logs/gdb_version.txt");
                            fw = new FileWriter(f, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        PrintWriter pw = new PrintWriter(fw);
                        pw.println(nameWithVersion + "\n");
                        pw.flush();
                        try {
                            fw.flush();
                            pw.close();
                            fw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        DBMSSpecificOptions command = ((CypherProviderAdapter)provider)
                                .generateOptionsFromConfig(jsonObject.getAsJsonObject(nameWithVersion));
                        connections.add(((CypherProviderAdapter)provider).createDatabaseWithOptions(options, command));
                        break;
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
