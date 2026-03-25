package org.example.project.cypher.oracle;

import org.apache.commons.lang3.tuple.Pair;
import org.example.project.GlobalState;
import org.example.project.common.query.projectResultSet;
import org.example.project.common.oracle.TestOracle;
//import org.example.project.cypher.CypherGlobalState;
import org.example.project.cypher.CypherConnection;
import org.example.project.cypher.CypherQueryAdapter;
//import org.example.project.cypher.ast.IClauseSequence;
//import org.example.project.cypher.dsl.IQueryGenerator;
import org.example.project.cypher.gen.Generator;
import org.example.project.cypher.schema.CypherSchema;
import org.example.project.exceptions.MultipleExceptionsOccurredException;
import org.example.project.exceptions.ResultMismatchException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletionException;

public class DifferentialNonEmptyBranchOracle<G extends GlobalState<?, CypherConnection>> implements TestOracle {

    private final G globalState;
    private Generator queryGenerator;

    public static final int BRANCH_PAIR_SIZE = 65536;
    public static final int BRANCH_SIZE = 1000000;

    public static final int PORT = 9009;
    public static final byte CLEAR = 1, PRINT_MEM = 2;


    public DifferentialNonEmptyBranchOracle(G globalState, Generator generator) {
        this.globalState = globalStatepackage org.example.project.cypher.oracle;

import org.apache.commons.lang3.tuple.Pair;
import org.example.project.GlobalState;
import org.example.project.common.query.projectResultSet;
import org.example.project.common.oracle.TestOracle;
//import org.example.project.cypher.CypherGlobalState;
import org.example.project.cypher.CypherConnection;
import org.example.project.cypher.CypherQueryAdapter;
//import org.example.project.cypher.ast.IClauseSequence;
//import org.example.project.cypher.dsl.IQueryGenerator;
import org.example.project.cypher.gen.Generator;
import org.example.project.cypher.schema.CypherSchema;
import org.example.project.exceptions.MultipleExceptionsOccurredException;
import org.example.project.exceptions.ResultMismatchException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletionException;

public class DifferentialNonEmptyBranchOracle<G extends GlobalState<?, CypherConnection>> implements TestOracle {

    private final G globalState;
    private Generator queryGenerator;

    public static final int BRANCH_PAIR_SIZE = 65536;
    public static final int BRANCH_SIZE = 1000000;

    public static final int PORT = 9009;
    public static final byte CLEAR = 1, PRINT_MEM = 2;


    public DifferentialNonEmptyBranchOracle(G globalState, Generator generator) {
        this.globalState = globalState;
        //todo 整个oracle的check会被执行多次，一直是同一个oracle实例，因此oracle本身可以管理种子库
        this.queryGenerator = generator;
    }

    public void check() throws Exception {
        //todo oracle 的检测逻辑，会被调用多次
        CypherQueryAdapter sequence = queryGenerator.generateQuery();
        //StringBuilder sb = new StringBuilder();

        String queryString = sequence.getQueryString();
        System.out.println(queryString);
        //String queryString1 = "MATCH p0 = (:l6) WHERE ((true AND false) XOR true) WITH count(p0) AS alias0, p0 AS alias1 SKIP 0 WITH alias0, count(alias1) AS alias2, max(alias1) AS alias3, alias1 AS alias4 OPTIONAL MATCH (:l10:l4), (n0:l6:l2) -[r0:rt0]-> (:l1:l11:l5 {k1: true}) RETURN r0, alias4 SKIP 0\n";
        //CypherQueryAdapter sequence1 = new CypherQueryAdapter(queryString1);//debug
        //List<projectResultSet> results;
        //List<Long> resultstime;
        List<Pair<projectResultSet,Long>> results;
        int resultLength = -1;

        byte[] branchCoverage = new byte[BRANCH_SIZE];
        byte[] branchPairCoverage = new byte[BRANCH_PAIR_SIZE];

        try {

            results = sequence.executeFirstQueryAndGetResultsWithTime(globalState);


            long baseTime = results.get(0).getRight(); // 获取第一个查询的执行时间

            for (int i = 1; i < results.size(); i++) {
                Long currentTime = results.get(i).getRight();
                if (currentTime == null || currentTime == -1) {
                    continue; // 跳过超时或异常的查询
                }

                if (baseTime >= currentTime * 5) {  // 检查是否超出 5 倍
                    throw new RuntimeException(String.format(
                            "Execution time mismatch! First query took %dms, while query %d took only %dms.",
                            baseTime, i, currentTime));
                }
            }

            // 检查结果集行数
            projectResultSet firstResultSet = results.get(0).getLeft();

            if (firstResultSet != null) {
                resultLength = firstResultSet.getRowNum();
            }

            for (int i = 1; i < results.size(); i++) {
                projectResultSet currentResultSet = results.get(i).getLeft();
                if (currentResultSet == null) {
                    continue; // 跳过空结果集
                }

                int currentRowNum = currentResultSet.getRowNum();
                if (resultLength != currentRowNum) {  // 检查行数是否一致
                    throw new RuntimeException(String.format(
                            "Row number mismatch! First query returned %d rows, while query %d returned %d rows.",
                            resultLength, i, currentRowNum));
                }
            }

        } catch (MultipleExceptionsOccurredException e) {
            // 捕获 MultipleExceptionsOccurredException 并打印异常信息
            System.err.println("Multiple exceptions occurred during query execution:");
            for (Exception ex : e.getExceptions()) {
                System.err.println(ex.getMessage());
                ex.printStackTrace();  // 打印每个异常的堆栈跟踪
            }
            throw e;  // 如果需要，可以重新抛出异常

        }catch (CompletionException e) {
            System.out.println("Query execution failed!");
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during query comparison.");
            e.printStackTrace();
            throw e;

            /*boolean found = false;
            StringBuilder msgSb = new StringBuilder();
            for(int i = 1; i < results.size(); i++) {
                if (!results.get(i).compareWithOutOrder(results.get(i - 1))) {
                    if(!found){
                        msgSb.append("The contents of the result sets mismatch!\n");
                        found = true;
                    }
                    String msg = "";
                    msg = msg + "Difference between " + (i-1) + " and " + i;
                    msg = msg + "First: " + results.get(i - 1).getRowNum() + " --- " + results.get(i - 1).resultToStringList() + "\n";
                    msg = msg + "Second: " + results.get(i).getRowNum() + " --- " + results.get(i).resultToStringList() + "\n";
                    msgSb.append(msg);
                }
            }
            if(found){
                throw new ResultMismatchException(msgSb.toString());
            }*/
            //resultLength = results.get(0).getRowNum();
        }
        /*catch (CompletionException e) {
            System.out.println("该Cypher查询不支持转换为Gremlin！");
            System.out.println(e.getMessage());
        }*/
        boolean isBugDetected = false;
        //todo 上层通过抛出的异常检测是否通过，所以这里可以捕获并检测异常的类型，可以计算一些统计数据，然后重抛异常


        queryGenerator.addNewRecord(sequence, isBugDetected, resultLength, branchCoverage, branchPairCoverage);
    }
}
