package org.example.project.cypher.standard_ast_mem;

import java.util.ArrayList;
import java.util.List;

import org.example.project.Randomly;
import org.example.project.cypher.gen.GraphManager;

/**
 * RootClause represents the final query structure in Cypher.
 * It includes a list of clauses that can be a mix of ReadingClause, WritingClause,
 * ReadingWritingClause (e.g., MERGE), and ProjectingClause.
 *
 * Grammar (simplified context-free grammar):
 *
 * RootClause ::= ReadingClause* WritingClause* (ReadingWritingClause | ProjectingClause)?
 *
 * - ReadingClause represents a MATCH operation.
 * - WritingClause represents a CREATE or DELETE operation.
 * - ReadingWritingClause represents a combination of both reading and writing (e.g., MERGE).
 * - ProjectingClause is used to handle projections or RETURN clauses.
 *
 * The RootClause is the top-level clause that combines multiple other clauses,
 * allowing a flexible structure for complex Cypher queries.
 */

public class RootClause extends Clause {

    private final List<Clause> clauses;   // List of clauses, such as ReadingClause, WritingClause, etc.

    public RootClause() {
        super("Root");
        this.clauses = new ArrayList<>();
    }

    /**
     * Generates a simple RootClause by adding one ReadingClause.
     * This function currently generates a single ReadingClause.
     * In the future, it can be extended to include WritingClauses, or mixed clauses.
     *
     * @return A RootClause containing one simple ReadingClause.
     */
    public static RootClause generateRootClause(GraphManager graphManager) {
        RootClause rootClause = new RootClause();
       /* RemoveClause removeClause1=RemoveClause.generateRandomRemoveClause(graphManager);
        rootClause.addClause(removeClause1);*/
        Randomly randomly=new Randomly();
        boolean optional=randomly.getInteger(0,10)<7;
        ReadingClause readingClause1=ReadingClause.generateReadingClause(graphManager,optional);
        rootClause.addClause(readingClause1);
        Boolean hasWriting=false;


        int numOfClauses = randomly.getInteger(1, 5);
        for(int i=0;i<numOfClauses;i++){
            int clauseChoice= randomly.getInteger(0,100);//debug,100右边
            if(clauseChoice<20){
                if(optional) {
                    ReadingClause readingClause = ReadingClause.generateReadingClause(graphManager, true);
                    rootClause.addClause(readingClause);
                }
                else{
                    optional=randomly.getInteger(0,10)<7;
                    ReadingClause readingClause = ReadingClause.generateReadingClause(graphManager, optional);
                    rootClause.addClause(readingClause);
                }
            }
            else if(clauseChoice<40){
                WithClause withClause=WithClause.generateRandomWithClauseNew(graphManager);
                rootClause.addClause(withClause);
            }
            else if(clauseChoice<60){
                if(hasWriting) continue;
                UnwindClause unwindClause = UnwindClause.generateUnwindClause(graphManager);
                //System.out.println(unwindClause.toCypher());
                rootClause.addClause(unwindClause);
            }
            else if(clauseChoice<70){
                hasWriting=true;
                MergeClause mergeClause=MergeClause.generateRandomMergeClause(graphManager);
                rootClause.addClause(mergeClause);
                WithClause withClause=WithClause.generateRandomWithClauseNew(graphManager);
                rootClause.addClause(withClause);
                i++;
            }else if (clauseChoice < 80) {
                CallSubquery callSubquery = CallSubquery.generateCallSubquery(graphManager,true);
                rootClause.addClause(callSubquery);
            }
            else{//writing clause
                hasWriting=true;
                int writingClauseChoice= randomly.getInteger(0,7);//debug,右边是7
                if(writingClauseChoice<=2){
                    CreateClause createClause=CreateClause.generateRandomCreateClause(graphManager);
                    rootClause.addClause(createClause);

                }
                else if(writingClauseChoice==3){
                    SetClause setClause=SetClause.generateRandomSetClause(graphManager);
                    rootClause.addClause(setClause);
                }
                else if(writingClauseChoice==4){
                    DeleteClause deleteClause=DeleteClause.generateRandomDeleteClause(graphManager);
                    rootClause.addClause(deleteClause);
                }
                else if(writingClauseChoice==5){
                    DetachDeleteClause detachDeleteClause=DetachDeleteClause.generateRandomDetachDeleteClause(graphManager);
                    rootClause.addClause(detachDeleteClause);
                }
                else if(writingClauseChoice==6){
                    RemoveClause removeClause=RemoveClause.generateRandomRemoveClause(graphManager);
                    rootClause.addClause(removeClause);
                }
                WithClause withClause=WithClause.generateRandomWithClauseNew(graphManager);
                rootClause.addClause(withClause);
                i++;

            }

        }
        ReturnClause returnClause=ReturnClause.generateRandomReturnClause(graphManager);
        rootClause.addClause(returnClause);
        return rootClause;


    }

    public static RootClause generateSubRootClause(GraphManager graphManager, Boolean hasWriting) {
        RootClause rootClause = new RootClause();
       /* RemoveClause removeClause1=RemoveClause.generateRandomRemoveClause(graphManager);
        rootClause.addClause(removeClause1);*/
        Randomly randomly=new Randomly();
        boolean optional=randomly.getInteger(0,10)<7;
        ReadingClause readingClause1=ReadingClause.generateReadingClause(graphManager,optional);
        rootClause.addClause(readingClause1);


        int numOfClauses = randomly.getInteger(1, 5);
        for(int i=0;i<numOfClauses;i++){
            int clauseChoice= randomly.getInteger(0,100);//debug,100右边
            if(clauseChoice<20){
                if(optional) {
                    ReadingClause readingClause = ReadingClause.generateReadingClause(graphManager, true);
                    rootClause.addClause(readingClause);
                }
                else{
                    optional=randomly.getInteger(0,10)<7;
                    ReadingClause readingClause = ReadingClause.generateReadingClause(graphManager, optional);
                    rootClause.addClause(readingClause);
                }
            }
            else if(clauseChoice<40){
                WithClause withClause=WithClause.generateRandomWithClauseNew(graphManager);
                rootClause.addClause(withClause);
            }
            else if(clauseChoice<60){
                UnwindClause unwindClause = UnwindClause.generateUnwindClause(graphManager);
                //System.out.println(unwindClause.toCypher());
                rootClause.addClause(unwindClause);
            }
            else if(clauseChoice<70){
                MergeClause mergeClause=MergeClause.generateRandomMergeClause(graphManager);
                rootClause.addClause(mergeClause);
                WithClause withClause=WithClause.generateRandomWithClauseNew(graphManager);
                rootClause.addClause(withClause);
                i++;
            }else if (clauseChoice < 80) {
                CallSubquery callSubquery = CallSubquery.generateCallSubquery(graphManager,true);
                rootClause.addClause(callSubquery);
            }
            else{//writing clause
                int writingClauseChoice= randomly.getInteger(0,7);//debug,右边是7
                if(writingClauseChoice<=2){
                    CreateClause createClause=CreateClause.generateRandomCreateClause(graphManager);
                    rootClause.addClause(createClause);

                }
                else if(writingClauseChoice==3){
                    SetClause setClause=SetClause.generateRandomSetClause(graphManager);
                    rootClause.addClause(setClause);
                }
                else if(writingClauseChoice==4){
                    DeleteClause deleteClause=DeleteClause.generateRandomDeleteClause(graphManager);
                    rootClause.addClause(deleteClause);
                }
                else if(writingClauseChoice==5){
                    DetachDeleteClause detachDeleteClause=DetachDeleteClause.generateRandomDetachDeleteClause(graphManager);
                    rootClause.addClause(detachDeleteClause);
                }
                else if(writingClauseChoice==6){
                    RemoveClause removeClause=RemoveClause.generateRandomRemoveClause(graphManager);
                    rootClause.addClause(removeClause);
                }
                WithClause withClause=WithClause.generateRandomWithClauseNew(graphManager);
                rootClause.addClause(withClause);
                i++;

            }

        }
        ReturnClause returnClause=ReturnClause.generateRandomSubReturnClause(graphManager);
        rootClause.addClause(returnClause);
        return rootClause;


    }

    /**
     * Adds a clause (ReadingClause, WritingClause, etc.) to the RootClause.
     *
     * @param clause The clause to be added.
     */
    public void addClause(Clause clause) {
        this.clauses.add(clause);
    }

    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();
        for (Clause clause : clauses) {
            sb.append(clause.toCypher()).append(" ");
        }
        return sb.toString().trim();
    }

    @Override
    public boolean validate() {
        return clauses.stream().allMatch(Clause::validate);
    }
}
