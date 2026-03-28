package org.example.project.memGraph;

import com.google.gson.JsonObject;
import org.example.project.GlobalState;
import org.example.project.MainOptions;
import org.example.project.common.log.LoggableFactory;
import org.example.project.cypher.*;
import org.example.project.neo4j.Neo4jOptions;
import org.neo4j.driver.Driver;

import java.util.List;

public class MemGraphProvider extends CypherProviderAdapter< MemGraphOptions> {
    public MemGraphProvider() {
        super( MemGraphOptions.class);
    }

    @Override
    public CypherConnection createDatabase(GlobalState globalState) throws Exception {
        MainOptions mainOptions = globalState.getOptions();
        if (globalState.getDbmsSpecificOptions() instanceof MemGraphOptions) {
            MemGraphOptions Options = (MemGraphOptions) globalState.getDbmsSpecificOptions();
            return createDatabaseWithOptions(mainOptions, Options);
        } else {
            throw new IllegalArgumentException("Expected MemGraphOptions but got " + globalState.getDbmsSpecificOptions().getClass().getSimpleName());
        }
    }

    @Override
    public String getDBMSName() {
        return "memgraph";
    }

    @Override
    public LoggableFactory getLoggableFactory() {
        return new CypherLoggableFactory();
    }





    @Override
    public MemGraphOptions generateOptionsFromConfig(JsonObject config) {
        return MemGraphOptions.parseOptionFromFile(config);
    }

    @Override
    public CypherConnection createDatabaseWithOptions(MainOptions mainOptions, MemGraphOptions specificOptions) throws Exception {
        String username = specificOptions.getUsername();
        String password = specificOptions.getPassword();
        String host = specificOptions.getHost();
        int port = specificOptions.getPort();
        if (host == null) {
            host = MemGraphOptions.DEFAULT_HOST;
        }
        if (port == MainOptions.NO_SET_PORT) {
            port = MemGraphOptions.DEFAULT_PORT;
        }

        String url = String.format("bolt://%s:%d", host, port);

        Driver driver = MemGraphDriverManager.getDriver(url, username, password);
        MemGraphConnection con = new MemGraphConnection(driver, specificOptions);
        con.executeStatement("MATCH (n) DETACH DELETE n");
        //con.executeStatement("CALL apoc.schema.assert({}, {})");
        return con;
    }
}
