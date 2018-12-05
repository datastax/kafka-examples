## Kafka Clients Producer API
In the `producers` directory are examples that use the Kafka Clients Producer API and take the written records and persist them to DataStax Enterprise using the DataStax Apache Kafka Connector

[`producers/src/main/java/avro`](https://github.com/datastax/kafka-examples/tree/master/producers/src/main/java/avro) - Example using KafkaAvroSerializer / AvroConverter

[`producers/src/main/java/json`](https://github.com/datastax/kafka-examples/tree/master/producers/src/main/java/json) - Example using JsonSerializer / JsonConverter + regular JSON record

[`producers/src/main/java/json/udt`](https://github.com/datastax/kafka-examples/tree/master/producers/src/main/java/json/udt) - Example using JsonSerializer / JsonConverter + mapping regular JSON to UDT in DSE

[`producers/src/main/java/json/single-topic-multi-table`](https://github.com/datastax/kafka-examples/tree/master/producers/src/main/java/json/single-topic-multi-table) - Example using JsonSerializer / JsonConverter + mapping regular JSON topic to multiple tables in DSE

[`producers/src/main/java/primitive/string`](https://github.com/datastax/kafka-examples/tree/master/producers/src/main/java/primitive/string) - Example using StringSerializer / StringConverter

[`producers/src/main/java/primitive/integer`](https://github.com/datastax/kafka-examples/tree/master/producers/src/main/java/primitive/integer) - Example using IntegerSerializer / IntegerConverter
