Overall, we select 42 query optimization issues for Neo4j, 11 query optimization issues for Memgraph, 29 query optimization issues for RedisGraph, and 20 query optimization issues for Kuzu. The following table contains the links for these issues, and our empirical study results concerning their root causes, manifestation methods, and fix strategies. The column `Subquery?` denotes whether the (minimal) bug-exposing query uses the subquery caluses, and the column `Variable Interaction?` denotes whether the (minimal) bug-exposing query has the `Single Clause Multiple Bound Variables Interaction` feature (see our paper for more details). For the column `Fix Strategy`, Design denotes complex algorithm/data-structure design change, CONDStat denotes the change of simple conditional statement (in particular adding/deleting a few simple conditional statements), COND denotes condition change (in particular adding/modifying a few conditions), and POC denotes parameter or operator change. Besides, the developers do not explicitly give the links to the issue-addressing commits in the issue messages for a few issues, we thus try to search the issue-addressing commits from the commit history and hyperlink to them in the column `Fix Strategy` if we sucessfully find them. 

To help present the root causes, the different root cause categories are denoted using numbers as follows:
- 1: Buggy AST optimization and normalization -- Ignore specific legal query formats (10 issues)
- 2: Buggy AST optimization and normalization -- Limited semantic analysis (3 issues)
- 3: Buggy AST optimization and normalization -- other (4 issues)
- 4: Buggy query plan generation and selection -- Incomplete or incorrect query plan space -- wrong interpretation of clause semantic (11 issues)
- 5: Buggy query plan generation and selection -- Incomplete or incorrect query plan space --  wrong interpretation of plan operator semantic (4 issues)
- 6: Buggy query plan generation and selection -- Incomplete or incorrect query plan space -- failure to capture graph semantic or properly deal with query state (14 issues)
- 7: Buggy query plan generation and selection -- Incomplete or incorrect query plan space --  impropriate handle of edge cases (8 issues)
- 8: Buggy query plan generation and selection -- Incomplete or incorrect query plan space --  other (3 issues)
- 9: Buggy query plan generation and selection -- Inaccurate cost estimation (9 issues)
- 10: Buggy query plan generation and selection -- Defective plan space exploration algorithm (14 isses)
- 11: Buggy query plan generation and selection -- other (6 issues)
- 12: Buggy rule-based query plan transformation -- Incomplete precondition checking (6 issues)
- 13: Buggy rule-based query plan transformation -- Incorrect plan operator relation assumption (3 issues)
- 14: other (7 issues)

| ID  | GDBMS  | Issue| Root Cause | Subquery? | Variable Interaction? | Symptom | Buggy fix? | Fix Strategy| 
|------|------|------|------|------|------|------|------|------|
| 1 |Neo4j|[Neo4j-4315](https://github.com/neo4j/neo4j/issues/4315) | 6 | NO | YES | Wrong | NO |Design|
| 2 |Neo4j|[Neo4j-5921](https://github.com/neo4j/neo4j/issues/5921) | 3 | NO | YES | Error | NO | POC| 
| 3 |Neo4j|[Neo4j-5986](https://github.com/neo4j/neo4j/issues/5986) | 7 | NO | YES | Error | NO | COND + POC |
| 4 |Neo4j|[Neo4j-6615](https://github.com/neo4j/neo4j/issues/6615) | 10 | NO | NO | Performance| NO | Design|
| 5 |Neo4j|[Neo4j-6947](https://github.com/neo4j/neo4j/issues/6947) | 1 | NO | YES | Error | NO | CONDStat|
| 6 |Neo4j|[Neo4j-6956](https://github.com/neo4j/neo4j/issues/6956) | 3 | NO | YES | Error | NO | CONDStat | 
| 7 |Neo4j|[Neo4j-7415](https://github.com/neo4j/neo4j/issues/7415) | 1 | NO | YES | Performance | NO | Other|
| 8 |Neo4j|[Neo4j-9055](https://github.com/neo4j/neo4j/issues/9055) | 6 | NO | NO | Error | NO | Design| 
| 9 |Neo4j|[Neo4j-9435](https://github.com/neo4j/neo4j/issues/9435) | 10 | NO | YES | Error | YES | Design| 
| 10 |Neo4j|[Neo4j-10100](https://github.com/neo4j/neo4j/issues/10100) | 1 | NO | NO | Error| NO | CONDStat |
| 11 |Neo4j|[Neo4j-10831](https://github.com/neo4j/neo4j/issues/10831) | 11 | NO | YES | Error | NO | CONDStat |
| 12 |Neo4j|[Neo4j-11463](https://github.com/neo4j/neo4j/issues/11463) | 9 | NO | YES | Performance | NO | CONDStat| 
| 13 |Neo4j|[Neo4j-11677](https://github.com/neo4j/neo4j/issues/11677) | 9 | NO | NO | Unknown | YES | Design |
| 14 |Neo4j|[Neo4j-11944](https://github.com/neo4j/neo4j/issues/11944) | 11 | NO | YES | Unknown | Unknown | Unknown |
| 15 |Neo4j|[Neo4j-12195](https://github.com/neo4j/neo4j/issues/12195) | 7 | NO | YES | Error | NO | POC | 
| 16 |Neo4j|[Neo4j-12225](https://github.com/neo4j/neo4j/issues/12225) | 9 | NO | YES | Unknown | Unknown | Unknown| 
| 17 |Neo4j|[Neo4j-12232](https://github.com/neo4j/neo4j/issues/12232) | 1 | NO | NO | Wrong | Unknown | Unknown |
| 18 |Neo4j|[Neo4j-12247](https://github.com/neo4j/neo4j/issues/12247) | 10 | NO | YES | Wrong | YES | Unknown| 
| 19 |Neo4j|[Neo4j-12329](https://github.com/neo4j/neo4j/issues/12329) | 10 | NO | YES | Error | YES | Unknown |
| 20 |Neo4j|[Neo4j-12484](https://github.com/neo4j/neo4j/issues/12484) | 10 | NO | YES | Error | NO | Design| 
| 21 |Neo4j|[Neo4j-12583](https://github.com/neo4j/neo4j/issues/12583) | 6 | NO | NO | Wrong | NO | Design |
| 22 |Neo4j|[Neo4j-12618](https://github.com/neo4j/neo4j/issues/12618) | 6 | YES | YES | Wrong | NO | [Other](https://github.com/neo4j/neo4j/commit/afb912a54fce63657a3a763293e7289fbe027732)|
| 23 |Neo4j|[Neo4j-12759](https://github.com/neo4j/neo4j/issues/12759) | 9 | NO | NO | Performance | Unknown | Unknown| 
| 24 |Neo4j|[Neo4j-12760](https://github.com/neo4j/neo4j/issues/12760) | 5 | YES | NO | Wrong | NO | [Design](https://github.com/neo4j/neo4j/commit/ad37eb99f7374d4a79a60b07cac6a187fae159bd)|
| 25 |Neo4j|[Neo4j-12876](https://github.com/neo4j/neo4j/issues/12876) | 1 | NO | NO | Error | YES | Unknown| 
| 26 |Neo4j|[Neo4j-12926](https://github.com/neo4j/neo4j/issues/12926) | 7 | NO | NO | Performance | NO | Design |
| 27 |Neo4j|[Neo4j-12973](https://github.com/neo4j/neo4j/issues/12973) | 1 | NO | NO | Performance | NO | COND |
| 28 |Neo4j|[Neo4j-12982](https://github.com/neo4j/neo4j/issues/12982) | 6 | NO | YES | Performance | Unknown | Unknown |
| 29 |Neo4j|[Neo4j-13034](https://github.com/neo4j/neo4j/issues/13034) | 4 | NO | NO | Performance | Unknown | Unknown |
| 30 |Neo4j|[Neo4j-13036](https://github.com/neo4j/neo4j/issues/13036) | 1 | NO | YES | Performance | Unknown | Unknown |
| 31 |Neo4j|[Neo4j-13147](https://github.com/neo4j/neo4j/issues/13147) | 6 | YES | NO | Error | YES | Design|
| 32 |Neo4j|[Neo4j-13166](https://github.com/neo4j/neo4j/issues/13166) | 2 | YES | NO | Error | NO | [POC](https://github.com/neo4j/neo4j/commit/746bd25de708f531018da509876a7927384a38d5) |
| 33 |Neo4j|[Neo4j-13169](https://github.com/neo4j/neo4j/issues/13169) | 14 | YES | NO | Error | Unknown | Unknown |
| 34 |Neo4j|[Neo4j-13189](https://github.com/neo4j/neo4j/issues/13189) | 14 | YES | NO | Error | NO | Other| 
| 35 |Neo4j|[Neo4j-13210](https://github.com/neo4j/neo4j/issues/13210) | 9 | NO | NO| Unknown | Unknown |Unknown|
| 36 |Neo4j|[Neo4j-13245](https://github.com/neo4j/neo4j/issues/13245) | 2 | YES | NO | Error | NO | COND|
| 37 |Neo4j|[Neo4j-13246](https://github.com/neo4j/neo4j/issues/13246) | 4 | YES | NO | Error | NO | [CONDStat](https://github.com/neo4j/neo4j/commit/05d7950e7e2b87bfbabced460b943e561f7a2a06)|
| 38 |Neo4j|[Neo4j-13354](https://github.com/neo4j/neo4j/issues/13354) | 10 | NO | YES | Performance | NO | CONDStat|
| 39 |Neo4j|[Neo4j-13382](https://github.com/neo4j/neo4j/issues/13382) | 4 | YES | YES | Wrong | NO | [CONDStat + POC](https://github.com/neo4j/neo4j/commit/2522d735934c8a38e100888fd8eb51bbb9efe600)|
| 40 |Neo4j|[Neo4j-13427](https://github.com/neo4j/neo4j/issues/13427) | 4 | YES | NO | Error | NO | Design| 
| 41 |Neo4j|[Neo4j-13486](https://github.com/neo4j/neo4j/issues/13486) | 4 | YES | NO | Error | NO | [COND](https://github.com/neo4j/neo4j/commit/caa89ad60ea53b376568feb5092b3c0f3bb10dab)|
| 42 |Neo4j|[Neo4j-13552](https://github.com/neo4j/neo4j/issues/13552) | 4 | YES | NO | Error | NO | [Design](https://github.com/neo4j/neo4j/commit/b22e9d87f87ede42e806683c4ba4e0e3587ab662) |
| 43 |Memgraph|[Memgraph-1293](https://github.com/memgraph/memgraph/issues/1293) | 7 | NO | NO | Crash | NO | Other| 
| 44 |Memgraph|[Memgraph-1295](https://github.com/memgraph/memgraph/issues/1295) | 6 | NO | YES | Crash | NO | Design |
| 45 |Memgraph|[Memgraph-1416](https://github.com/memgraph/memgraph/issues/1416) | 11 | NO | NO | Performance | YES | Unknown |
| 46 |Memgraph|[Memgraph-1486](https://github.com/memgraph/memgraph/issues/1486) | 5 | NO | YES | Performance | NO | CONDStat |
| 47 |Memgraph|[Memgraph-1592](https://github.com/memgraph/memgraph/issues/1592) | 9 | NO | YES | Performance | YES | Design |
| 48 |Memgraph|[Memgraph-1611](https://github.com/memgraph/memgraph/issues/1611) | 11 | YES | NO | Performance | Unknown | Unknown |
| 49 |Memgraph|[Memgraph-1648](https://github.com/memgraph/memgraph/issues/1648) | 13 | NO | YES | Wrong | YES | Design |
| 50 |Memgraph|[Memgraph-1958](https://github.com/memgraph/memgraph/issues/1958) | 4 | Unknown | Unknown | Performance | Unknown |Unknown| 
| 51 |Memgraph|[Memgraph-2116](https://github.com/memgraph/memgraph/issues/2116) | 4 | YES | NO | Crash | YES | CONDStat |
| 52 |Memgraph|[Memgraph-2456](https://github.com/memgraph/memgraph/issues/2456) | 10 | YES | YES | Unknown | NO | Design |
| 53 |Memgraph|[Memgraph-2491](https://github.com/memgraph/memgraph/issues/2491) | 10 | YES | YES | Crash | NO | Other |
| 54 |Redisgraph|[Redisgraph-135](https://github.com/RedisGraph/RedisGraph/issues/135) | 11 | --| NO | Crash | NO | CONDStat |
| 55 |Redisgraph|[Redisgraph-192](https://github.com/RedisGraph/RedisGraph/issues/192) | 6 | -- | NO | Crash | NO | Design | 
| 56 |Redisgraph|[Redisgraph-249](https://github.com/RedisGraph/RedisGraph/issues/249) | 5 | -- | YES | Wrong | NO | Design | 
| 57 |Redisgraph|[Redisgraph-456](https://github.com/RedisGraph/RedisGraph/issues/456) | 14 | -- | YES | Performance | YES | Design |
| 58 |Redisgraph|[Redisgraph-471](https://github.com/RedisGraph/RedisGraph/issues/471) | 7 | -- | NO | Crash | NO | CONDStat | 
| 59 |Redisgraph|[Redisgraph-512](https://github.com/RedisGraph/RedisGraph/issues/512) | 12 | -- | NO | Crash | YES | Design | 
| 60 |Redisgraph|[Redisgraph-634](https://github.com/RedisGraph/RedisGraph/issues/634) | 1 | -- | Unknown | Unknown | NO | CONDStat |
| 61 |Redisgraph|[Redisgraph-772](https://github.com/RedisGraph/RedisGraph/issues/772) | 12 | -- | NO | Unknown | NO | CONDStat |
| 62 |Redisgraph|[Redisgraph-792](https://github.com/RedisGraph/RedisGraph/issues/792) | 12 | -- | NO | Crash | NO | Other| 
| 63 |Redisgraph|[Redisgraph-818](https://github.com/RedisGraph/RedisGraph/issues/818) | 12 | -- | NO | Wrong | NO | CONDStat |
| 64 |Redisgraph|[Redisgraph-858](https://github.com/RedisGraph/RedisGraph/issues/858) | 11 | -- | YES | Crash | NO | COND| 
| 65 |Redisgraph|[Redisgraph-869](https://github.com/RedisGraph/RedisGraph/issues/869) | 13 | -- | YES | Crash | YES | Design | 
| 66 |Redisgraph|[Redisgraph-1036](https://github.com/RedisGraph/RedisGraph/issues/1036) | 6 | -- | NO | Crash | NO | Design |
| 67 |Redisgraph|[Redisgraph-1068](https://github.com/RedisGraph/RedisGraph/issues/1068) | 12 | -- | YES | Crash | YES | Design |
| 68 |Redisgraph|[Redisgraph-1096](https://github.com/RedisGraph/RedisGraph/issues/1096) | 2 | -- | YES | Error | NO | Design |
| 69 |Redisgraph|[Redisgraph-1270](https://github.com/RedisGraph/RedisGraph/issues/1270) | 8 | -- | YES | Crash | YES | Design | 
| 70 |Redisgraph|[Redisgraph-1305](https://github.com/RedisGraph/RedisGraph/issues/1305) | 14 | -- | NO | Crash | YES | Design | 
| 71 |Redisgraph|[Redisgraph-1309](https://github.com/RedisGraph/RedisGraph/issues/1309) | 8 | -- | NO | Crash | YES | Design |
| 72 |Redisgraph|[Redisgraph-1313](https://github.com/RedisGraph/RedisGraph/issues/1313) | 7 | -- | NO | Crash | YES | CONDStat |
| 73 |Redisgraph|[Redisgraph-1346](https://github.com/RedisGraph/RedisGraph/issues/1346) | 6 | -- | NO | Wrong | YES | Design |
| 74 |Redisgraph|[Redisgraph-1394](https://github.com/RedisGraph/RedisGraph/issues/1394) | 4 | -- | NO | Crash | NO | Other |
| 75 |Redisgraph|[Redisgraph-1455](https://github.com/RedisGraph/RedisGraph/issues/1455) | 14 | -- | NO | Wrong | YES | Design |
| 76 |Redisgraph|[Redisgraph-1491](https://github.com/RedisGraph/RedisGraph/issues/1491) | 6 | -- | NO | Crash | NO | Design |
| 77 |Redisgraph|[Redisgraph-1493](https://github.com/RedisGraph/RedisGraph/issues/1493) | 6 | -- | NO | Crash | NO | CONDStat |
| 78 |Redisgraph|[Redisgraph-1688](https://github.com/RedisGraph/RedisGraph/issues/1688) | 6 | -- | NO | Unknown | YES | Design | 
| 79 |Redisgraph|[Redisgraph-2206](https://github.com/RedisGraph/RedisGraph/issues/2206) | 1 | -- | NO | Wrong | NO | Design |
| 80 |Redisgraph|[Redisgraph-2424](https://github.com/RedisGraph/RedisGraph/issues/2424) | 14 | -- | NO | Crash | YES | Design |
| 81 |Redisgraph|[Redisgraph-2631](https://github.com/RedisGraph/RedisGraph/issues/2631) | 12 | -- | NO | Crash | YES | Design |
| 82 |Redisgraph|[Redisgraph-2836](https://github.com/RedisGraph/RedisGraph/issues/2836) | 7 | -- | NO | Crash | NO | Design |
| 83 |Kuzu|[Kuzu-103](https://github.com/kuzudb/kuzu/issues/103) | 10 | -- | Unknown | Unknown | NO | Design |
| 84 |Kuzu|[Kuzu-112](https://github.com/kuzudb/kuzu/issues/112) | 10 | -- | NO | Performance | NO | Design |
| 85 |Kuzu|[Kuzu-127](https://github.com/kuzudb/kuzu/issues/127) | 6 | -- | NO | Unknown | NO | Design |
| 86 |Kuzu|[Kuzu-248](https://github.com/kuzudb/kuzu/issues/248) | 3 | -- | Unknown | Unknown | Unknown | Unknown|
| 87 |Kuzu|[Kuzu-259](https://github.com/kuzudb/kuzu/issues/259) | 4 | -- | NO | Performance | NO | Design |
| 88 |Kuzu|[Kuzu-302](https://github.com/kuzudb/kuzu/issues/302) | 10 | -- | YES | Performance | NO | Design |
| 89 |Kuzu|[Kuzu-305](https://github.com/kuzudb/kuzu/issues/305) | 7 | -- | NO | Unknown | NO | Design |
| 90 |Kuzu|[Kuzu-327](https://github.com/kuzudb/kuzu/issues/327) | 9 | -- | NO | Unknown | Unknown | Unknown|
| 91 |Kuzu|[Kuzu-419](https://github.com/kuzudb/kuzu/issues/419) | 8 | -- | NO | Crash | YES | Design |
| 92 |Kuzu|[Kuzu-584](https://github.com/kuzudb/kuzu/issues/584) | 4 | -- | Unknown | Unknown | Unknown | Unknown|
| 93 |Kuzu|[Kuzu-606](https://github.com/kuzudb/kuzu/issues/606) | 5 | -- | Unknown | Unknown | Unknown | Unknown|
| 94 |Kuzu|[Kuzu-823](https://github.com/kuzudb/kuzu/issues/823) | 10 | -- | NO | Error | YES | Design |
| 95 |Kuzu|[Kuzu-2202](https://github.com/kuzudb/kuzu/issues/2202) | 9 | -- | YES | Performance | Unknown | Unknown|
| 96 |Kuzu|[Kuzu-2360](https://github.com/kuzudb/kuzu/issues/2360) | 10 | -- | YES | Performance | Unknown | Unknown|
| 97 |Kuzu|[Kuzu-2648](https://github.com/kuzudb/kuzu/issues/2648) | 14 | -- | NO | Wrong | NO | Design |
| 98 |Kuzu|[Kuzu-3606](https://github.com/kuzudb/kuzu/issues/3606) | 13 | -- | NO | Wrong | Unknown | Unknown|
| 99 |Kuzu|[Kuzu-4281](https://github.com/kuzudb/kuzu/issues/4281) | 10 | -- | YES | Unknown | Unknown | Unknown|
| 100 |Kuzu|[Kuzu-4967](https://github.com/kuzudb/kuzu/issues/4967) | 9 | -- |  Unknown | Performance | Unknown | Unknown|
| 101 |Kuzu|[Kuzu-5040](https://github.com/kuzudb/kuzu/issues/5040) | 1 | -- | NO | Performance | NO | Design |
| 102 |Kuzu|[Kuzu-5177](https://github.com/kuzudb/kuzu/issues/5177) | 3 | -- | YES | Error | NO | Design |
