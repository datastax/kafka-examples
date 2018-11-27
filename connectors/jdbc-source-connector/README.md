# JDBC Source Connector Example 

## File Details

## Prerequsites
1. Kafka installed and started
2. PostgresSQL installed ( https://www.postgresql.org/download/ )
3. JDBC Connector installed ( https://www.confluent.io/connector/kafka-connect-jdbc/ )

## Setup

In psql, change password of default `postgres` user, create the `customers` database, the `addresses` table, and write 5 rows

```
alter user postgres with password 'newpass';
create database customers;
create table addresses (personid int, firstname varchar(255), lastname varchar(255), street varchar(255), city varchar(255));
insert into addresses (personid, firstname, lastname, street, city) values (0, 'mookie', 'betts', '50 yawkey way', 'boston');
insert into addresses (personid, firstname, lastname, street, city) values (1, 'jd', 'martinez', '28 yawkey way', 'boston');
insert into addresses (personid, firstname, lastname, street, city) values (2, 'andrew', 'benintendi', '16 yawkey way', 'boston');
insert into addresses (personid, firstname, lastname, street, city) values (3, 'chris', 'sale', '41 yawkey way', 'boston');
insert into addresses (personid, firstname, lastname, street, city) values (4, 'david', 'price', '24 yawkey way', 'boston');
```

## JSON Records with Schema Steps

Add PostgreSQL driver to CLASSPATH and Start the Kafka Connect Worker
```
export CLASSPATH=/home/automaton/kafka-connect-jdbc-connector/lib/postgresql-9.4-1206-jdbc41.jar
kafka/bin/connect-distributed.sh connect-distributed-jdbc-with-schema.properties &> worker-jdbc-with-schema.log &
```

Start the JDBC Connector
```
curl -X POST -H "Content-Type: application/json" -d @jdbc-source-with-schema.json "http://localhost:8083/connectors"
...
{"name":"jdbc-postgres-source-with-schema-connector","config":{"connector.class":"io.confluent.connect.jdbc.JdbcSourceConnector","tasks.max":"1","mode":"bulk","connection.url":"jdbc:postgresql://localhost:5432/customers?user=postgres&password=newpass","table.whitelist":"addresses","topic.prefix":"jdbc-postgresql-with-schema-example-","name":"jdbc-postgres-source-with-schema-connector"},"tasks":[],"type":null}
```

Verify records in Kafka
```
kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --from-beginning --property print.key=true --max-messages 5 --topic jdbc-postgresql-with-schema-example-addresses
null	{"schema":{"type":"struct","fields":[{"type":"int32","optional":true,"field":"personid"},{"type":"string","optional":true,"field":"firstname"},{"type":"string","optional":true,"field":"lastname"},{"type":"string","optional":true,"field":"street"},{"type":"string","optional":true,"field":"city"}],"optional":false,"name":"addresses"},"payload":{"personid":0,"firstname":"mookie","lastname":"betts","street":"50 yawkey way","city":"boston"}}
null	{"schema":{"type":"struct","fields":[{"type":"int32","optional":true,"field":"personid"},{"type":"string","optional":true,"field":"firstname"},{"type":"string","optional":true,"field":"lastname"},{"type":"string","optional":true,"field":"street"},{"type":"string","optional":true,"field":"city"}],"optional":false,"name":"addresses"},"payload":{"personid":1,"firstname":"jd","lastname":"martinez","street":"28 yawkey way","city":"boston"}}
null	{"schema":{"type":"struct","fields":[{"type":"int32","optional":true,"field":"personid"},{"type":"string","optional":true,"field":"firstname"},{"type":"string","optional":true,"field":"lastname"},{"type":"string","optional":true,"field":"street"},{"type":"string","optional":true,"field":"city"}],"optional":false,"name":"addresses"},"payload":{"personid":2,"firstname":"andrew","lastname":"benintendi","street":"16 yawkey way","city":"boston"}}
null	{"schema":{"type":"struct","fields":[{"type":"int32","optional":true,"field":"personid"},{"type":"string","optional":true,"field":"firstname"},{"type":"string","optional":true,"field":"lastname"},{"type":"string","optional":true,"field":"street"},{"type":"string","optional":true,"field":"city"}],"optional":false,"name":"addresses"},"payload":{"personid":3,"firstname":"chris","lastname":"sale","street":"41 yawkey way","city":"boston"}}
null	{"schema":{"type":"struct","fields":[{"type":"int32","optional":true,"field":"personid"},{"type":"string","optional":true,"field":"firstname"},{"type":"string","optional":true,"field":"lastname"},{"type":"string","optional":true,"field":"street"},{"type":"string","optional":true,"field":"city"}],"optional":false,"name":"addresses"},"payload":{"personid":4,"firstname":"david","lastname":"price","street":"24 yawkey way","city":"boston"}}
Processed a total of 5 messages
```

Create DSE Schema
```
create keyspace if not exists kafka_examples with replication = {'class': 'NetworkTopologyStrategy', 'Cassandra': 1};
create table if not exists kafka_examples.addresses_with_schema (person_id int primary key, first_name text, last_name text, street text, city text);
```

Start the DataStax Connector
```
curl -X POST -H "Content-Type: application/json" -d @dse-sink-jdbc-with-schema.json "http://localhost:8083/connectors"
...
{"name":"dse-connector-jdbc-with-schema-example","config":{"connector.class":"com.datastax.kafkaconnector.DseSinkConnector","tasks.max":"1","topics":"jdbc-postgresql-with-schema-example-addresses","contactPoints":"127.0.0.1","loadBalancing.localDc":"Cassandra","topic.jdbc-postgresql-with-schema-example-addresses.kafka_examples.addresses_with_schema.mapping":"person_id=value.personid, first_name=value.firstname, last_name=value.lastname, street=value.street, city=value.city","topic.jdbc-postgresql-with-schema-example-addresses.kafka_examples.addresses_with_schema.consistencyLevel":"LOCAL_QUORUM","name":"dse-connector-jdbc-with-schema-example"},"tasks":[],"type":null}
```

Verify records in DSE
cqlsh> select * from kafka_examples.addresses_with_schema ;

 person_id | city   | first_name | last_name  | street
-----------+--------+------------+------------+---------------
         1 | boston |         jd |   martinez | 28 yawkey way
         0 | boston |     mookie |      betts | 50 yawkey way
         2 | boston |     andrew | benintendi | 16 yawkey way
         4 | boston |      david |      price | 24 yawkey way
         3 | boston |      chris |       sale | 41 yawkey way

(5 rows)


## JSON Records without Schema Steps

Add PostgreSQL driver to CLASSPATH and Start the Kafka Connect Worker
```
export CLASSPATH=/home/automaton/kafka-connect-jdbc-connector/lib/postgresql-9.4-1206-jdbc41.jar
kafka/bin/connect-distributed.sh connect-distributed-jdbc-without-schema.properties &> worker-jdbc-without-schema.log &
```

Start the JDBC Connector
```
curl -X POST -H "Content-Type: application/json" -d @jdbc-source-without-schema.json "http://localhost:8083/connectors"
...
{"name":"jdbc-postgres-source-no-schema-connector","config":{"connector.class":"io.confluent.connect.jdbc.JdbcSourceConnector","tasks.max":"1","mode":"bulk","connection.url":"jdbc:postgresql://localhost:5432/customers?user=postgres&password=newpass","table.whitelist":"addresses","topic.prefix":"jdbc-postgresql-no-schema-example-","name":"jdbc-postgres-source-no-schema-connector"},"tasks":[],"type":null}
```

Verify records in Kafka
```
kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --from-beginning --property print.key=true --max-messages 5 --topic jdbc-postgresql-without-schema-example-addresses
null	{"personid":0,"firstname":"mookie","lastname":"betts","street":"50 yawkey way","city":"boston"}
null	{"personid":1,"firstname":"jd","lastname":"martinez","street":"28 yawkey way","city":"boston"}
null	{"personid":2,"firstname":"andrew","lastname":"benintendi","street":"16 yawkey way","city":"boston"}
null	{"personid":3,"firstname":"chris","lastname":"sale","street":"41 yawkey way","city":"boston"}
null	{"personid":4,"firstname":"david","lastname":"price","street":"24 yawkey way","city":"boston"}
Processed a total of 5 messages
```

Create DSE Schema
```
create keyspace if not exists kafka_examples with replication = {'class': 'NetworkTopologyStrategy', 'Cassandra': 1};
create table if not exists kafka_examples.addresses_without_schema (person_id int primary key, first_name text, last_name text, street text, city text);
```

Start the DataStax Connector
```
curl -X POST -H "Content-Type: application/json" -d @dse-sink-jdbc-without-schema.json "http://localhost:8083/connectors"
...
{"name":"dse-connector-jdbc-without-schema-example","config":{"connector.class":"com.datastax.kafkaconnector.DseSinkConnector","tasks.max":"1","topics":"jdbc-postgresql-without-schema-example-addresses","contactPoints":"127.0.0.1","loadBalancing.localDc":"Cassandra","topic.jdbc-postgresql-without-schema-example-addresses.kafka_examples.addresses_without_schema.mapping":"person_id=value.personid, first_name=value.firstname, last_name=value.lastname, street=value.street, city=value.city","topic.jdbc-postgresql-without-schema-example-addresses.kafka_examples.addresses_without_schema.consistencyLevel":"LOCAL_QUORUM","name":"dse-connector-jdbc-without-schema-example"},"tasks":[],"type":null}
```

Verify records in DSE
cqlsh> select * from kafka_examples.addresses_without_schema ;

 person_id | city   | first_name | last_name  | street
-----------+--------+------------+------------+---------------
         1 | boston |         jd |   martinez | 28 yawkey way
         0 | boston |     mookie |      betts | 50 yawkey way
         2 | boston |     andrew | benintendi | 16 yawkey way
         4 | boston |      david |      price | 24 yawkey way
         3 | boston |      chris |       sale | 41 yawkey way

(5 rows)

