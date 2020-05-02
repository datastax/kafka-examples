# kafka-examples
This repository contains examples of using the DataStax Apache Kafka<sup>TM</sup> Connector. Feel free to use and modify any of these for your own purposes. There is no warranty or implied official support, but hopefully the examples will be useful as a starting point to show various ways of using the DataStax Apache Kafka Connector. And if you see anything that could be improved or added, issue reports and pull requests are always welcome.

Documentation - https://docs.datastax.com/en/kafka/doc

Download - https://downloads.datastax.com/#akc

Slack - https://academy.datastax.com/slack #kafka-connector

## Kafka Clients - Producer API
In the `producers` directory are examples that use the Kafka Clients Producer API and take the written records and persist them to DataStax Enterprise using the DataStax Apache Kafka Connector

[`producers/src/main/java/avro`](https://github.com/datastax/kafka-examples/tree/master/producers/src/main/java/avro) - Example using KafkaAvroSerializer / AvroConverter

[`producers/src/main/java/json`](https://github.com/datastax/kafka-examples/tree/master/producers/src/main/java/json) - Example using JsonSerializer / JsonConverter + regular JSON record

[`producers/src/main/java/json/udt`](https://github.com/datastax/kafka-examples/tree/master/producers/src/main/java/json/udt) - Example using JsonSerializer / JsonConverter + mapping regular JSON to UDT in DSE

[`producers/src/main/java/json/single-topic-multi-table`](https://github.com/datastax/kafka-examples/tree/master/producers/src/main/java/json/single-topic-multi-table) - Example using JsonSerializer / JsonConverter + mapping regular JSON topic to multiple tables in DSE

[`producers/src/main/java/primitive/string`](https://github.com/datastax/kafka-examples/tree/master/producers/src/main/java/primitive/string) - Example using StringSerializer / StringConverter

[`producers/src/main/java/primitive/integer`](https://github.com/datastax/kafka-examples/tree/master/producers/src/main/java/primitive/integer) - Example using IntegerSerializer / IntegerConverter

## Kafka Source Connectors
In the `connectors` directory are examples that use Kafka Source Connectors and take the written records and persist them to DataStax Enterprise using the DataStax Apache Kafka Connector

[`connectors/jdbc-source-connector`](https://github.com/datastax/kafka-examples/tree/master/connectors/jdbc-source-connector) - Example using JDBC Source Connector with and without schema in the JSON records
