package org.example.project.cypher;

import org.example.project.*;
//import org.example.project.cypher.algorithm.*;
import org.example.project.common.oracle.TestOracle;
import org.example.project.cypher.oracle.DifferentialNonEmptyBranchOracle;
import org.example.project.cypher.gen.GraphManager;

import org.example.project.DBMSSpecificOptions;
import org.example.project.MainOptions;
import org.example.project.OracleFactory;
import org.example.project.ProviderAdapter;
import org.example.project.cypher.gen.Generator;
import org.example.project.exceptions.DatabaseCrashException;
import org.example.project.exceptions.MultipleExceptionsOccurredException;
import org.example.project.exceptions.MustRestartDatabaseException;

import java.util.List;

public abstract class CypherProviderAdapter<O extends DBMSSpecificOptions> extends ProviderAdapter<O, CypherConnection> {

    public CypherProviderAdapter(Class<O> optionClass) {
        super(optionClass);
    }

    @Override
    public void generateAndTestDatabase(GlobalState globalState) throws Exception { //todo 主过程
        Generator generator = new Generator(globalState);

        List<CypherQueryAdapter> queries = generator.getGraphManager().generateCreateGraphQueries();

//        RandomGraphGenerator<G,S> graphGenerator = new RandomGraphGenerator<>(globalState);
//        queries = graphGenerator.createGraph(globalState);

        for (CypherQueryAdapter query : queries) {
            try {
                // 调用 CypherQueryAdapter 的 execute 方法执行查询
                boolean success = query.executeAndLog(globalState);
                if (success) {
                    globalState.getState().logCreateStatement(query); // 记录日志
                }
            } catch (Exception e) {
                System.err.println("Error executing query: " + query.getQueryString());
                e.printStackTrace();
                // 可根据需求决定是否继续执行其他查询或终止
            }
        }//debug
        try {
            TestOracle oracle = new DifferentialNonEmptyBranchOracle(globalState, generator);
            for (int i = 0; i < globalState.getOptions().getNrQueries(); i++) {
                try (StateToReproduce.OracleRunReproductionState localState = globalState.getState().createLocalState()) {
                    assert localState != null;
                    try {
                        oracle.check();
                        globalState.getManager().incrementSelectQueryCount();
                    } catch (IgnoreMeException e) {
                    } catch (MustRestartDatabaseException e) {
                        throw e;
                    } catch (DatabaseCrashException e) {
                        if (e.getCause() instanceof MustRestartDatabaseException) {
                            throw new MustRestartDatabaseException(e);
                        }
                       // e.printStackTrace();
//                        executor.getStateToReproduce().exception = reduce.getMessage();
//                        globalState.getLogger().logFileWriter = null;
                        globalState.getLogger().logException(e, globalState.getState());

                    } catch (MultipleExceptionsOccurredException e) {
                        // 捕获 MultipleExceptionsOccurredException 并打印异常信息
                        System.err.println("Multiple exceptions occurred during query execution:");
                        globalState.getLogger().logExceptions(e, globalState.getState());

                    } catch (Exception e) {
                        //e.printStackTrace();
//                        executor.getStateToReproduce().exception = reduce.getMessage();
//                        globalState.getLogger().logFileWriter = null;
                        globalState.getLogger().logException(e, globalState.getState());

                    }
                    assert localState != null;
                    localState.executedWithoutError();
                }
                //if(i==globalState.getOptions().getNrQueries()) throw new RuntimeException("total number reached");
            }
            //throw new RuntimeException("total number reached");
//

        } finally {
            globalState.getConnection().close();
            System.gc();
        }
        //List<CypherQueryAdapter> Queries=generator.generateQuery();

    }

    public void InitialDatabase(GlobalState globalState) throws Exception {

    }


    public abstract CypherConnection createDatabaseWithOptions(MainOptions mainOptions, O specificOptions) throws Exception;

}
