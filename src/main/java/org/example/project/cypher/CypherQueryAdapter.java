package org.example.project.cypher;

import org.apache.commons.lang3.tuple.Pair;
import org.example.project.ExecutionTimer;
import org.example.project.GlobalState;
import org.example.project.common.query.ExpectedErrors;
import org.example.project.common.query.projectResultSet;
import org.example.project.common.query.Query;

import java.util.List;

public class CypherQueryAdapter extends Query<CypherConnection> {

    private final String query;

    public CypherQueryAdapter(String query) {
        this.query = query;
    }

    private String canonicalizeString(String s) {
        if (s.endsWith(";")) {
            return s;
        } else if (!s.contains("--")) {
            return s + ";";
        } else {
            // query contains a comment
            return s;
        }
    }

    @Override
    public String getLogString() {
        return getQueryString();
    }

    @Override
    public String getQueryString() {
        return query;
    }

    @Override
    public String getUnterminatedQueryString() {
        return canonicalizeString(query);
    }

    @Override
    public boolean couldAffectSchema() {
        return false;
    }



    @Override
    public boolean execute(GlobalState globalState, String... fills) throws Exception {

        if (globalState.getConnection() instanceof CypherConnection) {
            CypherConnection connection = (CypherConnection) globalState.getConnection();
            connection.executeStatement(query);
        } else {
            throw new IllegalArgumentException("Unsupported connection type: " + globalState.getConnection().getClass());
        }
        return true;
    }

    @Override
    public List<projectResultSet> executeAndGet(GlobalState globalState, String... fills) throws Exception {
        if (globalState.getConnection() instanceof CypherConnection) {
            CypherConnection connection = (CypherConnection) globalState.getConnection();
            return connection.executeStatementAndGet(query);
        }
        throw new IllegalArgumentException("Unsupported connection type: " + globalState.getConnection().getClass());

            //return globalState.getConnection().executeStatementAndGet(query);
    }

    @Override
    public List<Long> executeAndGetTime(GlobalState globalState, String... fills) throws Exception {
        if (globalState.getConnection() instanceof CypherConnection) {
            CypherConnection connection = (CypherConnection) globalState.getConnection();
           // List<Long> nowTime=connection.executeFirstQueryAndGetTimes(query);
            //List<Long> nowTime2=connection.executeFirstQueryAndGetTimes(query);
            //List<Long> originalTime=connection.executeStatementAndGetTime(query);//debug
            return connection.executeFirstQueryAndGetTimes(query);
            //return originalTime;
        }
        throw new IllegalArgumentException("Unsupported connection type: " + globalState.getConnection().getClass());

        //return globalState.getConnection().executeStatementAndGetTime(query);
    }

    public List<Pair<projectResultSet,Long>> executeFirstQueryAndGetResultsWithTime(GlobalState globalState, String... fills) throws Exception {
        // 记录查询语句
        if (globalState.getOptions().printAllStatements()) {
            System.out.println(getQueryString());
        }

        // 记录执行时间
        ExecutionTimer timer = new ExecutionTimer().start();
        globalState.getLogger().writeCurrent(query);
        globalState.getState().logStatement(query);

        boolean success = false;
        List<Pair<projectResultSet,Long>> results;
        if (globalState.getConnection() instanceof CypherConnection) {
            CypherConnection connection = (CypherConnection) globalState.getConnection();


            results=connection.executeFirstQueryAndGetResultsWithTime(query);
            if(results!=null) success=true;

        }
        else throw new IllegalArgumentException("Unsupported connection type: " + globalState.getConnection().getClass());

        // 记录执行时间
        if (globalState.getOptions().logExecutionTime()) {
            globalState.getLogger().writeCurrent(" -- " + timer.end().asString());
        }

        // 返回执行结果
        return results;

    }

    @Override
    public ExpectedErrors getExpectedErrors() {
        //todo complete
        return null;
    }

    /**
     * 统一的执行和记录方法
     */
    public boolean executeAndLog(GlobalState globalState, String... fills) throws Exception {
        // 记录查询语句
        if (globalState.getOptions().printAllStatements()) {
            System.out.println(getQueryString());
        }

        // 记录执行时间
        ExecutionTimer timer = new ExecutionTimer().start();
        globalState.getLogger().writeCurrent(query);
        globalState.getState().logStatement(query);

        // 执行查询
        boolean success = false;
        if (globalState.getConnection() instanceof CypherConnection) {
            CypherConnection connection = (CypherConnection) globalState.getConnection();
            connection.executeStatement(query);
            success=true;
        } else {
            throw new IllegalArgumentException("Unsupported connection type: " + globalState.getConnection().getClass());
        }


        // 记录执行时间
        if (globalState.getOptions().logExecutionTime()) {
            globalState.getLogger().writeCurrent(" -- " + timer.end().asString());
        }

        // 返回执行结果
        return success;
    }

    public List<projectResultSet> executeGetAndLog(GlobalState globalState, String... fills) throws Exception {
        // 记录查询语句
        if (globalState.getOptions().printAllStatements()) {
            System.out.println(getQueryString());
        }

        // 记录执行时间
        ExecutionTimer timer = new ExecutionTimer().start();
        globalState.getLogger().writeCurrent(query);
        globalState.getState().logStatement(query);

        // 执行查询
        boolean success = false;
        List<projectResultSet> results;
        if (globalState.getConnection() instanceof CypherConnection) {
            CypherConnection connection = (CypherConnection) globalState.getConnection();
            results=connection.executeStatementAndGet(query);
            if(results!=null) success=true;
        } else {
            throw new IllegalArgumentException("Unsupported connection type: " + globalState.getConnection().getClass());
        }


        // 记录执行时间
        if (globalState.getOptions().logExecutionTime()) {
            globalState.getLogger().writeCurrent(" -- " + timer.end().asString());
        }

        // 返回执行结果
        return results;
    }

    public List<Long> executeGetTimeAndLog(GlobalState globalState, String... fills) throws Exception {
        // 记录查询语句
        if (globalState.getOptions().printAllStatements()) {
            System.out.println(getQueryString());
        }

        // 记录执行时间
        ExecutionTimer timer = new ExecutionTimer().start();
        globalState.getLogger().writeCurrent(query);
        globalState.getState().logStatement(query);

        // 执行查询
        boolean success = false;
        List<Long> results;
        if (globalState.getConnection() instanceof CypherConnection) {
            CypherConnection connection = (CypherConnection) globalState.getConnection();
            results=connection.executeStatementAndGetTime(query);
            if(results!=null) success=true;
        } else {
            throw new IllegalArgumentException("Unsupported connection type: " + globalState.getConnection().getClass());
        }


        // 记录执行时间
        if (globalState.getOptions().logExecutionTime()) {
            globalState.getLogger().writeCurrent(" -- " + timer.end().asString());
        }

        // 返回执行结果
        return results;
    }
}
