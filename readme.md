# Understanding Query Optimization Bugs in Graph Database Systems



This repository contains the dataset, code, and supplementary material for our ASPLOS work "Understanding Query Optimization Bugs in Graph Database Systems". Recent years have witnessed an ever-growing usage of graph database management systems (GDBMSs) in various data-driven applications. Query optimization aims to improve the performance of database queries by identifying the most efficient way to execute them, and is an important stage of GDBMS workflow. Like other sophisticated systems, such as compilers, the query optimization process is complex and its implementation is prone to bugs. This work conducts the first characteristic study of query optimization bugs in GDBMSs, including the root causes, manifestation methods, and fix strategies, and delivers 10 novel and important findings about them. Based on the characteristic study, we also developed a testing tool tailored to uncover GDBMS query optimization bugs, and the tool found 20 unique GDBMS bugs, at least 10 of which are query optimization bugs. If you find work useful in your research, please cite our paper

```
@inproceedings{GDBMS-QueryOpt,
  title={Understanding query optimization bugs in graph database systems},
  author={Chen, Yuyu and Yu, Zhongxing},
  url = {https://dl.acm.org/doi/epdf/10.1145/3779212.3790244},
  booktitle={Proceedings of the 31st ACM International Conference on Architectural Support for Programming Languages and Operating Systems, Volume 2},
  year={2026},
  location = {Pittsburgh, USA},
  series = {ASPLOS 2026}
}
```



## GDBMS Issues Under Study



The [Studied-Issues](./Studied-Issues) folder contains the 102 issues used for conducting the characterstic study. These issues were selected from four popular GDBMSs, including Neo4j, Memgraph, RedisGraph, and Kuzu. For each issue, we manually inspect and analyze the issue message, source code, bug-exposing test, commit message, and bug patch to understand its root cause, manifestation method, and fix strategy. Overall, 10 novel and important findings on the causes, manifestations, and fixes of query optimization bugs are obtained at last.

## Tool for Exposing Query Optimization Bugs



To demonstrate the value of our characteristic study, we develop a testing tool based on our finding about the manifestation of query optimization bugs. The tool is implemented on top of [GDSmith](https://github.com/ddaa2000/GDsmith), and uses our findings to guide the generation of tests that can effecitively expose query optimization bugs. Our re-implementation consists of around 10K new non-comment lines of Java code, and the [src](./src) folder contains the source code. The packaged code is given in tool.jar.

#### Installation



- Java 21
- Maven
- Docker (to run database instances conveniently)

------

#### Running the tool ---- an example for running a Neo4j version



Before running the tool, make sure Docker is installed and running.
You should also manually pull the required database image first to ensure it is available locally:

```
docker pull neo4j:5.26.3
```



Then create a `config.json` file:

```
{
  "neo4j@5.26.3": {
    "port": 7687,
    "host": "localhost",
    "username": "neo4j",
    "password": "password"
  }
}
```



Now run the tool :

```
java -jar tool.jar --num-tries 500 --num-queries 1 --target-db neo4j
```


This means:
- the tool will generate **500 graphs**,
- for each graph, it will generate **1 query**,
- queries will be executed across the configured database(s),
- All generated graphs and queries, together with possible bugs observed during execution (such as wrong results, crashes, and internal errors), will be recorded in the `logs/` directory.


Note that the tool does not automatically create database users. You may need to manually create a user and grant the necessary privileges for remote connection, query execution, writing to databases, and creating or deleting databases.

------

##### Configuration



- `--num-tries <N>` → number of graphs to generate
- `--num-queries <M>` → queries per graph
- --target-db <neo4j|memgraph> → target GDBMS to test
- connection details are defined in `config.json`.

The config file uses keys of the form `<engine>@<label>` where:

- `<engine>` is one of `neo4j` and `memgraph`,
- `<label>` can be a version number or a custom tag.

To compare results across different versions, you only need to ensure that the required images are available and add the corresponding version entries to the config file.

------

#### Supported Databases



- **Neo4j**
- **Memgraph**

The supprt for other GDBMSs (for instance Kuzu) is in progress.

## Evaluation of the Tool



We evaluate the tool using the latest available release versions of Neo4j and Memgraph, the two most popular GDBMSs that use Cypher as the query language. During our testing period, if a new version of the two tested GDBMSs is released, we set up the tool to test the updated versions. More specifically, we test Neo4j starting from version 5.25.1 and Memgraph starting from version 3.0.0.

Overall, the tool found 20 unique bugs, including 12 in Neo4j and 8 in Memgraph. Among of them were confirmed, 11 fixed, at least 10 of them are query optimization bugs. These results demonstrate that the tool is practical and effective in detecting query optimization bugs in GDBMSs. The overall bug information can be found in [Found-Bugs](./Found-Bugs) folder.
