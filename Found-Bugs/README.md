The following table gives a summary of the detected bugs, the column `Opt Bugs?` denotes whether the detected bug is a query optimization bug, the column `Subquery?` denotes whether the minimal bug-exposing query uses the subquery caluses, and the column `Variable Interaction?` denotes whether the minimal bug-exposing query has the `Single Clause Multiple Bound Variables Interaction` feature (see our paper for more details).  

| Issue                                                        | Confirmed? | Status | Opt Bugs? | Subquery? | Variable Interaction? |
| ------------------------------------------------------------ | ---------- | ------ | --------- | --------- | --------------------- |
| [Neo4j-13678](https://github.com/neo4j/neo4j/issues/13678)   | YES        | Fixed  | YES       | YES       | NO                    |
| [Neo4j-13611](https://github.com/neo4j/neo4j/issues/13611)   | YES        | Fixed  | YES       | YES       | NO                    |
| [Neo4j-13614](https://github.com/neo4j/neo4j/issues/13614)   | YES        | Fixed  | NO        | YES       | NO                    |
| [Neo4j-13615](https://github.com/neo4j/neo4j/issues/13615)   | YES        | Fixed  | NO        | YES       | NO                    |
| [Neo4j-13632](https://github.com/neo4j/neo4j/issues/13632)   | YES        | Fixed  | YES       | YES       | NO                    |
| [Neo4j-13667](https://github.com/neo4j/neo4j/issues/13667)   | YES        | Fixed  | NO        | YES       | NO                    |
| [Neo4j-13651](https://github.com/neo4j/neo4j/issues/13651)   | YES        | Fixed  | Unknown   | YES       | NO                    |
| [Neo4j-13666](https://github.com/neo4j/neo4j/issues/13666)   | YES        | Fixed  | NO        | YES       | NO                    |
| [Neo4j-13649](https://github.com/neo4j/neo4j/issues/13649)   | YES        | Fixed  | YES       | YES       | NO                    |
| [Neo4j-13583](https://github.com/neo4j/neo4j/issues/13583)   | YES        | Fixed  | YES       | NO        | YES                   |
| [Neo4j-13581](https://github.com/neo4j/neo4j/issues/13581)   | YES        | NO     | NO        | NO        | YES                   |
| [Neo4j-13591](https://github.com/neo4j/neo4j/issues/13591)   | YES        | Fixed  | YES       | NO        | YES                   |
| [Memgraph-2688](https://github.com/memgraph/memgraph/issues/2688) | YES        | Fixed  | YES       | NO        | YES                   |
| [Memgraph-3397](https://github.com/memgraph/memgraph/issues/3397) | YES        | NO     | YES       | NO        | YES                   |
| [Memgraph-3072](https://github.com/memgraph/memgraph/issues/3072) | YES        | NO     | Unknown   | YES       | NO                    |
| [Memgraph-2927](https://github.com/memgraph/memgraph/issues/2927) | YES        | NO     | YES       | YES       | NO                    |
| [Memgraph-2728](https://github.com/memgraph/memgraph/issues/2728) | YES        | NO     | Unknown   | NO        | NO                    |
| [Memgraph-2729](https://github.com/memgraph/memgraph/issues/2729) | YES        | NO     | Unknown   | NO        | YES                   |
| [Memgraph-2824](https://github.com/memgraph/memgraph/issues/2824) | YES        | NO     | YES       | YES       | NO                    |
| [Memgraph-3385](https://github.com/memgraph/memgraph/issues/3385) | YES        | NO     | NO        | YES       | NO                    |
