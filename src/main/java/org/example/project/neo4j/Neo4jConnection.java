package org.example.project.neo4j;

import org.example.project.GlobalState;
import org.example.project.common.query.projectResultSet;
import org.example.project.cypher.CypherConnection;
import org.neo4j.driver.*;

import java.util.Arrays;
import java.util.List;

public class Neo4jConnection extends CypherConnection {


    private Driver driver;
    private Neo4jOptions options;

    public Neo4jConnection(Driver driver, Neo4jOptions options){
        this.driver = driver;
        this.options = options;
    }


    @Override
    public String getDatabaseVersion() throws Exception {
        //todo complete
        return "neo4j";
    }

    @Override
    public void close() throws Exception {
//        Neo4jDriverManager.closeDriver(driver);
    }

    @Override
    public void executeStatement(String arg) throws Exception{
        try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    tx.run(arg);
                    return "";
                }
            } );
            //System.out.println( greeting );
        }
    }


    @Override
    public List<projectResultSet> executeStatementAndGet(String arg) throws Exception{
        try ( Session session = driver.session() )
        {
            projectResultSet resultSet = new projectResultSet(session.run(arg));
            resultSet.resolveFloat();
            return Arrays.asList(resultSet);
        }
    }
}
