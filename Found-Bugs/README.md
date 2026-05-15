The following table gives a summary of the detected bugs, the column `Opt Bugs?` denotes whether the detected bug is a query optimization bug, the column `Subquery?` denotes whether the minimal bug-exposing query uses the subquery caluses, and the column `Variable Interaction?` denotes whether the minimal bug-exposing query has the `Single Clause Multiple Bound Variables Interaction` feature (see our paper for more details). Note that the issue Neo4j-13651 was fixed recently, and our paper did not take this fix into account. 

| Issue                                                        | Confirmed? | Status | Opt Bugs? | Subquery? | Variable Interaction? |
| ------------------------------------------------------------ | ---------- | ------ | --------- | --------- | --------------------- |
| [Neo4j-13678](https://github.com/neo4j/neo4j/issues/13678)   | YES        | [Fixed](https://github.com/neo4j/neo4j/commit/3a13cc1b6a0df24620a1298cb638b551739c453e) | YES       | YES       | NO                    |
| [Neo4j-13611](https://github.com/neo4j/neo4j/issues/13611)   | YES        | [Fixed](https://github.com/neo4j/neo4j/commit/9c34aa63e3c82d9186d059a7c525da953c633846)  | YES       | YES       | NO                    |
| [Neo4j-13614](https://github.com/neo4j/neo4j/issues/13614)   | YES        | [Fixed](https://github.com/neo4j/neo4j/commit/ba98cab00454e72aada26454e02eddf8dbe98e1b)  | NO        | YES       | NO                    |
| [Neo4j-13615](https://github.com/neo4j/neo4j/issues/13615)   | YES        | [Fixed](https://github.com/neo4j/neo4j/commit/14fafef1313442c585a961b80409fd2d34c43af6)  | NO        | YES       | NO                    |
| [Neo4j-13632](https://github.com/neo4j/neo4j/issues/13632)   | YES        | [Fixed](https://github.com/neo4j/neo4j/commit/5f2de6b45a9d0557915978f3e8dbda52eb64efc5)  | YES       | YES       | NO                    |
| [Neo4j-13667](https://github.com/neo4j/neo4j/issues/13667)   | YES        | [Fixed](https://github.com/neo4j/neo4j/commit/ad7b97a7bff12e9634f29442197bf82004d3ce5c)  | NO        | YES       | NO                    |
| [Neo4j-13651](https://github.com/neo4j/neo4j/issues/13651)   | YES        | [Fixed](https://github.com/neo4j/neo4j/commit/0d7dbf6c34b4b3b907ab78757ac41776dc8aa14c)  | Unknown   | YES       | NO                    |
| [Neo4j-13666](https://github.com/neo4j/neo4j/issues/13666)   | YES        | [Fixed](https://github.com/neo4j/neo4j/commit/ad7b97a7bff12e9634f29442197bf82004d3ce5c)  | NO        | YES       | NO                    |
| [Neo4j-13649](https://github.com/neo4j/neo4j/issues/13649)   | YES        | [Fixed](https://github.com/neo4j/neo4j/commit/06510c8171e7aeb3cb3d1a1e3bf40fceb828817a)  | YES       | YES       | NO                    |
| [Neo4j-13583](https://github.com/neo4j/neo4j/issues/13583)   | YES        | Fixed  | YES       | NO        | YES                   |
| [Neo4j-13581](https://github.com/neo4j/neo4j/issues/13581)   | YES        | NO     | NO        | NO        | YES                   |
| [Neo4j-13591](https://github.com/neo4j/neo4j/issues/13591)   | YES        | [Fixed](https://github.com/neo4j/neo4j/commit/c21cb019c0668624d4eb70da2b27b0192279ab24)  | YES       | NO        | YES                   |
| [Memgraph-2688](https://github.com/memgraph/memgraph/issues/2688) | YES        | [Fixed](https://github.com/memgraph/memgraph/commit/16ac65d7f5f5d45c611c82649aa371c255abf456)  | YES       | NO        | YES                   |
| [Memgraph-3397](https://github.com/memgraph/memgraph/issues/3397) | YES        | NO     | YES       | NO        | YES                   |
| [Memgraph-3072](https://github.com/memgraph/memgraph/issues/3072) | YES        | NO     | Unknown   | YES       | NO                    |
| [Memgraph-2927](https://github.com/memgraph/memgraph/issues/2927) | YES        | NO     | YES       | YES       | NO                    |
| [Memgraph-2728](https://github.com/memgraph/memgraph/issues/2728) | YES        | NO     | Unknown   | NO        | NO                    |
| [Memgraph-2729](https://github.com/memgraph/memgraph/issues/2729) | YES        | NO     | Unknown   | NO        | YES                   |
| [Memgraph-2824](https://github.com/memgraph/memgraph/issues/2824) | YES        | NO     | YES       | YES       | NO                    |
| [Memgraph-3385](https://github.com/memgraph/memgraph/issues/3385) | YES        | NO     | NO        | YES       | NO                    |
