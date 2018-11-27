# kafka-examples

## Kafka Clients - Producer API
In the `producers` directory are examples that use the Kafka Clients Producer API and take the written records and persist them to DataStax Enterprise using the DataStax Apache Kafka Connector

`producers/src/main/java/avro` - Example using KafkaAvroSerializer and AvroConverter

`producers/src/main/java/json` - Example using JsonSerializer and JsonConverter

`producers/src/main/java/primitive/string` - Example using StringSerializer and StringConverter

`producers/src/main/java/primitive/integer` - Example using IntegerSerializer and IntegerConverter

## Kafka Source Connectors
In the `connectors` directory are examples that use Kafka Source Connectors and take the written records and persist them to DataStax Enterprise using the DataStax Apache Kafka Connector

`connectors/jdbc-source-connector` - Example using JDBC Source Connector with and without schema in the JSON records
