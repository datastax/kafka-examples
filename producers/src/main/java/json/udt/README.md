# Kafka Clients Producer API - JSON + UDT Example

## Prerequsites
1. Kafka installed and started ( https://kafka.apache.org/quickstart )
2. DataStax installed and started ( https://docs.datastax.com/en/install/6.7/install/installTOC.html )
3. DataStax Apache Kafka Connector installed ( https://downloads.datastax.com/kafka/kafka-connect-dse.tar.gz )
4. kafka-examples repository cloned ( `git clone https://github.com/datastax/kafka-examples.git` )
5. Maven Installed

## File Details

`connect-distributed-json-udt.properties` - Kafka Connect Worker configuration file for the json udt example

`dse-sink-udt.json` - DataStax Connector configuration file for the json udt example

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

Create DSE Schema and UDT in cqlsh
```
create keyspace if not exists kafka_examples with replication = {'class': 'NetworkTopologyStrategy', 'Cassandra': 1};
create type if not exists kafka_examples.stocks_type (name text, symbol text, datetime timestamp, exchange text, industry text, value double);
create table if not exists kafka_examples.stocks_udt_table (name text primary key, stocks FROZEN<stocks_type>);
```

Ensure DataStax Connector is in `plugin.path` in `connect-distributed-json-udt.properties`

Start Kafka Connect Worker
```
kafka/bin/connect-distributed.sh kafka-examples/producers/src/main/java/json/udt/connect-distributed-json-udt.properties &> worker-json-udt-example.log &
```

Below is the Converter configuration in `connect-distributed-json.properties`
```
key.converter=org.apache.kafka.connect.storage.StringConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
```

Ensure that `contactPoints` in `dse-sink-udt.json` points to the DSE cluster and that `loadBalancing.localDc` in `dse-sink-udt.json` is the data center name of the DSE Cluster

Start DataStax Connector
```
curl -X POST -H "Content-Type: application/json" -d @kafka-examples/producers/src/main/java/json/udt/dse-sink-udt.json "http://localhost:8083/connectors"
```
```
{"name":"dse-connector-udt-example","config":{"connector.class":"com.datastax.oss.kafka.sink.CassandraSinkConnector","tasks.max":"1","topics":"json-stream","contactPoints":"127.0.0.1","loadBalancing.localDc":"Cassandra","topic.json-stream.kafka_examples.stocks_udt_table.mapping":"name=key, stocks=value","topic.json-stream.kafka_examples.stocks_udt_table.consistencyLevel":"LOCAL_QUORUM","name":"dse-connector-udt-example"},"tasks":[],"type":null}
```

Below is the Connector Mapping in `dse-sink-udt.json`
```
"topic.json-stream.kafka_examples.stocks_udt_table.mapping":"name=key, stocks=value"
```

Confirm rows in DSE
```
cqlsh> select * from kafka_examples.stocks_udt_table limit 10;

 name                 | stocks
----------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------
                 LEAR |               {name: 'LEAR', symbol: 'LEA', datetime: '2018-12-01 09:16:07.873000+0000', exchange: 'NYSE', industry: 'MOTOR VEHICLES & PARTS', value: 144.03371}
               EXELON |                              {name: 'EXELON', symbol: 'EXC', datetime: '2018-12-01 09:15:04.873000+0000', exchange: 'NYSE', industry: 'ENERGY', value: 39.54775}
            STARBUCKS | {name: 'STARBUCKS', symbol: 'SBUX', datetime: '2018-12-01 09:15:47.873000+0000', exchange: 'NASDAQ', industry: 'HOTELS, RESTAURANTS & LEISURE', value: 69.56465}
     WEC ENERGY GROUP |                    {name: 'WEC ENERGY GROUP', symbol: 'WEC', datetime: '2018-12-01 09:19:44.873000+0000', exchange: 'NYSE', industry: 'ENERGY', value: 72.37324}
         HCA HOLDINGS |                       {name: 'HCA HOLDINGS', symbol: 'HCA', datetime: '2018-12-01 09:14:39.873000+0000', exchange: 'NYSE', industry: 'HEALTH', value: 156.65286}
       CHARLES SCHWAB |                 {name: 'CHARLES SCHWAB', symbol: 'SCHW', datetime: '2018-12-01 09:19:33.873000+0000', exchange: 'NYSE', industry: 'FINANCIALS', value: 43.26269}
 SIMON PROPERTY GROUP |           {name: 'SIMON PROPERTY GROUP', symbol: 'SPG', datetime: '2018-12-01 09:21:33.873000+0000', exchange: 'NYSE', industry: 'FINANCIALS', value: 175.62063}
                  ADP |                      {name: 'ADP', symbol: 'ADP', datetime: '2018-12-01 09:17:36.873000+0000', exchange: 'NASDAQ', industry: 'BUSINESS SERVICES', value: 144.49}
               APACHE |                              {name: 'APACHE', symbol: 'APA', datetime: '2018-12-01 09:21:44.873000+0000', exchange: 'NYSE', industry: 'ENERGY', value: 35.89624}
          WELLS FARGO |                        {name: 'WELLS FARGO', symbol: 'WFC', datetime: '2018-12-01 09:22:21.873000+0000', exchange: 'NYSE', industry: 'FINANCE', value: 56.57938}

(10 rows)
```
