# Kafka Clients Producer API - Avro Example

## Want to run the example automatically?

See [avro_example.sh](https://github.com/datastax/kafka-examples/edit/master/producers/src/main/java/avro/avro_example.sh) and [avro_script_output.txt](https://github.com/datastax/kafka-examples/edit/master/producers/src/main/java/avro/avro_script_output.txt) for details

## Prerequsites
1. Kafka installed and started ( https://kafka.apache.org/quickstart )
2. Schema Registry installed and started ( https://docs.confluent.io/current/schema-registry/docs/installation.html )
3. DataStax installed and started ( https://docs.datastax.com/en/install/6.7/install/installTOC.html )
4. DataStax Apache Kafka Connector installed ( https://downloads.datastax.com/kafka/kafka-connect-dse.tar.gz )
5. kafka-examples repository cloned ( `git clone https://github.com/datastax/kafka-examples.git` )
6. Maven Installed

## File Details
`AvroProducer.java` - Uses Kafka Clients Producer API to write 200 million records to Kafka Topic named avro-stream

`InfiniteAvroProducer.java` - Uses Kafka Clients Producer API to continuously write records to the configured Kafka Topic at a user specified rate

`connect-distributed-avro.properties` - Kafka Connect Worker configuration file for the avro example

`dse-sink-avro.json` - DataStax Connector configuration file for the avro example

`bigavro.asvc` - Avro schema for the example, read by AvroProducer and InfiniteAvroProducer

`create_avro_table_udt.cql` - DSE Schema for the example, contains 10 UDTs and 1 table definition that match the Avro Schema

## Data Details

The `AvroProducer` creates 200 million records by default, set within the file by `TOTAL_RECORDS`. Each record contains 10 segments with 10 fields per segment.

The `InfiniteAvroProducer` creates these same records until stopped.

## Steps
Create "avro-stream" Topic
```
kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 10 --topic avro-stream --config retention.ms=-1
```
* Note: the number of partitions affects the parallelism factor of the DataStax Connector. See docs - https://docs.datastax.com/en/kafka/doc.

Run AvroProducer, from `kafka-examples/producers` directory
```
mvn clean compile exec:java -Dexec.mainClass=avro.AvroProducer &> avro-producer.log &
```

Run InfiniteAvroProducer, from `kafka-examples/producer` directory
```
mvn clean compile exec:java -Dexec.mainClass=avro.InfiniteAvroProducer -Dexec.args="5 avro-stream" &> infinite-avro-producer.log &
```
* Note: -Dexec.args="5 avro-stream" ( 5=maxRequestsPerSecond | avro-stream=Topic Name )

Verify Records in `avro-stream`
```
confluent/bin/kafka-avro-console-consumer --bootstrap-server localhost:9092 --from-beginning --property print.key=true --max-messages 2 --topic avro-stream
```
```
{"key":"8"}	{"segment0":{"com.datastax.SEGMENT0":{"segment0_0":"0","segment0_1":"1","segment0_2":"2","segment0_3":"3","segment0_4":"4","segment0_5":"5","segment0_6":"6","segment0_7":"7","segment0_8":"8","segment0_9":"9"}},"segment1":{"com.datastax.SEGMENT1":{"segment1_0":"0","segment1_1":"1","segment1_2":"2","segment1_3":"3","segment1_4":"4","segment1_5":"5","segment1_6":"6","segment1_7":"7","segment1_8":"8","segment1_9":"9"}},"segment2":{"com.datastax.SEGMENT2":{"segment2_0":"0","segment2_1":"1","segment2_2":"2","segment2_3":"3","segment2_4":"4","segment2_5":"5","segment2_6":"6","segment2_7":"7","segment2_8":"8","segment2_9":"9"}},"segment3":{"com.datastax.SEGMENT3":{"segment3_0":"0","segment3_1":"1","segment3_2":"2","segment3_3":"3","segment3_4":"4","segment3_5":"5","segment3_6":"6","segment3_7":"7","segment3_8":"8","segment3_9":"9"}},"segment4":{"com.datastax.SEGMENT4":{"segment4_0":"0","segment4_1":"1","segment4_2":"2","segment4_3":"3","segment4_4":"4","segment4_5":"5","segment4_6":"6","segment4_7":"7","segment4_8":"8","segment4_9":"9"}},"segment5":{"com.datastax.SEGMENT5":{"segment5_0":"0","segment5_1":"1","segment5_2":"2","segment5_3":"3","segment5_4":"4","segment5_5":"5","segment5_6":"6","segment5_7":"7","segment5_8":"8","segment5_9":"9"}},"segment6":{"com.datastax.SEGMENT6":{"segment6_0":"0","segment6_1":"1","segment6_2":"2","segment6_3":"3","segment6_4":"4","segment6_5":"5","segment6_6":"6","segment6_7":"7","segment6_8":"8","segment6_9":"9"}},"segment7":{"com.datastax.SEGMENT7":{"segment7_0":"0","segment7_1":"1","segment7_2":"2","segment7_3":"3","segment7_4":"4","segment7_5":"5","segment7_6":"6","segment7_7":"7","segment7_8":"8","segment7_9":"9"}},"segment8":{"com.datastax.SEGMENT8":{"segment8_0":"0","segment8_1":"1","segment8_2":"2","segment8_3":"3","segment8_4":"4","segment8_5":"5","segment8_6":"6","segment8_7":"7","segment8_8":"8","segment8_9":"9"}},"segment9":{"com.datastax.SEGMENT9":{"segment9_0":"0","segment9_1":"1","segment9_2":"2","segment9_3":"3","segment9_4":"4","segment9_5":"5","segment9_6":"6","segment9_7":"7","segment9_8":"8","segment9_9":"9"}}}
{"key":"10"}	{"segment0":{"com.datastax.SEGMENT0":{"segment0_0":"0","segment0_1":"1","segment0_2":"2","segment0_3":"3","segment0_4":"4","segment0_5":"5","segment0_6":"6","segment0_7":"7","segment0_8":"8","segment0_9":"9"}},"segment1":{"com.datastax.SEGMENT1":{"segment1_0":"0","segment1_1":"1","segment1_2":"2","segment1_3":"3","segment1_4":"4","segment1_5":"5","segment1_6":"6","segment1_7":"7","segment1_8":"8","segment1_9":"9"}},"segment2":{"com.datastax.SEGMENT2":{"segment2_0":"0","segment2_1":"1","segment2_2":"2","segment2_3":"3","segment2_4":"4","segment2_5":"5","segment2_6":"6","segment2_7":"7","segment2_8":"8","segment2_9":"9"}},"segment3":{"com.datastax.SEGMENT3":{"segment3_0":"0","segment3_1":"1","segment3_2":"2","segment3_3":"3","segment3_4":"4","segment3_5":"5","segment3_6":"6","segment3_7":"7","segment3_8":"8","segment3_9":"9"}},"segment4":{"com.datastax.SEGMENT4":{"segment4_0":"0","segment4_1":"1","segment4_2":"2","segment4_3":"3","segment4_4":"4","segment4_5":"5","segment4_6":"6","segment4_7":"7","segment4_8":"8","segment4_9":"9"}},"segment5":{"com.datastax.SEGMENT5":{"segment5_0":"0","segment5_1":"1","segment5_2":"2","segment5_3":"3","segment5_4":"4","segment5_5":"5","segment5_6":"6","segment5_7":"7","segment5_8":"8","segment5_9":"9"}},"segment6":{"com.datastax.SEGMENT6":{"segment6_0":"0","segment6_1":"1","segment6_2":"2","segment6_3":"3","segment6_4":"4","segment6_5":"5","segment6_6":"6","segment6_7":"7","segment6_8":"8","segment6_9":"9"}},"segment7":{"com.datastax.SEGMENT7":{"segment7_0":"0","segment7_1":"1","segment7_2":"2","segment7_3":"3","segment7_4":"4","segment7_5":"5","segment7_6":"6","segment7_7":"7","segment7_8":"8","segment7_9":"9"}},"segment8":{"com.datastax.SEGMENT8":{"segment8_0":"0","segment8_1":"1","segment8_2":"2","segment8_3":"3","segment8_4":"4","segment8_5":"5","segment8_6":"6","segment8_7":"7","segment8_8":"8","segment8_9":"9"}},"segment9":{"com.datastax.SEGMENT9":{"segment9_0":"0","segment9_1":"1","segment9_2":"2","segment9_3":"3","segment9_4":"4","segment9_5":"5","segment9_6":"6","segment9_7":"7","segment9_8":"8","segment9_9":"9"}}}
Processed a total of 2 messages
```

Create DSE Schema in cqlsh by executing `create_avro_table_udt.cql`
```
cqlsh -f kafka-examples/producers/src/main/java/avro/create_avro_table_udt.cql
```

Ensure DataStax Connector is in `plugin.path` in `connect-distributed-avro.properties` and `confluent/share/` is in `plugin.path` in `connect-distributed-avro.properties`

Start Kafka Connect Worker
```
kafka/bin/connect-distributed.sh kafka-examples/producers/src/main/java/avro/connect-distributed-avro.properties &> worker-avro-example.log &
```

Below is the Converter configuration in `connect-distributed-json.properties`
```
key.converter=io.confluent.connect.avro.AvroConverter
key.converter.schema.registry.url=http://127.0.0.1:8081
value.converter=io.confluent.connect.avro.AvroConverter
value.converter.schema.registry.url=http://127.0.0.1:8081

key.converter.schemas.enable=true
value.converter.schemas.enable=true
```

Ensure that `contactPoints` in `dse-sink-avro.json` points to the DSE cluster and that `loadBalancing.localDc` in `dse-sink-avro.json` is the data center name of the DSE Cluster

Create/Start Connector
```
curl -X POST -H "Content-Type: application/json" -d @kafka-examples/producers/src/main/java/avro/dse-sink-avro.json "http://localhost:8083/connectors"
```
```
{"name":"dse-connector-avro-example","config":{"connector.class":"com.datastax.kafkaconnector.DseSinkConnector","tasks.max":"10","topics":"avro-stream","contactPoints":"127.0.0.1","loadBalancing.localDc":"Cassandra","topic.avro-stream.kafka_examples.avro_udt_table.mapping":"id=key.key, udt_col0=value.segment0,udt_col1=value.segment1,udt_col2=value.segment2,udt_col3=value.segment3,udt_col4=value.segment4,udt_col5=value.segment5,udt_col6=value.segment6,udt_col7=value.segment7,udt_col8=value.segment8,udt_col9=value.segment9","name":"dse-connector-avro-example"},"tasks":[{"connector":"dse-connector-avro-example","task":0},{"connector":"dse-connector-avro-example","task":1},{"connector":"dse-connector-avro-example","task":2},{"connector":"dse-connector-avro-example","task":3},{"connector":"dse-connector-avro-example","task":4},{"connector":"dse-connector-avro-example","task":5},{"connector":"dse-connector-avro-example","task":6},{"connector":"dse-connector-avro-example","task":7},{"connector":"dse-connector-avro-example","task":8},{"connector":"dse-connector-avro-example","task":9}],"type":null}```
```

Below is the Connector Mapping in `dse-sink-avro.json`
```
"topic.avro-stream.kafka_examples.avro_udt_table.mapping": "id=key.key, udt_col0=value.segment0,udt_col1=value.segment1,udt_col2=value.segment2,udt_col3=value.segment3,udt_col4=value.segment4,udt_col5=value.segment5,udt_col6=value.segment6,udt_col7=value.segment7,udt_col8=value.segment8,udt_col9=value.segment9"
```

Confirm rows in DSE
```
cqlsh> select * from kafka_examples.avro_udt_table limit 2;

 id    | udt_col0                                                                                                                                                                   | udt_col1                                                                                                                                                                   | udt_col2                                                                                                                                                                   | udt_col3                                                                                                                                                                   | udt_col4                                                                                                                                                                   | udt_col5                                                                                                                                                                   | udt_col6                                                                                                                                                                   | udt_col7                                                                                                                                                                   | udt_col8                                                                                                                                                                   | udt_col9
-------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  4317 | {segment0_0: '0', segment0_1: '1', segment0_2: '2', segment0_3: '3', segment0_4: '4', segment0_5: '5', segment0_6: '6', segment0_7: '7', segment0_8: '8', segment0_9: '9'} | {segment1_0: '0', segment1_1: '1', segment1_2: '2', segment1_3: '3', segment1_4: '4', segment1_5: '5', segment1_6: '6', segment1_7: '7', segment1_8: '8', segment1_9: '9'} | {segment2_0: '0', segment2_1: '1', segment2_2: '2', segment2_3: '3', segment2_4: '4', segment2_5: '5', segment2_6: '6', segment2_7: '7', segment2_8: '8', segment2_9: '9'} | {segment3_0: '0', segment3_1: '1', segment3_2: '2', segment3_3: '3', segment3_4: '4', segment3_5: '5', segment3_6: '6', segment3_7: '7', segment3_8: '8', segment3_9: '9'} | {segment4_0: '0', segment4_1: '1', segment4_2: '2', segment4_3: '3', segment4_4: '4', segment4_5: '5', segment4_6: '6', segment4_7: '7', segment4_8: '8', segment4_9: '9'} | {segment5_0: '0', segment5_1: '1', segment5_2: '2', segment5_3: '3', segment5_4: '4', segment5_5: '5', segment5_6: '6', segment5_7: '7', segment5_8: '8', segment5_9: '9'} | {segment6_0: '0', segment6_1: '1', segment6_2: '2', segment6_3: '3', segment6_4: '4', segment6_5: '5', segment6_6: '6', segment6_7: '7', segment6_8: '8', segment6_9: '9'} | {segment7_0: '0', segment7_1: '1', segment7_2: '2', segment7_3: '3', segment7_4: '4', segment7_5: '5', segment7_6: '6', segment7_7: '7', segment7_8: '8', segment7_9: '9'} | {segment8_0: '0', segment8_1: '1', segment8_2: '2', segment8_3: '3', segment8_4: '4', segment8_5: '5', segment8_6: '6', segment8_7: '7', segment8_8: '8', segment8_9: '9'} | {segment9_0: '0', segment9_1: '1', segment9_2: '2', segment9_3: '3', segment9_4: '4', segment9_5: '5', segment9_6: '6', segment9_7: '7', segment9_8: '8', segment9_9: '9'}
 25269 | {segment0_0: '0', segment0_1: '1', segment0_2: '2', segment0_3: '3', segment0_4: '4', segment0_5: '5', segment0_6: '6', segment0_7: '7', segment0_8: '8', segment0_9: '9'} | {segment1_0: '0', segment1_1: '1', segment1_2: '2', segment1_3: '3', segment1_4: '4', segment1_5: '5', segment1_6: '6', segment1_7: '7', segment1_8: '8', segment1_9: '9'} | {segment2_0: '0', segment2_1: '1', segment2_2: '2', segment2_3: '3', segment2_4: '4', segment2_5: '5', segment2_6: '6', segment2_7: '7', segment2_8: '8', segment2_9: '9'} | {segment3_0: '0', segment3_1: '1', segment3_2: '2', segment3_3: '3', segment3_4: '4', segment3_5: '5', segment3_6: '6', segment3_7: '7', segment3_8: '8', segment3_9: '9'} | {segment4_0: '0', segment4_1: '1', segment4_2: '2', segment4_3: '3', segment4_4: '4', segment4_5: '5', segment4_6: '6', segment4_7: '7', segment4_8: '8', segment4_9: '9'} | {segment5_0: '0', segment5_1: '1', segment5_2: '2', segment5_3: '3', segment5_4: '4', segment5_5: '5', segment5_6: '6', segment5_7: '7', segment5_8: '8', segment5_9: '9'} | {segment6_0: '0', segment6_1: '1', segment6_2: '2', segment6_3: '3', segment6_4: '4', segment6_5: '5', segment6_6: '6', segment6_7: '7', segment6_8: '8', segment6_9: '9'} | {segment7_0: '0', segment7_1: '1', segment7_2: '2', segment7_3: '3', segment7_4: '4', segment7_5: '5', segment7_6: '6', segment7_7: '7', segment7_8: '8', segment7_9: '9'} | {segment8_0: '0', segment8_1: '1', segment8_2: '2', segment8_3: '3', segment8_4: '4', segment8_5: '5', segment8_6: '6', segment8_7: '7', segment8_8: '8', segment8_9: '9'} | {segment9_0: '0', segment9_1: '1', segment9_2: '2', segment9_3: '3', segment9_4: '4', segment9_5: '5', segment9_6: '6', segment9_7: '7', segment9_8: '8', segment9_9: '9'}

(2 rows)
```
