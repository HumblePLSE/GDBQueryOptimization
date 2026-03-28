package org.example.project.redisGraph;

import com.google.gson.JsonObject;
import org.example.project.*;
import org.example.project.MainOptions;
import org.example.project.common.log.LoggableFactory;
import org.example.project.cypher.*;
import org.example.project.neo4j.Neo4jOptions;
import redis.clients.jedis.JedisPooled;

import java.util.List;

public class RedisGraphProvider extends CypherProviderAdapter<RedisGraphOptions> {
    public RedisGraphProvider() {
        super( RedisGraphOptions.class);
    }

    @Override
    public CypherConnection createDatabase(GlobalState globalState) throws Exception {
        MainOptions mainOptions = globalState.getOptions();
        if (globalState.getDbmsSpecificOptions() instanceof RedisGraphOptions) {
            RedisGraphOptions Options = (RedisGraphOptions) globalState.getDbmsSpecificOptions();
            return createDatabaseWithOptions(mainOptions, Options);
        } else {
            throw new IllegalArgumentException("Expected Neo4jOptions but got " + globalState.getDbmsSpecificOptions().getClass().getSimpleName());
        }
    }

    @Override
    public String getDBMSName() {
        return "redisgraph";
    }

    @Override
    public LoggableFactory getLoggableFactory() {
        return new CypherLoggableFactory();
    }




    @Override
    public RedisGraphOptions generateOptionsFromConfig(JsonObject config) {
        return RedisGraphOptions.parseOptionFromFile(config);
    }

    @Override
    public CypherConnection createDatabaseWithOptions(MainOptions mainOptions, RedisGraphOptions specificOptions) throws Exception {
        String username = specificOptions.getUsername();
        String password = specificOptions.getPassword();
        String host = specificOptions.getHost();
        int port = specificOptions.getPort();
        if (host == null) {
            host = RedisGraphOptions.DEFAULT_HOST;
        }
        if (port == MainOptions.NO_SET_PORT) {
            port = RedisGraphOptions.DEFAULT_PORT;
        }
        RedisGraphConnection con = null;
        try{
            con = new RedisGraphConnection(specificOptions, new JedisPooled(host, port), "sqlancer");
            con.executeStatement("MATCH (n) DETACH DELETE n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }
}
