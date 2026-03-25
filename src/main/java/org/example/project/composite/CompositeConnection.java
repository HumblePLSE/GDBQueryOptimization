package org.example.project.composite;

import org.apache.commons.lang3.tuple.Pair;
import org.example.project.GlobalState;
import org.example.project.MainOptions;
import org.example.project.common.query.projectResultSet;
import org.example.project.cypher.CypherConnection;
import org.example.project.exceptions.DatabaseCrashException;
import org.example.project.exceptions.MultipleExceptionsOccurredException;
import org.example.project.exceptions.MustRestartDatapackage org.example.project.composite;

import org.apache.commons.lang3.tuple.Pair;
import org.example.project.GlobalState;
import org.example.project.MainOptions;
import org.example.project.common.query.projectResultSet;
import org.example.project.cypher.CypherConnection;
import org.example.project.exceptions.DatabaseCrashException;
import org.example.project.exceptions.MultipleExceptionsOccurredException;
import org.example.project.exceptions.MustRestartDatabaseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class CompositeConnection extends CypherConnection {

    private List<CypherConnection> connections;
    public static final Long TIMEOUT = 60000L;

    private MainOptions options;

    public CompositeConnection(List<CypherConnection> connections, MainOptions options) {
        this.connections = connections;
        this.options = options;
    }

    public static final int times = 3;

    public Object lock;

    @Override
    public String getDatabaseVersion() {
        return "composite";
    }

    @Override
    public void close() throws Exception {
        for (CypherConnection connection : connections) {
            connection.close();
        }
    }

    @Override
    public void executeStatement(String arg) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(connections.size());
        List<Exception> exceptions = new ArrayList<>();
        for (int i = 0; i < connections.size(); i++) {
            CypherConnection connection = connections.get(i);
            int present = i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    List<projectResultSet> result = null;
                    try {
                        connection.executeStatement(arg);
//                        throw new RuntimeException();
                    } catch (Exception e) {
                        synchronized (exceptions) {
                            exceptions.add(new DatabaseCrashException(e, present));
                        }
                    }
                }
            });
        }

        executorService.shutdown();
        while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
            //do nothing
        }
        if (exceptions.size() != 0) {
            System.out.println("contains crashes");
            if (exceptions.stream().anyMatch(e -> e.getCause() instanceof MustRestartDatabaseException)) {
                System.out.println("must restart");
                throw new MustRestartDatabaseException(exceptions.stream().filter(e -> e.getCause() instanceof MustRestartDatabaseException).findFirst().get());
            }
            System.out.println("not must restart");
            throw exceptions.get(0);
        }
    }

    @Override
    public List<projectResultSet> executeStatementAndGet(String arg) throws Exception {
        List<projectResultSet> results = new ArrayList<>();
        for (int i = 0; i < connections.size(); i++) {
            results.add(null);
        }
        ExecutorService executorService = Executors.newFixedThreadPool(connections.size());
        List<Exception> exceptions = new ArrayList<>();
        for (int i = 0; i < connections.size(); i++) {
            CypherConnection connection = connections.get(i);
            int present = i;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    List<projectResultSet> result = null;
                    try {
                        result = connection.executeStatementAndGet(arg);
//                        throw new RuntimeException();
                    } catch (Exception e) {
                        synchronized (exceptions) {
                            exceptions.add(new DatabaseCrashException(e, present));
                        }
                        result = null;
                    }
                    if (result == null || result.get(0) == null) {

                    } else {
                        synchronized (results) {
                            results.set(present, result.get(0));
                        }
                    }
                }
            });
        }

        executorService.shutdown();
        int totalSeconds = 0;
        while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
            //do nothing
            totalSeconds += 10;
            if (totalSeconds >= 120) {
                System.exit(-1);
            }
        }

        if (!options.forceCompareAndIgnoreException()) {
            if (exceptions.size() != 0) {
                System.out.println("contains crashes");
                if (exceptions.stream().anyMatch(e -> e.getCause() instanceof MustRestartDatabaseException)) {
                    System.out.println("must restart");
                    throw new MustRestartDatabaseException(exceptions.stream().filter(e -> e.getCause() instanceof MustRestartDatabaseException).findFirst().get());
                }
                System.out.println("not must restart");
                if (exceptions.size() == 1) throw exceptions.get(0);
                else {

                    StringBuilder sb = new StringBuilder();

                    // 遍历所有的异常
                    for (Exception ex : exceptions) {
                        // 检查异常的 cause 中是否包含特定的错误信息
                        if (ex.getCause() != null && ex.getCause().getMessage().contains("key not found: VariableSlotKey") && !ex.getMessage().contains("database 0")) {
                            // 如果是正常的异常，跳过此异常
                            continue;
                        }

                        if (ex.getCause() != null && ex.getCause().getMessage().contains("Failed to create") && ex.getCause().getMessage().contains("is missing")) {
                            // 如果是正常的异常，跳过此异常
                            continue;
                        }

                        // 拼接非正常异常的信息
                        sb.append(ex.getMessage());
                        sb.append("\n");
                    }

                    // 如果有其他异常，则抛出 MultipleExceptionsOccurredException
                    if (sb.length() > 0) {
                        throw new MultipleExceptionsOccurredException(sb.toString(), exceptions);
                    }
                }
            }
                /*if (results.contains(null)) {
                    throw new Exception("a specific database failed"); // todo
                }*/
        }


        return results;
    }

    @Override
    public List<Long> executeStatementAndGetTime(String arg) throws Exception {
        List<Long> timeList1 = new ArrayList<>();
        List<Long> timeList2 = new ArrayList<>();
        final ExecutorService exec = Executors.newFixedThreadPool(1);

        for (int i = 0; i < times; i++) {
            Long startTime1 = System.currentTimeMillis();
            Callable<Long> call = () -> {
                connections.get(0).executeStatement(arg);
                return System.currentTimeMillis();
            };
            Long endTime1;
            try {
                Future<Long> future = exec.submit(call);
                endTime1 = future.get(TIMEOUT * 3 / 2, TimeUnit.MILLISECONDS) - startTime1;
            } catch (TimeoutException ex) {
                endTime1 = TIMEOUT + 1L;
                System.out.println(ex);
            } catch (Exception e) {
                if (e.getMessage().contains("JedisDataException: Query timed out") || e.getMessage().contains("ClientException: The transaction has been terminated") || e.getMessage().contains("TransientException: Transaction was asked to abort")) {
                    endTime1 = TIMEOUT + 1L;
                    System.out.println(e);
                } else {
                    endTime1 = -1L;
                    System.out.println("Error!");
                    e.printStackTrace();
                }
            }

            Long startTime2 = System.currentTimeMillis();
            call = () -> {
                connections.get(1).executeStatement(arg);
                return System.currentTimeMillis();
            };
            Long endTime2;
            try {
                Future<Long> future = exec.submit(call);
                endTime2 = future.get(TIMEOUT * 3 / 2, TimeUnit.MILLISECONDS) - startTime2;
            } catch (TimeoutException ex) {
                endTime2 = TIMEOUT + 1L;
                System.out.println(ex);
            } catch (Exception e) {
                if (e.getMessage().contains("JedisDataException: Query timed out") || e.getMessage().contains("ClientException: The transaction has been terminated") || e.getMessage().contains("TransientException: Transaction was asked to abort")) {
                    endTime2 = TIMEOUT + 1L;
                    System.out.println(e);
                } else {
                    endTime2 = -1L;
                    System.out.println("Error!");
                    e.printStackTrace();
                }
            }

            timeList1.add(endTime1);
            timeList2.add(endTime2);
            System.out.println(connections.get(0).getDatabaseVersion() + ": " + endTime1 + "; " + connections.get(1).getDatabaseVersion() + ": " + endTime2);
        }
        exec.shutdown();
        Collections.sort(timeList1);
        Collections.sort(timeList2);
        return Arrays.asList(timeList1.get(1), timeList2.get(1));
    }

    @Override
    public List<Long> executeFirstQueryAndGetTimes(String query) throws Exception {//parallel
        if (connections.isEmpty()) {
            throw new IllegalStateException("No available connections for execution.");
        }

        final ExecutorService exec = Executors.newFixedThreadPool(connections.size());
        List<Future<Long>> futureResults = new ArrayList<>();
        List<Long> executionTimes = new ArrayList<>();

        try {
            // 并发执行所有 connections 的第一次查询
            for (CypherConnection connection : connections) {
                Callable<Long> task = () -> measureQueryTime(connection, query);
                futureResults.add(exec.submit(task));
            }

            // 收集所有连接的执行时间，并设置超时控制
            for (Future<Long> future : futureResults) {
                try {
                    // 设置超时，300秒为示例超时时间
                    executionTimes.add(future.get(300, TimeUnit.SECONDS)); // 获取执行时间，超时则抛出 TimeoutException
                } catch (TimeoutException e) {
                    System.out.println("Query execution timed out, skipping...");
                    future.cancel(true); // 超时后取消当前任务
                    executionTimes.add((long) TIMEOUT + 1); // 设置超时返回值，超时返回 TIMEOUT + 1
                } catch (Exception e) {
                    System.out.println("Query execution failed: " + e.getMessage());
                    executionTimes.add(-1L); // 其他异常时返回 -1
                }
            }
        } finally {
            exec.shutdown();
        }

        return executionTimes; // 返回每个连接的执行时间列表
    }

    public List<Long> executeFirstQueryAndGetTimesSerial(String query) throws Exception {//serial
        if (connections.isEmpty()) {
            throw new IllegalStateException("No available connections for execution.");
        }

        List<Long> executionTimes = new ArrayList<>();

        // 串行执行所有 connections 的第一次查询
        for (CypherConnection connection : connections) {
            try {
                long executionTime = measureQueryTime(connection, query); // 获取执行时间
                executionTimes.add(executionTime);
            } catch (Exception e) {
                System.out.println("Query execution failed on one connection: " + e.getMessage());
                executionTimes.add(-1L); // 如果执行失败，则记录 -1
            }
        }

        return executionTimes; // 返回每个连接的执行时间列表
    }

    private long measureQueryTime(CypherConnection connection, String query) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            connection.executeStatement(query); // 执行查询
            return System.currentTimeMillis() - startTime; // 返回执行时间
        } catch (Exception e) {
            System.out.println("Execution failed on connection: " + e.getMessage());
            throw e; // 抛出异常以供上层捕获
        }
    }

    public List<Pair<projectResultSet, Long>> executeFirstQueryAndGetResultsWithTime(String query) throws Exception {
        if (connections.isEmpty()) {
            throw new IllegalStateException("No available connections for execution.");
        }

        final ExecutorService exec = Executors.newFixedThreadPool(connections.size());
        List<Future<Pair<projectResultSet, Long>>> futureResults = new ArrayList<>();
        List<Pair<projectResultSet, Long>> resultsWithTime = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();

        try {
            // 并发执行所有 connections 的第一次查询
            for (CypherConnection connection : connections) {
                Callable<Pair<projectResultSet, Long>> task = () -> measureQueryTimeWithResult(connection, query);
                futureResults.add(exec.submit(task));
            }

            // 收集所有连接的执行时间，并设置超时控制
            for (Future<Pair<projectResultSet, Long>> future : futureResults) {
                try {
                    // 设置超时，300秒为示例超时时间
                    Pair<projectResultSet, Long> resultPair = future.get(200, TimeUnit.SECONDS);
                    resultsWithTime.add(resultPair);
                } catch (TimeoutException e) {
                    System.out.println("Query execution timed out, skipping...");
                    future.cancel(true); // 超时后取消当前任务
                    resultsWithTime.add(Pair.of(null, TIMEOUT)); // 超时返回 null 和超时标志
                } catch (Exception e) {
                    System.out.println("Query execution failed: " + e.getMessage());
                    resultsWithTime.add(Pair.of(null, -1L)); // 其他异常时返回 null 和 -1
                    synchronized (exceptions) {
                        exceptions.add(new DatabaseCrashException(e, futureResults.indexOf(future)));
                    }
                }
            }
        } finally {
            exec.shutdown();
        }

        // 处理异常
        if (!exceptions.isEmpty()) {
            handleExceptions(exceptions);
        }

        return resultsWithTime; // 返回查询结果和执行时间的 Pair 列表
    }

    // 记录查询时间并返回结果，捕捉异常
    private Pair<projectResultSet, Long> measureQueryTimeWithResult(CypherConnection connection, String query) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            projectResultSet resultSet = connection.executeStatementAndGet(query).get(0); // 获取查询结果
            long executionTime = System.currentTimeMillis() - startTime; // 计算执行时间
            return Pair.of(resultSet, executionTime); // 返回查询结果和执行时间
        } catch (Exception e) {
            System.out.println("Execution failed on connection: " + e.getMessage());
            throw e; // 捕获异常并重新抛出
        }
    }

    // 处理异常
    private void handleExceptions(List<Exception> exceptions) throws Exception {
        if (!options.forceCompareAndIgnoreException()) {
            if (exceptions.size() != 0) {
                System.out.println("contains crashes");
                if (exceptions.stream().anyMatch(e -> e.getCause() instanceof MustRestartDatabaseException)) {
                    System.out.println("must restart");
                    throw new MustRestartDatabaseException(exceptions.stream().filter(e -> e.getCause() instanceof MustRestartDatabaseException).findFirst().get());
                }
                System.out.println("not must restart");
                // 过滤掉不需要的异常
                List<Exception> filteredExceptions = new ArrayList<>();
                StringBuilder sb = new StringBuilder();

                // 遍历所有的异常
                for (Exception ex : exceptions) {
                    if (ex.getCause() != null && ex.getCause().getMessage().contains("key not found: VariableSlotKey") && !ex.getMessage().contains("database 0")) {
                        continue; // 跳过特定异常
                    }

                    if (ex.getCause() != null && ex.getCause().getMessage().contains("Failed to create") && ex.getCause().getMessage().contains("is missing")) {
                        continue; // 跳过特定异常
                    }
                    if (ex.getCause() != null && ex.getCause().getMessage().contains("has been deleted in this transaction") ) {
                        continue; // 跳过特定异常
                    }
                    if (ex.getCause() != null && ex.getCause().getMessage().contains("The transaction has been terminated. Retry your operation in a new transaction, and you should see a successful result. The transaction has not completed within the timeout specified at its start by the client. You may want to retry with a longer timeout.") ) {
                        continue; // 跳过特定异常
                    }
                    if (ex.getCause() != null && ex.getCause().getMessage().contains("The shortest path algorithm does not work when the start and end nodes are the same.") ) {
                        continue; // 跳过特定异常
                    }

                    filteredExceptions.add(ex);
                    // 拼接非正常异常的信息
                    sb.append(ex.getMessage());
                    sb.append("\n");
                }

                // 如果有其他异常，则抛出 MultipleExceptionsOccurredException
                if (filteredExceptions.size() == 1) {
                    throw filteredExceptions.get(0);
                } else if (filteredExceptions.size() > 1) {
                    // 如果有多个异常，拼接它们的信息并抛出 MultipleExceptionsOccurredException

                    throw new MultipleExceptionsOccurredException(sb.toString(), filteredExceptions);
                }
            }
        }
    }
}


