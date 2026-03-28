package org.example.project.cypher;

import org.apache.commons.lang3.tuple.Pair;
import org.example.project.GlobalState;
import org.example.project.projectDBConnection;
import org.example.project.common.query.projectResultSet;

import java.util.List;

public abstract class CypherConnection implements projectDBConnection {

    public void executeStatement(String arg) throws Exception{
        System.out.println("execute statement: "+arg);
    }

    public List<projectResultSet> executeStatementAndGet(String arg) throws Exception{
        System.out.println("execute statement: "+arg);
        return null;
    }

    public List<Long> executeStatementAndGetTime(String arg) throws Exception{
        return null;
    }

    public List<Long> executeFirstQueryAndGetTimes(String arg) throws Exception{
        return null;
    }

    public List<Pair<projectResultSet,Long>> executeFirstQueryAndGetResultsWithTime(String arg) throws Exception{
        return null;
    }
}
