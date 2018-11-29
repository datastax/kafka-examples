# Kafka Clients Producer API - JSON Example

## File Details
`JsonProducer.java` - Uses Kafka Clients Producer API to write 1 billion records to Kafka Topic named json-stream

`InfiniteJsonProducer.java` - Uses Kafka Clients Producer API to continuously write records to the configured Kafka Topic at a user specified rate

`connect-distributed-json.properties` - Kafka Connect Worker configuration file for the json example

`dse-sink.json` - DataStax Connector configuration file for the json example

`ticks/TickData.java` - Java Object representing a stock tick

`ticks/TickGenerator.java` - Uses the stocks in `resources/json/csv/exchangedata.csv` to create random tick values

`utils/ExchangeUtils.java` - Reads the stocks in `resources/json/csv/exchangedata.csv`

## Data Details
Both the `JsonProducer` and `InfiniteJsonProducer` create 500 stocks, set within the files by `NUMBER_OF_STOCKS`.

The `JsonProducer` creates 2 million records per stock by default, set within the file by `RECORDS_PER_STOCK`.

The `InfiniteJsonProducer` creates records until stopped.

The Kafka Producer for these records uses the stock name as the key and does not specify the partition id for which the record should be written. This means that the data distribution on the Kafka side is subject to the `key % num partitions` for the topic.

The DSE Schema below uses the stock symbol as the partition key and the timestamp created by the Kafka Producer as the clustering key. This means that there will be 500 partitions with 2 million rows each when using the `JsonProducer` and 500 partitions with an unbounded number of rows each when using the `InfiniteJsonProducer`.

The workload from Kafka to DSE is pure inserts with no overwrites as the Kafka Producer is using atomic timestamps.


## Steps
Create "json-stream" Topic
```
kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 100 --topic json-stream --config retention.ms=-1
```
* Note: the number of partitions affects the parallelism factor of the DataStax Connector. See docs - https://docs.datastax.com/en/kafka/doc.

Run JsonProducer, from `kafka-examples/producers`
```
mvn clean compile exec:java -Dexec.mainClass=json.JsonProducer &> json-producer.log &
```

Run InfiniteJsonProducer, from `kafka-examples/producer`
```
mvn clean compile exec:java -Dexec.mainClass=json.InfiniteJsonProducer -Dexec.args="5 json-stream localhost:9092" &> infinite-json-producer.log &
```
* Note: -Dexec.args="5 json-stream localhost:9092" ( 5=maxRequestsPerSecond | json-stream=Topic Name | localhost:9092=bootstrap servers )

Verify Records in `json-stream`
```
kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --from-beginning --property print.key=true --max-messages 5 --topic json-stream
WALMART	{"name":"WALMART","symbol":"WMT","value":89.80096928571885,"exchange":"NYSE","industry":"RETAIL","datetime":"2018-11-29T20:58:37.873"}
BERKSHIRE HATHAWAY	{"name":"BERKSHIRE HATHAWAY","symbol":"BRK.A","value":302576.97150908393,"exchange":"NYSE","industry":"FINANCE","datetime":"2018-11-29T20:58:38.873"}
APPLE	{"name":"APPLE","symbol":"APPL","value":207.69573976526948,"exchange":"NASDAQ","industry":"TECH","datetime":"2018-11-29T20:58:39.873"}
EXXON MOBIL	{"name":"EXXON MOBIL","symbol":"XOM","value":80.55810525493033,"exchange":"NYSE","industry":"ENERGY","datetime":"2018-11-29T20:58:40.873"}
MCKESSON	{"name":"MCKESSON","symbol":"MCK","value":124.95785820714305,"exchange":"NYSE","industry":"HEALTH","datetime":"2018-11-29T20:58:41.873"}
Processed a total of 5 messages
```

Create DSE Schema in cqlsh
```
create keyspace if not exists stocks with replication = {'class': 'NetworkTopologyStrategy', 'Cassandra': 1};
create table if not exists stocks.ticks (symbol text, datetime timestamp, exchange text, industry text, name text, value double, PRIMARY KEY (symbol, ts));
```

Start Kafka Connect Worker
```
kafka/bin/connect-distributed.sh kafka-examples/producers/src/main/java/json/connect-distributed-json.properties &> worker-json-example.log &
```

Below is the Converter configuration in `connect-distributed-json.properties`
```
key.converter=org.apache.kafka.connect.storage.StringConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
```

Create/Start Connector
```
curl -X POST -H "Content-Type: application/json" -d @kafka-examples/producers/src/main/java/json/dse-sink.json "http://localhost:8083/connectors"
{"name":"dse-connector-json-example","config":{"connector.class":"com.datastax.kafkaconnector.DseSinkConnector","tasks.max":"100","topics":"json-stream","contactPoints":"127.0.0.1","loadBalancing.localDc":"Cassandra","topic.json-stream.stocks.ticks.mapping":"name=key, symbol=value.symbol, datetime=value.datetime, exchange=value.exchange, industry=value.industry, value=value.value","topic.json-stream.stocks.ticks.consistencyLevel":"LOCAL_QUORUM","name":"dse-connector-json-example"},"tasks":[],"type":null}
```

Below is the Connector Mapping in `dse-sink.json`
```
"topic.json-stream.stocks.ticks.mapping": "name=key, symbol=value.symbol, datetime=value.datetime, exchange=value.exchange, industry=value.industry, value=value.value"
```

Confirm rows in DSE
```
cqlsh> select * from stocks.ticks limit 10;

 symbol | datetime                        | exchange | industry  | name        | value
--------+---------------------------------+----------+-----------+-------------+----------
   DLTR | 2018-11-26 19:18:34.483000+0000 |   NASDAQ | RETAILING | DOLLAR TREE | 86.40502
   DLTR | 2018-11-26 19:26:54.483000+0000 |   NASDAQ | RETAILING | DOLLAR TREE | 85.94761
   DLTR | 2018-11-26 19:35:14.483000+0000 |   NASDAQ | RETAILING | DOLLAR TREE | 86.19404
   DLTR | 2018-11-26 19:43:34.483000+0000 |   NASDAQ | RETAILING | DOLLAR TREE |   86.533
   DLTR | 2018-11-26 19:51:54.483000+0000 |   NASDAQ | RETAILING | DOLLAR TREE | 86.52447
   DLTR | 2018-11-26 20:00:14.483000+0000 |   NASDAQ | RETAILING | DOLLAR TREE |  86.1377
   DLTR | 2018-11-26 20:08:34.483000+0000 |   NASDAQ | RETAILING | DOLLAR TREE | 85.49169
   DLTR | 2018-11-26 20:16:54.483000+0000 |   NASDAQ | RETAILING | DOLLAR TREE |    84.69
   DLTR | 2018-11-26 20:25:14.483000+0000 |   NASDAQ | RETAILING | DOLLAR TREE |  83.9685
   DLTR | 2018-11-26 20:33:34.483000+0000 |   NASDAQ | RETAILING | DOLLAR TREE | 83.27707

(10 rows)
```
