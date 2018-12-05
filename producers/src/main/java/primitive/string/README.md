# Primitive String Example

## Prerequsites
1. Kafka installed and started ( https://kafka.apache.org/quickstart )
2. DataStax installed and started ( https://docs.datastax.com/en/install/6.7/install/installTOC.html )
3. DataStax Apache Kafka Connector installed ( https://academy.datastax.com/downloads#connectors )
- Note - Requires DataStax Academy account
4. kafka-examples repository cloned ( `git clone https://github.com/datastax/kafka-examples.git` )
5. Maven Installed

## File Details
`StringProducer.java` - Uses Kafka Clients Producer API to write 1000 records to Kafka Topic named string_stream

`connect-distributed-string.properties` - Kafka Connect Worker configuration file for the string example

`dse-sink-string.json` - DataStax Connector configuration file for the string example

## Steps
Create "string_stream" Topic
```
kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic string_stream --config retention.ms=-1
```

Run StringProducer, from `kafka-examples/producers` directory
```
mvn clean compile exec:java -Dexec.mainClass=primitive.string.StringProducer
```

Observe records in "string_stream" Topic
```
kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --from-beginning --property print.key=true --max-messages 5 --topic string_stream
```
```
record-id-0	Australia
record-id-1	South America
record-id-2	Africa
record-id-3	Africa
record-id-4	Asia
Processed a total of 5 messages
```

Create DSE Schema in cqlsh
```
create keyspace if not exists kafka_examples with replication = {'class': 'NetworkTopologyStrategy', 'Cassandra': 1};
create table if not exists kafka_examples.string_table (recordid text, continent text, primary key(recordid));
```

Ensure DataStax Connector is in `plugin.path` in `connect-distributed-string.properties`

Start Kafka Connect Worker
```
kafka/bin/connect-distributed.sh kafka-examples/producers/src/main/java/primitive/string/connect-distributed-string.properties &> worker-string-example.log &
```

Below is the Converter configuration in `connect-distributed-string.properties`
```
key.converter=org.apache.kafka.connect.storage.StringConverter
value.converter=org.apache.kafka.connect.storage.StringConverter
```

Ensure that `contactPoints` in `dse-sink-string.json` points to the DSE cluster and that `loadBalancing.localDc` in `dse-sink-string.json` is the data center name of the DSE Cluster


Create/Start Connector
```
curl -X POST -H "Content-Type: application/json" -d @kafka-examples/producers/src/main/java/primitive/string/dse-sink-string.json "http://localhost:8083/connectors"
```
```
{"name":"dse-connector-string-example","config":{"connector.class":"com.datastax.kafkaconnector.DseSinkConnector","tasks.max":"1","topics":"string_stream","contactPoints":"127.0.0.1","loadBalancing.localDc":"Cassandra","topic.string_stream.kafka_examples.string_table.mapping":"recordid=key, continent=value","topic.string_stream.kafka_examples.string_table.consistencyLevel":"LOCAL_QUORUM","name":"dse-connector-string-example"},"tasks":[],"type":null}
```

Below is the Connector Mapping in `dse-sink-string.json`
```
"topic.string_stream.kafka_examples.string_table.mapping": "recordid=key, continent=value"
```

Confirm rows in DSE
```
cqlsh> select * from kafka_examples.string_table limit 10;

 recordid      | continent
---------------+---------------
  record-id-47 |          Asia
 record-id-335 | North America
 record-id-981 |        Europe
 record-id-482 | South America
  record-id-50 |     Antartica
 record-id-772 |     Australia
 record-id-826 |     Antartica
 record-id-700 | South America
 record-id-995 | South America
 record-id-277 |        Europe

(10 rows)
```
```
dse/bin/dsbulk count -h localhost -k kafka_examples -t string_table
Operation directory: /home/automaton/logs/COUNT_20181126-143256-512917.
total | failed | rows/s | mb/s | kb/row | p50 ms |  p99ms | p999ms
1,000 |      0 |  1,458 | 0.02 |   0.01 | 101.97 | 102.24 | 102.24
Operation COUNT_20181126-143256-512917 completed successfully in 0 seconds.
1000
```
