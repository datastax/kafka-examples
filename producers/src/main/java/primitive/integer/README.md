# Primitive Integer Example

## Prerequsites
1. Kafka installed and started ( https://kafka.apache.org/quickstart )
- Note - If using Kafka version < 2.0, the Numeric Converters must be installed manually by adding newer connect-runtime.jar to `plugin.path`
2. DataStax installed and started ( https://docs.datastax.com/en/install/6.7/install/installTOC.html )
3. DataStax Apache Kafka Connector installed ( https://downloads.datastax.com/kafka/ )
4. kafka-examples repository cloned ( `git clone https://github.com/datastax/kafka-examples.git` )
5. Maven Installed

## File Details
`IntegerProducer.java` - Uses Kafka Clients Producer API to write 1000 records to Kafka Topic named integer_stream

`connect-distributed-integer.properties` - Kafka Connect Worker configuration file for the integer example

`dse-sink-integer.json` - DataStax Connector configuration file for the integer example

## Steps
Create "integer_stream" Topic
```
kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic integer_stream --config retention.ms=-1
```

Run StringProducer, from `kafka-examples/producers` directory
```
mvn clean compile exec:java -Dexec.mainClass=primitive.integer.IntegerProducer
```

Observe records in "integer_stream" Topic
```
kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --from-beginning --property print.key=true --max-messages 5 --topic integer_stream --key-deserializer org.apache.kafka.common.serialization.IntegerDeserializer
```
```
0	Australia
1	Asia
2	Australia
3	Africa
4	Antartica
Processed a total of 5 messages
```

Create DSE Schema in cqlsh
```
create keyspace if not exists kafka_examples with replication = {'class': 'NetworkTopologyStrategy', 'Cassandra': 1};
create table if not exists kafka_examples.integer_table (recordid int, continent text, primary key(recordid));
```

Ensure DataStax Connector is in `plugin.path` in `connect-distributed-integer.properties`

Start Kafka Connect Worker
```
kafka/bin/connect-distributed.sh kafka-examples/producers/src/main/java/primitive/integer/connect-distributed-integer.properties &> worker-integer-example.log &
```

Below is the Converter configuration in `connect-distributed-integer.properties`. Must be using Apache Kafka 2.0+ for IntegerConverter, see KAFKA-6913
```
key.converter=org.apache.kafka.connect.converters.IntegerConverter
value.converter=org.apache.kafka.connect.storage.StringConverter
```

Ensure that `contactPoints` in `dse-sink-integer.json` points to the DSE cluster and that `loadBalancing.localDc` in `dse-sink-integer.json` is the data center name of the DSE Cluster


Create/Start Connector
```
curl -X POST -H "Content-Type: application/json" -d @kafka-examples/producers/src/main/java/primitive/integer/dse-sink-integer.json "http://localhost:8083/connectors"
```
```
{"name":"dse-connector-integer-example","config":{"connector.class":"com.datastax.kafkaconnector.DseSinkConnector","tasks.max":"1","topics":"string_stream","contactPoints":"127.0.0.1","loadBalancing.localDc":"Cassandra","topic.integer_stream.kafka_examples.integer_table.mapping":"recordid=key, continent=value","topic.string_stream.kafka_examples.integer_table.consistencyLevel":"LOCAL_QUORUM","name":"dse-connector-integer-example"},"tasks":[],"type":null}
```

Below is the Connector Mapping in `dse-sink-integer.json`
```
"topic.integer_stream.kafka_examples.integer_table.mapping": "recordid=key, continent=value"
```

Confirm rows in DSE
```
cqlsh> select * from kafka_examples.integer_table limit 10;

 recordid | continent
----------+---------------
      769 | North America
       23 | North America
      114 |          Asia
      660 |          Asia
      893 |        Africa
       53 |        Africa
      987 |     Australia
      878 |          Asia
      110 |        Europe
       91 |        Europe

(10 rows)

```
```
dse/bin/dsbulk count -h localhost -k kafka_examples -t integer_table
Operation directory: /home/automaton/logs/COUNT_20181126-180133-307117.
total | failed | rows/s | mb/s | kb/row | p50 ms | p99ms | p999ms
1,000 |      0 |  1,238 | 0.00 |   0.00 |  41.55 | 41.68 |  41.68
Operation COUNT_20181126-180133-307117 completed successfully in 0 seconds.
1000
```
