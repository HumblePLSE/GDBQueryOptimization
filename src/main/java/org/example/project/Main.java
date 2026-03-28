package org.example.project;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.JCommander.Builder;

import org.example.project.common.log.Loggable;
import org.example.project.common.query.projectResultSet;
import org.example.project.common.query.Query;
import org.example.project.composite.CompositeProvider;
import org.example.project.cypher.CypherExecutor;
import org.example.project.exceptions.MultipleExceptionsOccurredException;
import org.example.project.memGraph.MemGraphProvider;
import org.example.project.neo4j.Neo4jProvider;
import org.example.project.redisGraph.RedisGraphProvider;


public final class Main {

    public static final File LOG_DIRECTORY = new File("logs");
    public static volatile AtomicLong nrQueries = new AtomicLong();
    public static volatile AtomicLong nrDatabases = new AtomicLong();
    public static volatile AtomicLong nrSuccessfulActions = new AtomicLong();
    public static volatile AtomicLong nrUnsuccessfulActions = new AtomicLong();
    static int threadsShutdown;
    static boolean progressMonitorStarted;

    static {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "ERROR");
        if (!LOG_DIRECTORY.exists()) {
            LOG_DIRECTORY.mkdir();
        }
    }

    private Main() {
    }

    public static final class StateLogger {

        private File loggerFile;

        private int loggerNum = 0;
        private File curFile;
        private FileWriter logFileWriter;
        public FileWriter currentFileWriter;
        private static final List<String> INITIALIZED_PROVIDER_NAMES = new ArrayList<>();
        private final boolean logEachSelect;
        private final DatabaseProvider<?, ?> databaseProvider;

        private File dir;

        private String databaseName;

        private static final class AlsoWriteToConsoleFileWriter extends FileWriter {

            AlsoWriteToConsoleFileWriter(File file) throws IOException {
                super(file);
            }

            @Override
            public Writer append(CharSequence arg0) throws IOException {
                System.err.println(arg0);
                return super.append(arg0);
            }

            @Override
            public void write(String str) throws IOException {
                System.err.println(str);
                super.write(str);
            }
        }

        public StateLogger(String databaseName, DatabaseProvider<?, ?> provider, MainOptions options) {
            this.databaseName = databaseName;
            dir = new File(LOG_DIRECTORY, provider.getDBMSName());
            if (dir.exists() && !dir.isDirectory()) {
                throw new AssertionError(dir);
            }
            ensureExistsAndIsEmpty(dir, provider);
            loggerFile = new File(dir, databaseName + "-" + loggerNum + ".log");
            loggerNum++;
            logEachSelect = options.logEachSelect();
            if (logEachSelect) {
                curFile = new File(dir, databaseName + "-allLogs.log");
            }
            this.databaseProvider = provider;
        }

        private void ensureExistsAndIsEmpty(File dir, DatabaseProvider<?, ?> provider) {
            if (INITIALIZED_PROVIDER_NAMES.contains(provider.getDBMSName())) {
                return;
            }
            synchronized (INITIALIZED_PROVIDER_NAMES) {
                if (!dir.exists()) {
                    try {
                        Files.createDirectories(dir.toPath());
                    } catch (IOException e) {
                        throw new AssertionError(e);
                    }
                }
                File[] listFiles = dir.listFiles();
                assert listFiles != null : "directory was just created, so it should exist";
                for (File file : listFiles) {
                    if (!file.isDirectory()) {
                        file.delete();
                    }
                }
                INITIALIZED_PROVIDER_NAMES.add(provider.getDBMSName());
            }
        }

        private FileWriter getLogFileWriter() {
            if (logFileWriter == null) {
                try {
                    logFileWriter = new AlsoWriteToConsoleFileWriter(loggerFile);
                } catch (IOException e) {
                    throw new AssertionError(e);
                }
            }
            return logFileWriter;
        }

        public FileWriter getCurrentFileWriter() {
            if (!logEachSelect) {
                throw new UnsupportedOperationException();
            }
            if (currentFileWriter == null) {
                try {
                    currentFileWriter = new FileWriter(curFile, false);
                } catch (IOException e) {
                    throw new AssertionError(e);
                }
            }
            return currentFileWriter;
        }

        public void writeCurrent(StateToReproduce state) {
            if (!logEachSelect) {
                throw new UnsupportedOperationException();
            }
            printState(getCurrentFileWriter(), state);
            try {
                currentFileWriter.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void writeCurrent(String input) {
            write(databaseProvider.getLoggableFactory().createLoggable(input));
        }

        public void writeCurrentNoLineBreak(String input) {
            write(databaseProvider.getLoggableFactory().createLoggableWithNoLinebreak(input));
        }

        private void write(Loggable loggable) {
            if (!logEachSelect) {
                throw new UnsupportedOperationException();
            }
            try {
                getCurrentFileWriter().write(loggable.getLogString());

                currentFileWriter.flush();
            } catch (IOException e) {
                throw new AssertionError();
            }
        }

        public void logException(Throwable reduce, StateToReproduce state) {
            Loggable stackTrace = getStackTrace(reduce);
            FileWriter logFileWriter2 = getLogFileWriter();
            try {
                logFileWriter2.write(stackTrace.getLogString());
                printState(logFileWriter2, state);
            } catch (IOException e) {
                throw new AssertionError(e);
            } finally {
                try {
                    logFileWriter2.flush();
                    logFileWriter2.close();
                    logFileWriter = null;
                    loggerFile = new File(dir, databaseName + "-" + loggerNum + ".log");
                    loggerNum++;

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        public void logExceptions(Throwable reduce, StateToReproduce state) {
            Loggable stackTrace = getStackTrace(reduce);
            FileWriter logFileWriter2 = getLogFileWriter();
            try {
                // 如果是 MultipleExceptionsOccurredException, 需要遍历其内部的 exceptions 并记录
                if (reduce instanceof MultipleExceptionsOccurredException) {
                    MultipleExceptionsOccurredException multiEx = (MultipleExceptionsOccurredException) reduce;
                    // 遍历 MultipleExceptionsOccurredException 中的每一个异常，输出其堆栈信息
                    for (Exception ex : multiEx.getExceptions()) {
                        Loggable exStackTrace = getStackTrace(ex);
                        logFileWriter2.write(exStackTrace.getLogString());
                        logFileWriter2.write("\n"); // 每个异常之间添加换行符
                    }
                } else {
                    // 否则，直接输出原始异常的堆栈信息
                    logFileWriter2.write(stackTrace.getLogString());
                }

                // 输出 StateToReproduce 信息
                printState(logFileWriter2, state);

            } catch (IOException e) {
                throw new AssertionError(e);
            } finally {
                try {
                    logFileWriter2.flush();
                    logFileWriter2.close();
                    logFileWriter = null;
                    loggerFile = new File(dir, databaseName + "-" + loggerNum + ".log");
                    loggerNum++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void logExceptionPartial(Throwable reduce, StateToReproduce state, Query<?> query) {
            Loggable stackTrace = getStackTrace(reduce);
            FileWriter logFileWriter2 = getLogFileWriter();
            try {
                logFileWriter2.write(stackTrace.getLogString());
                printCreateState(logFileWriter2, state);

                StringBuilder sb = new StringBuilder();

                sb.append("\n\n");

                sb.append(query.getLogString());
                logFileWriter2.write(sb.toString());
            } catch (IOException e) {
                throw new AssertionError(e);
            } finally {
                try {
                    logFileWriter2.flush();
                    logFileWriter2.close();
                    logFileWriter = null;
                    loggerFile = new File(dir, databaseName + "-" + loggerNum + ".log");
                    loggerNum++;

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        private Loggable getStackTrace(Throwable e1) {
            return databaseProvider.getLoggableFactory().convertStacktraceToLoggable(e1);
        }

        private void printState(FileWriter writer, StateToReproduce state) {
            StringBuilder sb = new StringBuilder();

            sb.append(databaseProvider.getLoggableFactory()
                    .getInfo(state.getDatabaseName(), state.getDatabaseVersion(), state.getSeedValue()).getLogString());

            for (Query<?> s : state.getStatements()) {
                sb.append(s.getLogString());
                sb.append('\n');
            }
            try {
                writer.write(sb.toString());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }

        private void printCreateState(FileWriter writer, StateToReproduce state) {
            StringBuilder sb = new StringBuilder();

            sb.append(databaseProvider.getLoggableFactory()
                    .getInfo(state.getDatabaseName(), state.getDatabaseVersion(), state.getSeedValue()).getLogString());

            for (Query<?> s : state.getCreateStatements()) {
                sb.append(s.getLogString());
                sb.append('\n');
            }
            try {
                writer.write(sb.toString());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }

    }

    public static class QueryManager<C extends projectDBConnection> {

        private final GlobalState<?, C> globalState;

        QueryManager(GlobalState<?, C> globalState) {
            this.globalState = globalState;
        }

        public boolean execute(Query<C> q, String... fills) throws Exception {
            globalState.getState().logStatement(q);
            boolean success;
            success = q.execute(globalState, fills);
            Main.nrSuccessfulActions.addAndGet(1);
            return success;
        }

        public List<projectResultSet> executeAndGet(Query<C> q, String... fills) throws Exception {
            globalState.getState().logStatement(q);
            List<projectResultSet> result;
            result = q.executeAndGet(globalState, fills);
            Main.nrSuccessfulActions.addAndGet(1);
            return result;
        }

        public void incrementSelectQueryCount() {
            Main.nrQueries.addAndGet(1);
        }

        public void incrementCreateDatabase() {
            Main.nrDatabases.addAndGet(1);
        }

        public List<Long> executeAndGetTime(Query<C> q, String... fills) throws Exception {
            globalState.getState().logStatement(q);
            List<Long> result;
            result = q.executeAndGetTime(globalState, fills);
            Main.nrSuccessfulActions.addAndGet(1);
            return result;
        }
    }

    public static void main(String[] args) throws IOException {
        System.exit(executeMain(args));
    }

    public static class DBMSExecutor<O extends DBMSSpecificOptions, C extends projectDBConnection> {

        private final DatabaseProvider<O, C> provider;
        private final MainOptions options;
        private final O command;
        private final String databaseName;
        private StateLogger logger;
        private StateToReproduce stateToRepro;
        private final Randomly r;

        public DBMSExecutor(DatabaseProvider<O, C> provider, MainOptions options, O dbmsSpecificOptions,
                            String databaseName, Randomly r) {
            this.provider = provider;
            this.options = options;
            this.databaseName = databaseName;
            this.command = dbmsSpecificOptions;
            this.r = r;
        }

        public O getCommand() {
            return command;
        }

        public void testConnection() throws Exception {
            GlobalState state = getInitializedGlobalState(options.getRandomSeed());
            try (projectDBConnection con = provider.createDatabase(state)) {
                return;
            }
        }

        public void run() throws Exception {
            GlobalState state = new GlobalState();
            stateToRepro = provider.getStateToReproduce(databaseName);
            stateToRepro.seedValue = r.getSeed();
            state.setState(stateToRepro);
            logger = new StateLogger(databaseName, provider, options);
            state.setRandomly(r);
            state.setDatabaseName(databaseName);
            state.setMainOptions(options);
            state.setDbmsSpecificOptions(command);
            System.gc();
            try (C con = provider.createDatabase(state)) { //创建数据库
                QueryManager<C> manager = new QueryManager<>(state); //todo QueryManager
                try {
                    stateToRepro.databaseVersion = con.getDatabaseVersion();
                } catch (Exception e) {
                    // ignore
                }
                state.setConnection(con);//把连接存在了globalState中
                state.setStateLogger(logger);
                state.setManager(manager);
                if (options.logEachSelect()) {
                    logger.writeCurrent(state.getState());
                }

                CypherExecutor executor = new CypherExecutor();
                //executor.executeQueriesFromFile(filePath,state);//debug


                provider.generateAndTestDatabase(state); //todo 主逻辑？
                try {
                    logger.getCurrentFileWriter().close();
                    logger.currentFileWriter = null;
                } catch (IOException e) {
                    throw new AssertionError(e);
                }
            }

        }

        private GlobalState getInitializedGlobalState(long seed) {
            GlobalState state = new GlobalState();
            stateToRepro = provider.getStateToReproduce(databaseName);
            stateToRepro.seedValue = seed;
            state.setState(stateToRepro);
            logger = new StateLogger(databaseName, provider, options);
            Randomly r = new Randomly(seed);
            state.setRandomly(r);
            state.setDatabaseName(databaseName);
            state.setMainOptions(options);
            state.setDbmsSpecificOptions(command);
            return state;
        }

        public StateLogger getLogger() {
            return logger;
        }

        public StateToReproduce getStateToReproduce() {
            return stateToRepro;
        }
    }

    public static class DBMSExecutorFactory<O extends DBMSSpecificOptions, C extends projectDBConnection> {

        private final DatabaseProvider<O, C> provider;
        private final MainOptions options;
        private final O command;

        public DBMSExecutorFactory(DatabaseProvider<O, C> provider, MainOptions options) {
            this.provider = provider;
            this.options = options;
            this.command = createCommand();
        }

        private O createCommand() {
            try {
                return provider.getOptionClass().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }

        public O getCommand() {
            return command;
        }

        @SuppressWarnings("unchecked")
        public DBMSExecutor<O, C> getDBMSExecutor(String databaseName, Randomly r) {
            try {
                return new DBMSExecutor<O, C>(provider.getClass().getDeclaredConstructor().newInstance(), options,
                        command, databaseName, r);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }

        public DatabaseProvider<O, C> getProvider() {
            return provider;
        }

    }

    public static int executeMain(String... args) throws AssertionError {
        List<DatabaseProvider<?, ?>> providers = getDBMSProviders(); // 不同的数据库的provider
         Map<String, DBMSExecutorFactory<?, ?>> nameToProvider = new HashMap<>();
        MainOptions options = new MainOptions();
        Builder commandBuilder = JCommander.newBuilder().addObject(options);
        for (DatabaseProvider<?, ?> provider : providers) {
            String name = provider.getDBMSName();
              DBMSExecutorFactory<?, ?> executorFactory = new DBMSExecutorFactory<>(provider, options);
            commandBuilder = commandBuilder.addCommand(name, executorFactory.getCommand());
            nameToProvider.put(name, executorFactory);
         }
         JCommander jc = commandBuilder.programName("SQLancer").build();
        jc.parse(args);


        if (options.isHelp()) {
            jc.usage();
            return options.getErrorExitCode();
        }

        // 如果没有指定命令，默认使用 composite
        String parsedCommand = jc.getParsedCommand();
        if (parsedCommand == null) {
            parsedCommand = "composite";
        }

        Randomly.initialize(options);
        // 在 testConnection 之前，先从 config.json 加载所有版本号
        loadVersionsFromConfig(options);
        startContainers(options.getAvailableVersions(), options.getTargetDb());

        DBMSExecutorFactory<?, ?> executorFactory = nameToProvider.get(parsedCommand);
        if (options.performConnectionTest()) {
            try {
                executorFactory.getDBMSExecutor(options.getDatabasePrefix() + "connectiontest", new Randomly())
                         .testConnection();
            } catch (Exception e) {
                System.err.println(
                        "SQLancer failed creating a test database, indicating that SQLancer might have failed connecting to the DBMS. In order to change the username, password, host and port, you can use the --username, --password, --host and --port options.\n\n");
                e.printStackTrace();
                return options.getErrorExitCode();
            }
        }

        System.out.println(options.getNrQueries());
        for (int i = 0; i < options.getTotalNumberTries(); i++) {
            final String databaseName = options.getDatabasePrefix() + "test-" + i;
            final long seed;
            if (options.getRandomSeed() == -1) {
                seed = System.currentTimeMillis() + i;
            } else {
                seed = options.getRandomSeed() + i;
            }
            Randomly r = new Randomly(seed);
            DBMSExecutor<?, ?> executor = executorFactory.getDBMSExecutor(databaseName, r); //todo 似乎是这个
            try {
                executor.run();//todo
                //return true;
            } catch (IgnoreMeException e) {
                //return true;
            } catch (Throwable reduce) {
                reduce.printStackTrace();
                executor.getStateToReproduce().exception = reduce.getMessage();
                executor.getLogger().logFileWriter = null;
                executor.getLogger().logException(reduce, executor.getStateToReproduce());
                //return false;
            } finally {

                try {
                    if (options.logEachSelect()) {
                        if (executor.getLogger().currentFileWriter != null) {
                            executor.getLogger().currentFileWriter.close();
                        }
                        executor.getLogger().currentFileWriter = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            stopContainers(options.getAvailableVersions(), options.getTargetDb());
            startContainers(options.getAvailableVersions(), options.getTargetDb());




                /*try {
                    // 执行 stop.bat
                    Process process1 = Runtime.getRuntime().exec("cmd /c D:\\IdeaProject\\memgraph\\project\\stop_delete.bat");//D:\IdeaProject\memgraph\project
                    process1.waitFor();  // 等待脚本执行完成
                    // 执行 run.bat
                    Process process2 = Runtime.getRuntime().exec("cmd /c D:\\IdeaProject\\memgraph\\project\\run_memgraph.bat");
                    process2.waitFor();  // 等待脚本执行完成
                    Thread.sleep(10000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }*/
            }


        /*try {
            if (options.getTimeoutSeconds() == -1) {
                execService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } else {
                execService.awaitTermination(options.getTimeoutSeconds(), TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/


        System.out.println("executeMain");
        return 0;
    }

    public static void stopContainers(List<String> versions, String dbName) {
        System.out.println("Stopping and removing " + dbName + " containers...");

        for (String version : versions) {
            String containerName = dbName + "-" + version;

            try {
                // 1. 停止容器
                System.out.println("Stopping " + dbName + " container: " + containerName);
                int stopExitCode = runCommand("docker", "stop", containerName);

                if (stopExitCode != 0) {
                    System.out.println("Warning: Failed to stop container " + containerName +
                            " (exit code: " + stopExitCode + ")");
                }

                // 2. 删除容器
                System.out.println("Removing " + dbName + " container: " + containerName);
                int removeExitCode = runCommand("docker", "rm", "-v", containerName);

                if (removeExitCode != 0) {
                    System.out.println("Warning: Failed to remove container " + containerName +
                            " (exit code: " + removeExitCode + ")");
                }

            } catch (IOException | InterruptedException e) {
                System.err.println("Error stopping/removing container " + containerName + ": " + e.getMessage());
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return;
            }
        }

        try {
            System.out.println("Waiting 5 seconds...");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        System.out.println("All specified " + dbName + " containers have been processed.");
    }

    public static void startContainers(List<String> versions, String dbName) {
        int baseBoltPort = 7687;
        int baseHttpPort;

        if ("memgraph".equalsIgnoreCase(dbName)) {
            baseHttpPort = 7444;
        } else if ("neo4j".equalsIgnoreCase(dbName)) {
            baseHttpPort = 7474;
        } else {
            throw new IllegalArgumentException("Unknown database name: " + dbName +
                    ". Must be 'memgraph' or 'neo4j'.");
        }

        System.out.println("Starting " + versions.size() + " " + dbName + " containers...");

        for (int i = 0; i < versions.size(); i++) {
            String version = versions.get(i);
            int boltPort = baseBoltPort + i;
            int httpPort = baseHttpPort + i;

            System.out.println("Starting " + dbName + " container version " + version +
                    " (ports: " + boltPort + ", " + httpPort + ")...");

            try {
                String containerName = dbName + "-" + version;
                int exitCode;

                if ("memgraph".equalsIgnoreCase(dbName)) {
                    System.out.println("Starting Memgraph container: " + containerName);
                    exitCode = runCommand(
                            "docker", "run", "-d",
                            "-p", boltPort + ":7687",
                            "-p", httpPort + ":7444",
                            "--name", containerName,
                            "memgraph/memgraph:" + version,
                            "--query-execution-timeout-sec=20"
                    );

                    if (exitCode != 0) {
                        System.err.println("Failed to start Memgraph container: " + containerName +
                                ", exit code = " + exitCode);
                        continue;
                    }

                } else if ("neo4j".equalsIgnoreCase(dbName)) {
                    System.out.println("Starting Neo4j container: " + containerName);
                    exitCode = runCommand(
                            "docker", "run", "-d",
                            "--name", containerName,
                            "-e", "NEO4J_AUTH=neo4j/password",
                            "-e", "NEO4J_dbms_memory_transaction_total_max=8GiB",
                            "-e", "NEO4J_dbms_transaction_timeout=180s",
                            "-p", httpPort + ":7474",
                            "-p", boltPort + ":7687",
                            "neo4j:" + version
                    );

                    if (exitCode != 0) {
                        System.err.println("Failed to start Neo4j container: " + containerName +
                                ", exit code = " + exitCode);
                        continue;
                    }

                } else {
                    throw new IllegalArgumentException("Unknown database name: " + dbName +
                            ". Must be 'memgraph' or 'neo4j'.");
                }

                System.out.println("Waiting for " + dbName + " container (version " + version + ") to start...");
                Thread.sleep(5000);

                System.out.println(dbName + " container (version " + version + ") started successfully!");

            } catch (IOException | InterruptedException e) {
                System.err.println("Failed to start " + dbName + " container (version " + version + "): " + e.getMessage());
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("All " + versions.size() + " " + dbName + " containers processed!");
    }

    static List<DatabaseProvider<?, ?>> providers = new ArrayList<>();

    public static List<DatabaseProvider<?, ?>> getDBMSProviders() {
        if (providers.size() == 0) {
            providers.add(new Neo4jProvider());
            providers.add(new CompositeProvider());
            providers.add(new RedisGraphProvider());
            providers.add(new MemGraphProvider());


        }
        return providers;
    }

    private static int runCommand(String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true); // 合并 stdout 和 stderr
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[docker] " + line);
            }
        }

        return process.waitFor();
    }

    /**
     * 从 config.json 文件中加载所有数据库版本号并存入 MainOptions
     */
    private static void loadVersionsFromConfig(MainOptions options) {
        try {
            // 查找 CompositeOptions 中的 config 路径
            String configPath = "./config.json";

            // 尝试从命令行参数或默认位置获取 config 文件
            File configFile = new File(configPath);
            if (!configFile.exists()) {
                System.err.println("Warning: config.json not found at " + configPath);
                options.setAvailableVersions(new ArrayList<>());
                return;
            }

            // 使用 Gson 解析 JSON
            com.google.gson.Gson gson = new com.google.gson.Gson();
            FileReader fileReader = new FileReader(configFile);
            com.google.gson.JsonObject jsonObject = gson.fromJson(fileReader, com.google.gson.JsonObject.class);
            fileReader.close();

            // 提取所有版本号
            List<String> versions = new ArrayList<>();
            for (String key : jsonObject.keySet()) {
                // key 格式为 "database@version"，例如 "memgraph@3.8.1"
                if (key.contains("@")) {
                    String[] parts = key.split("@");
                    if (parts.length == 2) {
                        String version = parts[1];
                        versions.add(version);

                        // 也可以从对象内部验证 version 字段
                        com.google.gson.JsonObject dbConfig = jsonObject.getAsJsonObject(key);
                        if (dbConfig.has("version")) {
                            String internalVersion = dbConfig.get("version").getAsString();
                            if (!internalVersion.equals(version)) {
                                System.out.println("Warning: Version mismatch for " + key +
                                        " - Key: " + version + ", Internal: " + internalVersion);
                            }
                        }
                    }
                }
            }

            options.setAvailableVersions(versions);
          //  System.out.println("Loaded " + versions.size() + " versions from config: " + versions);

        } catch (Exception e) {
            System.err.println("Error loading versions from config.json: " + e.getMessage());
            e.printStackTrace();
            options.setAvailableVersions(new ArrayList<>());
        }
    }
}