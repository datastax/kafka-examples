# Kafka Clients Producer API - Single Topic + Multi Table Example

## Prerequsites
1. Kafka installed and started ( https://kafka.apache.org/quickstart )
2. DataStax installed and started ( https://docs.datastax.com/en/install/6.7/install/installTOC.html )
3. DataStax Apache Kafka Connector installed ( https://academy.datastax.com/downloads#connectors )
- Note - Requires DataStax Academy account
4. kafka-examples repository cloned ( `git clone https://github.com/datastax/kafka-examples.git` )
5. Maven Installed

## File Details

`connect-distributed-json-multi-table.properties` - Kafka Connect Worker configuration file for the json single topic / multi table example

`dse-sink-multi-table.json` - DataStax Connector configuration file for the json single topic / multi table example

See `producers/src/main/java/json` for more File Details

## Steps
Create "json-stream" Topic
```
kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 100 --topic json-stream --config retention.ms=-1
```
* Note: the number of partitions affects the parallelism factor of the DataStax Connector. See docs - https://docs.datastax.com/en/kafka/doc.

Consider decreasing `RECORDS_PER_STOCK` and `NUMBER_OF_STOCKS` in `JsonProducer` then Run JsonProducer, from `kafka-examples/producers` directory
```
mvn clean compile exec:java -Dexec.mainClass=json.JsonProducer &> json-producer.log &
```

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
create keyspace if not exists kafka_examples with replication = {'class': 'NetworkTopologyStrategy', 'Cassandra': 1};
create table if not exists kafka_examples.stocks_table_by_symbol (symbol text, datetime timestamp, exchange text, industry text, name text, value double, PRIMARY KEY (symbol, datetime));
create table if not exists kafka_examples.stocks_table_by_exchange (symbol text, datetime timestamp, exchange text, industry text, name text, value double, PRIMARY KEY (exchange, datetime));
create table if not exists kafka_examples.stocks_table_by_industry (symbol text, datetime timestamp, exchange text, industry text, name text, value double, PRIMARY KEY (industry, datetime));
```

Ensure DataStax Connector is in `plugin.path` in `connect-distributed-json-multi-table.properties`

Start Kafka Connect Worker
```
kafka/bin/connect-distributed.sh kafka-examples/producers/src/main/java/json/single-topic-multi-table/connect-distributed-json-multi-table.properties &> worker-json-multi-table-example.log &
```

Below is the Converter configuration in `connect-distributed-json-multi-table.properties`
```
key.converter=org.apache.kafka.connect.storage.StringConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
```

Ensure that `contactPoints` in `dse-sink-multi-table.json` points to the DSE cluster and that `loadBalancing.localDc` in `dse-sink-multi-table.json` is the data center name of the DSE Cluster

Start DataStax Connector
```
curl -X POST -H "Content-Type: application/json" -d @kafka-examples/producers/src/main/java/json/single-topic-multi-table/dse-sink-multi-table.json "http://localhost:8083/connectors"
```
```
{"name":"dse-connector-json-multi-table-example","config":{"connector.class":"com.datastax.kafkaconnector.DseSinkConnector","tasks.max":"10","topics":"json-stream","contactPoints":"127.0.0.1","loadBalancing.localDc":"Cassandra","topic.json-stream.kafka_examples.stocks_table_by_symbol.mapping":"symbol=value.symbol, datetime=value.datetime, exchange=value.exchange, industry=value.industry, name=key, value=value.value","topic.json-stream.kafka_examples.stocks_table_by_exchange.mapping":"symbol=value.symbol, datetime=value.datetime, exchange=value.exchange, industry=value.industry, name=key, value=value.value","topic.json-stream.kafka_examples.stocks_table_by_industry.mapping":"symbol=value.symbol, datetime=value.datetime, exchange=value.exchange, industry=value.industry, name=key, value=value.value","topic.json-stream.kafka_examples.stocks_table_by_symbol.consistencyLevel":"LOCAL_QUORUM","topic.json-stream.kafka_examples.stocks_table_by_exchange.consistencyLevel":"LOCAL_QUORUM","topic.json-stream.kafka_examples.stocks_table_by_industry.consistencyLevel":"LOCAL_QUORUM","name":"dse-connector-json-multi-table-example"},"tasks":[],"type":null}
```

Below is the Connector Mapping in `dse-sink-multi-table.json`
```
“topic.json-stream.kafka_examples.stocks_table_by_symbol.mapping”: “symbol=value.symbol, datetime=value.datetime, exchange=value.exchange, industry=value.industry, name=key, value=value.value”
“topic.json-stream.kafka_examples.stocks_table_by_exchange.mapping”: “symbol=value.symbol, datetime=value.datetime, exchange=value.exchange, industry=value.industry, name=key, value=value.value”
“topic.json-stream.kafka_examples.stocks_table_by_industry.mapping”: “symbol=value.symbol, datetime=value.datetime, exchange=value.exchange, industry=value.industry, name=key, value=value.value”
```

Confirm rows in DSE
```
cqlsh> select * from kafka_examples.stocks_table_by_symbol limit 10;

 symbol | datetime                        | exchange | industry  | name        | value
--------+---------------------------------+----------+-----------+-------------+----------
   DLTR | 2018-11-30 15:06:56.495000+0000 |   NASDAQ | RETAILING | DOLLAR TREE | 85.58024
   DLTR | 2018-11-30 15:15:16.495000+0000 |   NASDAQ | RETAILING | DOLLAR TREE | 85.95095
   DLTR | 2018-11-30 15:23:36.495000+0000 |   NASDAQ | RETAILING | DOLLAR TREE | 86.36792
   DLTR | 2018-11-30 15:31:56.495000+0000 |   NASDAQ | RETAILING | DOLLAR TREE |  87.2129
   DLTR | 2018-11-30 15:40:16.495000+0000 |   NASDAQ | RETAILING | DOLLAR TREE |  86.9603
   DLTR | 2018-11-30 15:48:36.495000+0000 |   NASDAQ | RETAILING | DOLLAR TREE | 87.49695
   DLTR | 2018-11-30 15:56:56.495000+0000 |   NASDAQ | RETAILING | DOLLAR TREE | 87.54067
   DLTR | 2018-11-30 16:05:16.495000+0000 |   NASDAQ | RETAILING | DOLLAR TREE | 87.21651
   DLTR | 2018-11-30 16:13:36.495000+0000 |   NASDAQ | RETAILING | DOLLAR TREE | 87.45975
   DLTR | 2018-11-30 16:21:56.495000+0000 |   NASDAQ | RETAILING | DOLLAR TREE | 88.32192

(10 rows)
```
```
cqlsh> select * from kafka_examples.stocks_table_by_exchange limit 10;
 exchange | datetime                        | industry               | name                       | symbol | value
----------+---------------------------------+------------------------+----------------------------+--------+------------
   NASDAQ | 2018-11-30 15:10:44.495000+0000 |            WHOLESALERS |         HD SUPPLY HOLDINGS |    HDS |   38.80785
   NASDAQ | 2018-11-30 15:10:54.495000+0000 |                 HEALTH |           LIFEPOINT HEALTH |   LPNT |   65.13379
   NASDAQ | 2018-11-30 15:11:03.495000+0000 | MOTOR VEHICLES & PARTS |                      TESLA |   TSLA |  351.75298
   NASDAQ | 2018-11-30 15:11:04.495000+0000 |              RETAILING |        ASCENA RETAIL GROUP |   ASNA |    3.99409
   NASDAQ | 2018-11-30 15:11:07.495000+0000 |                   TECH |                     NVIDIA |   NVDA |  213.54785
   NASDAQ | 2018-11-30 15:11:09.495000+0000 |             FINANCIALS |        FIFTH THIRD BANCORP |   FITB |   26.86183
   NASDAQ | 2018-11-30 15:11:15.495000+0000 |              MATERIALS |     A-MARK PRECIOUS METALS |   AMRK |   12.02509
   NASDAQ | 2018-11-30 15:11:16.495000+0000 |              RETAILING |             TRACTOR SUPPLY |   TSCO |   94.26373
   NASDAQ | 2018-11-30 15:11:23.495000+0000 |             FINANCIALS | JETBLUE TRANSPORTATIONWAYS |   JBLU |   17.86146
   NASDAQ | 2018-11-30 15:11:26.495000+0000 |                   TECH |        ACTIVISION BLIZZARD |   ATVI |   65.21916
(10 rows)
```
```
cqlsh> select * from kafka_examples.stocks_table_by_industry limit 10;

 industry      | datetime                        | exchange | name                        | symbol | value
---------------+---------------------------------+----------+-----------------------------+--------+-----------
 FOOD BEVERAGE | 2018-11-30 15:05:24.495000+0000 |   NASDAQ |                     PEPSICO |    PEP | 116.11559
 FOOD BEVERAGE | 2018-11-30 15:05:25.495000+0000 |     NYSE |      ARCHER DANIELS MIDLAND |    ADM |  50.02722
 FOOD BEVERAGE | 2018-11-30 15:05:44.495000+0000 |     NYSE |                   COCA-COLA |     KO |  45.97783
 FOOD BEVERAGE | 2018-11-30 15:06:01.495000+0000 |     NYSE |                 TYSON FOODS |    TSN |  62.58158
 FOOD BEVERAGE | 2018-11-30 15:06:12.495000+0000 |  PRIVATE |                         CHS |  PVT-4 |         0
 FOOD BEVERAGE | 2018-11-30 15:06:23.495000+0000 |     NYSE | PHILIP MORRIS INTERNATIONAL |     PM |  90.61268
 FOOD BEVERAGE | 2018-11-30 15:06:25.495000+0000 |   NASDAQ |                 KRAFT HEINZ |    KHC |  54.38447
 FOOD BEVERAGE | 2018-11-30 15:06:28.495000+0000 |   NASDAQ |      MONDELEZ INTERNATIONAL |   MDLZ |  43.35462
 FOOD BEVERAGE | 2018-11-30 15:07:08.495000+0000 |     NYSE |                ALTRIA GROUP |     MO |  64.48016
 FOOD BEVERAGE | 2018-11-30 15:07:25.495000+0000 |     NYSE |               GENERAL MILLS |    GIS |  44.25882

(10 rows)
```