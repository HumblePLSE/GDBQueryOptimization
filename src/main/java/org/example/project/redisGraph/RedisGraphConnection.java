package org.example.project.redisGraph;

import org.example.project.common.query.projectResultSet;
import org.example.project.cypher.CypherConnection;
import org.example.project.exceptions.MustRestartDatabaseException;
import redis.clients.jedis.JedisPooled;

import java.util.Arrays;
import java.util.List;

public class RedisGraphConnection extends CypherConnection {

    private final JedisPooled graph;
    private String graphName;

    private RedisGraphOptions options;

    public RedisGraphConnection(RedisGraphOptions options, JedisPooled graph, String graphName){
         this.graph = graph;
         this.graphName = graphName;
         this.options = options;
    }


    @Override
    public String getDatabaseVersion() {
        return "redisgraph";
    }

    @Override
    public void close() throws Exception {
        graph.close();
    }

    @Override
    public void executeStatement(String arg) throws Exception{
        try{
            graph.graphQuery(graphName, arg, 10000);
        } catch (redis.clients.jedis.exceptions.JedisConnectionException e){
            e.printStackTrace();
            Process process = Runtime.getRuntime().exec(options.restartCommand);
            process.waitFor();
            Thread.sleep(10000);
            throw new MustRestartDatabaseException(e);
        }

    }

    @Override
    public List<projectResultSet> executeStatementAndGet(String arg) throws Exception{
        try{
            return Arrays.asList(new projectResultSet(graph.graphQuery(graphName, arg, 10000)));
        } catch (redis.clients.jedis.exceptions.JedisConnectionException e){
            System.out.println("got jedis crashed");
            e.printStackTrace();
            Process process = Runtime.getRuntime().exec(options.restartCommand);
            process.waitFor();
            Thread.sleep(10000);
            throw new MustRestartDatabaseException(e);
        }
    }
}
