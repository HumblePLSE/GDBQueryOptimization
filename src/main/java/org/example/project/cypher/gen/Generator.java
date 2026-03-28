package org.example.project.cypher.gen;

import org.example.project.GlobalState;
import org.example.project.Randomly;
//import org.example.project.cypher.CypherQueryAdapter;
//import org.example.project.cypher.dsl.IGraphGenerator;
import org.example.project.cypher.CypherQueryAdapter;
import org.example.project.cypher.ast.IClause;
import org.example.project.cypher.gen.GraphManager;
import org.example.project.cypher.schema.CypherSchema;
import org.example.project.cypher.standard_ast.CypherType;
//import org.example.project.cypher.standard_ast.expr.ConstExpression;
/*import org.example.project.cypher.standard_ast.RootClause;
import org.example.project.cypher.standard_ast_mem.RootClause;*/
import org.example.project.cypher.standard_ast.Clause;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Generator {

    private GlobalState globalState;

    private GraphManager graphManager;

    public static File coverageFile;

    public static FileOutputStream outputStream;

    public static int coverageNum = 0;
    public static int nonEmptyCoverageNum = 0;

    static {
        coverageFile = new File("coverage_log");
        if(coverageFile.exists()){
            coverageFile.delete();
        }
        try {
            coverageFile.createNewFile();
            outputStream = new FileOutputStream(coverageFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Generator(GlobalState globalState){
        this.globalState=globalState;
        this.graphManager=new GraphManager(globalState.getOptions());
    }

    public GraphManager getGraphManager(){
        return graphManager;
    }

    public CypherQueryAdapter generateQuery() {
        List<CypherQueryAdapter> queries = new ArrayList<>();  // 创建一个列表来存储生成的查询
        CypherQueryAdapter query;

        // 获取需要生成查询的数量
        int queryCount = globalState.getOptions().getNrQueries();

        // 循环生成指定数量的查询
        //for (int i = 0; i < queryCount; i++) {
            graphManager.initialVariableManager();
            // 生成一个 RootClause（即一个 Cypher 查询）
            //RootClause rootClause = RootClause.generateRootClause(graphManager);

        String gdb = globalState.getOptions().getTargetDb();
        String cypherQuery;
        if (gdb.equals("memgraph")) {
            // 使用完全限定名
            org.example.project.cypher.standard_ast_mem.RootClause rootClause =
                    org.example.project.cypher.standard_ast_mem.RootClause.generateRootClause(graphManager);
            cypherQuery = rootClause.toCypher();
        } else {
            // 使用完全限定名
            org.example.project.cypher.standard_ast.RootClause rootClause =
                    org.example.project.cypher.standard_ast.RootClause.generateRootClause(graphManager);
            cypherQuery = rootClause.toCypher();
        }
        return new CypherQueryAdapter(cypherQuery);
           /* String cypherQuery = rootClause.toCypher();
            //System.out.println(CypherQuery);

            // 将生成的查询封装成 CypherQueryAdapter
            CypherQueryAdapter cypherQueryAdapter = new CypherQueryAdapter(cypherQuery);
            return cypherQueryAdapter;*/

            // 将生成的查询添加到列表中
            //queries.add(cypherQueryAdapter);
       // }

        // 返回生成的查询列表
       // return queries;
    }

    public void addNewRecord(CypherQueryAdapter sequence, boolean bugDetected, int resultLength, byte[] branchInfo, byte[] branchPairInfo){
        writeInfoln("coverage: "+coverageNum);
        writeInfoln("non_empty_coverage: "+nonEmptyCoverageNum);
        writeInfoln("result_size: "+resultLength);
    }

    public static void writeInfoln(String info){
        try {
            outputStream.write((""+System.currentTimeMillis()+"\n"+info+"\n").getBytes());
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
