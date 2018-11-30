# kafka-examples
This repository contains examples of using the DataStax Apache Kafka Connector. 

Documentation - https://docs.datastax.com/en/kafka/doc

Download - https://academy.datastax.com/downloads#connectors

Slack - https://academy.datastax.com/slack #kafka-connector

## Kafka Clients - Producer API
In the `producers` directory are examples that use the Kafka Clients Producer API and take the written records and persist them to DataStax Enterprise using the DataStax Apache Kafka Connector

`producers/src/main/java/avro` - Example using KafkaAvroSerializer / AvroConverter

`producers/src/main/java/json` - Example using JsonSerializer / JsonConverter + regular JSON record

`producers/src/main/java/json/udt` - Example using JsonSerializer / JsonConverter + mapping regular JSON to UDT in DSE

`producers/src/main/java/primitive/string` - Example using StringSerializer / StringConverter

`producers/src/main/java/primitive/integer` - Example using IntegerSerializer / IntegerConverter

## Kafka Source Connectors
In the `connectors` directory are examples that use Kafka Source Connectors and take the written records and persist them to DataStax Enterprise using the DataStax Apache Kafka Connector

`connectors/jdbc-source-connector` - Example using JDBC Source Connector with and without schema in the JSON records
