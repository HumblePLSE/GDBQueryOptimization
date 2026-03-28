package org.example.project.neo4j;

import com.google.gson.JsonObject;
import org.example.project.GlobalState;
import org.example.project.MainOptions;
import org.example.project.common.log.LoggableFactory;

import org.example.project.cypher.*;

import org.neo4j.driver.Driver;

public class Neo4jProvider extends CypherProviderAdapter<Neo4jOptions> {
    public Neo4jProvider() {
        super(Neo4jOptions.class);
    }

    @Override
    public CypherConnection createDatabase(GlobalState globalState) throws Exception {
        MainOptions mainOptions = globalState.getOptions();
        if (globalState.getDbmsSpecificOptions() instanceof Neo4jOptions) {
            Neo4jOptions neo4jOptions = (Neo4jOptions) globalState.getDbmsSpecificOptions();
            return createDatabaseWithOptions(mainOptions, neo4jOptions);
        } else {
            throw new IllegalArgumentException("Expected Neo4jOptions but got " + globalState.getDbmsSpecificOptions().getClass().getSimpleName());
        }
    }

    @Override
    public Neo4jOptions generateOptionsFromConfig(JsonObject config) {
        return Neo4jOptions.parseOptionFromFile(config);
    }

    @Override
    public CypherConnection createDatabaseWithOptions(MainOptions mainOptions, Neo4jOptions specificOptions) throws Exception {
        String username = specificOptions.getUsername();
        String password = specificOptions.getPassword();
        String host = specificOptions.getHost();
        int port = specificOptions.getPort();
        if (host == null) {
            host = Neo4jOptions.DEFAULT_HOST;
        }
        if (port == MainOptions.NO_SET_PORT) {
            port = Neo4jOptions.DEFAULT_PORT;
        }

        CypherConnection con;
        if(specificOptions.proxyPort == 0){
            String url = String.format("bolt://%s:%d", host, port);
            Driver driver = Neo4jDriverManager.getDriver(url, username, password);
            con = new Neo4jConnection(driver, specificOptions);
        }
        else{
            con = new Neo4jProxyConnection(specificOptions);
        }
        con.executeStatement("MATCH (n) DETACH DELETE n");
//        con.executeStatement("CALL apoc.schema.assert({}, {})");

        return con;
    }

    @Override
    public String getDBMSName() {
        return "neo4j";
    }

    @Override
    public LoggableFactory getLoggableFactory() {
        return new CypherLoggableFactory();
    }



    // 你可以添加其他方法或 Neo4j 特定的逻辑
}

