package org.example.project.cypher.standard_ast;

import java.util.ArrayList;
import java.util.List;

import org.example.project.Randomly;
import org.example.project.cypher.gen.GraphManager;

/**
 * RootClause represents the final query structure in Cypher.
 * It includes a list of clauses that can be a mix of ReadingClause, WritingClause,
 * ReadingWritingClause (e.g., MERGE), and ProjectingClause.
 * <p>
 * Grammar (simplified context-free grammar):
 * <p>
 * RootClause ::= ReadingClause* WritingClause* (ReadingWritingClause | ProjectingClause)?
 * <p>
 * - ReadingClause represents a MATCH operation.
 * - WritingClause represents a CREATE or DELETE operation.
 * - ReadingWritingClause represents a combination of both reading and writing (e.g., MERGE).
 * - ProjectingClause is used to handle projections or RETURN clauses.
 * <p>
 * The RootClause is the top-level clause that combines multiple other clauses,
 * allowing a flexible structure for complex Cypher queries.
 */

public class RootClause extends Clause {

    private final List<Clause> clauses;   // List of clauses, such as ReadingClause, WritingClause, etc.

    public RootClause() {
        super("Root");
        this.clauses = new ArrayList<>();
    }

    public List<Clause> getClauses() {
        return clauses;
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

        /*ReadingClause readingClause1 = ReadingClause.generateReadingClause(graphManager);
        rootClause.addClause(readingClause1);*///是否以match开头

        Randomly randomly = new Randomly();
        int numOfClauses = randomly.getInteger(2, 4);
        for (int i = 0; i < numOfClauses; i++) {
            int clauseChoice = randomly.getInteger(0, 100);
            if (clauseChoice < 15) {
                UnwindClause unwindClause = UnwindClause.generateUnwindClause(graphManager);
                //System.out.println(unwindClause.toCypher());
                rootClause.addClause(unwindClause);
            } else if (clauseChoice < 50) {
                ReadingClause readingClause = ReadingClause.generateReadingClause(graphManager);
                rootClause.addClause(readingClause);
            } else if (clauseChoice < 65) {
                WithClause withClause = WithClause.generateRandomWithClauseNew(graphManager);
                rootClause.addClause(withClause);
            } else if (clauseChoice < 75) {
                MergeClause mergeClause = MergeClause.generateRandomMergeClause(graphManager);
                rootClause.addClause(mergeClause);
                WithClause withClause = WithClause.generateRandomWithClauseNew(graphManager);
                rootClause.addClause(withClause);
                i++;
            } else if (clauseChoice < 85) {
                CallSubquery callSubquery = CallSubquery.generateCallSubquery(graphManager,true);
                rootClause.addClause(callSubquery);
            } else {//writing clause
                int writingClauseChoice = randomly.getInteger(0, 15);
                if (writingClauseChoice <= 1) {
                    CreateClause createClause = CreateClause.generateRandomCreateClause(graphManager);
                    rootClause.addClause(createClause);

                } else if (writingClauseChoice <= 5) {
                    SetClause setClause = SetClause.generateRandomSetClause(graphManager);
                    rootClause.addClause(setClause);
                } else if (writingClauseChoice <= 9) {
                    DeleteClause deleteClause = DeleteClause.generateRandomDeleteClause(graphManager);
                    rootClause.addClause(deleteClause);
                } else if (writingClauseChoice <= 11) {
                    DetachDeleteClause detachDeleteClause = DetachDeleteClause.generateRandomDetachDeleteClause(graphManager);
                    rootClause.addClause(detachDeleteClause);
                } else {
                    RemoveClause removeClause = RemoveClause.generateRandomRemoveClause(graphManager);
                    rootClause.addClause(removeClause);
                }
                WithClause withClause = WithClause.generateRandomWithClauseNew(graphManager);
                rootClause.addClause(withClause);
                i++;

            }

        }

        ReturnClause returnClause = ReturnClause.generateRandomReturnClause(graphManager);
        rootClause.addClause(returnClause);
        return rootClause;


    }

    public static RootClause generateSubRootClause(GraphManager graphManager, Boolean hasWriting) {
        RootClause rootClause = new RootClause();

        ReadingClause readingClause1 = ReadingClause.generateReadingClause(graphManager);
        rootClause.addClause(readingClause1);


        Randomly randomly = new Randomly();
        int numOfClauses;
        if (hasWriting) numOfClauses = randomly.getInteger(0, 4);
        else numOfClauses = randomly.getInteger(0, 3);
        for (int i = 0; i < numOfClauses; i++) {
            int clauseChoice;
            if (hasWriting) clauseChoice = randomly.getInteger(0, 100);
            else clauseChoice = randomly.getInteger(0, 70);
            if (clauseChoice < 20) {
                UnwindClause unwindClause = UnwindClause.generateUnwindClause(graphManager);
                //System.out.println(unwindClause.toCypher());
                rootClause.addClause(unwindClause);
            } else if (clauseChoice < 40) {
                ReadingClause readingClause = ReadingClause.generateReadingClause(graphManager);
                rootClause.addClause(readingClause);
            } else if (clauseChoice < 60) {
                WithClause withClause = WithClause.generateRandomWithClauseNew(graphManager);
                rootClause.addClause(withClause);
            } else if (clauseChoice < 70) {
                CallSubquery callSubquery = CallSubquery.generateCallSubquery(graphManager,hasWriting);
                rootClause.addClause(callSubquery);
            } else if (clauseChoice < 80) {
                MergeClause mergeClause = MergeClause.generateRandomMergeClause(graphManager);
                rootClause.addClause(mergeClause);
                WithClause withClause = WithClause.generateRandomWithClauseNew(graphManager);
                rootClause.addClause(withClause);
                i++;
            } else {//writing clause
                int writingClauseChoice = randomly.getInteger(0, 15);
                if (writingClauseChoice <= 1) {
                    CreateClause createClause = CreateClause.generateRandomCreateClause(graphManager);
                    rootClause.addClause(createClause);

                } else if (writingClauseChoice <= 5) {
                    SetClause setClause = SetClause.generateRandomSetClause(graphManager);
                    rootClause.addClause(setClause);
                } else if (writingClauseChoice <= 9) {
                    DeleteClause deleteClause = DeleteClause.generateRandomDeleteClause(graphManager);
                    rootClause.addClause(deleteClause);
                } else if (writingClauseChoice <= 11) {
                    DetachDeleteClause detachDeleteClause = DetachDeleteClause.generateRandomDetachDeleteClause(graphManager);
                    rootClause.addClause(detachDeleteClause);
                } else {
                    RemoveClause removeClause = RemoveClause.generateRandomRemoveClause(graphManager);
                    rootClause.addClause(removeClause);
                }
                WithClause withClause = WithClause.generateRandomWithClauseNew(graphManager);
                rootClause.addClause(withClause);
                i++;

            }

        }

        ReturnClause returnClause = ReturnClause.generateRandomSubReturnClause(graphManager);
        rootClause.addClause(returnClause);


        return rootClause;


    }


    public static RootClause generateExistRootClause(GraphManager graphManager) {
        RootClause rootClause = new RootClause();

        ReadingClause readingClause1 = ReadingClause.generateReadingClause(graphManager);
        rootClause.addClause(readingClause1);

        Randomly randomly = new Randomly();
        int numOfClauses = randomly.getInteger(0, 2);
        for (int i = 0; i < numOfClauses; i++) {
            int clauseChoice = randomly.getInteger(0, 100);
            if (clauseChoice < 20) {
                UnwindClause unwindClause = UnwindClause.generateUnwindClause(graphManager);
                rootClause.addClause(unwindClause);
            } else if (clauseChoice < 50) {
                ReadingClause readingClause = ReadingClause.generateReadingClause(graphManager);
                rootClause.addClause(readingClause);
            } else if (clauseChoice < 70) {
                WithClause withClause = WithClause.generateRandomWithClauseNew(graphManager);
                rootClause.addClause(withClause);
            } else if (clauseChoice < 80) {
                CallSubquery callSubquery = CallSubquery.generateCallSubquery(graphManager,false);
                rootClause.addClause(callSubquery);
            }

        }
        if (randomly.getInteger(0, 4) == 0) {
            ReturnClause returnClause = ReturnClause.generateRandomReturnClause(graphManager);
            rootClause.addClause(returnClause);
        }
        return rootClause;


    }

    public static RootClause generateCountRootClause(GraphManager graphManager) {
        RootClause rootClause = new RootClause();

        ReadingClause readingClause1 = ReadingClause.generateReadingClause(graphManager);
        rootClause.addClause(readingClause1);

        Randomly randomly = new Randomly();
        int numOfClauses = randomly.getInteger(0, 2);
        for (int i = 0; i < numOfClauses; i++) {
            int clauseChoice = randomly.getInteger(0, 100);
            if (clauseChoice < 20) {
                UnwindClause unwindClause = UnwindClause.generateUnwindClause(graphManager);
                rootClause.addClause(unwindClause);
            } else if (clauseChoice < 50) {
                ReadingClause readingClause = ReadingClause.generateReadingClause(graphManager);
                rootClause.addClause(readingClause);
            } else if (clauseChoice < 70) {
                WithClause withClause = WithClause.generateRandomWithClauseNew(graphManager);
                rootClause.addClause(withClause);
            } else if (clauseChoice < 80) {
                CallSubquery callSubquery = CallSubquery.generateCallSubquery(graphManager,false);
                rootClause.addClause(callSubquery);
            }


        }


        return rootClause;
    }

    public static RootClause generateCollectRootClause(GraphManager graphManager) {
        RootClause rootClause = new RootClause();

        ReadingClause readingClause1 = ReadingClause.generateReadingClause(graphManager);
        rootClause.addClause(readingClause1);

        Randomly randomly = new Randomly();
        int numOfClauses = randomly.getInteger(0, 2);
        for (int i = 0; i < numOfClauses; i++) {
            int clauseChoice = randomly.getInteger(0, 100);
            if (clauseChoice < 20) {
                UnwindClause unwindClause = UnwindClause.generateUnwindClause(graphManager);
                rootClause.addClause(unwindClause);
            } else if (clauseChoice < 50) {
                ReadingClause readingClause = ReadingClause.generateReadingClause(graphManager);
                rootClause.addClause(readingClause);
            } else if (clauseChoice < 70) {
                WithClause withClause = WithClause.generateRandomWithClauseNew(graphManager);
                rootClause.addClause(withClause);
            } else if (clauseChoice < 80) {
                CallSubquery callSubquery = CallSubquery.generateCallSubquery(graphManager,false);
                rootClause.addClause(callSubquery);
            }

        }

        ReturnClause returnClause = ReturnClause.generateRandomCollectReturnClause(graphManager);
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
