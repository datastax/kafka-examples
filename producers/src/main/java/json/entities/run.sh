#!/usr/bin/env bash

docker-compose up -d

docker-compose exec broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 100 --topic person --config retention.ms=-1

#docker-compose exec broker kafka-console-producer --broker-list localhost:9092 --topic person --property "parse.key=true" --property "key.separator=:"
docker-compose exec broker kafka-console-producer --broker-list localhost:9092 --topic person

{"name":  "Max", "phone": "5551234567", "dob": "dob1"}
{"name":  "John", "phone":  "2021234567", "dob": "dob2"}

docker-compose exec broker kafka-console-consumer --bootstrap-server localhost:9092 --from-beginning --property print.key=true --max-messages 5 --topic person

docker-compose exec dse cqlsh

create keyspace if not exists test with replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
create table if not exists test.person (name text, phone text, dob text, PRIMARY KEY (name));
create table if not exists test.phone (phone text, type text, PRIMARY KEY (phone));

curl -X POST -H "Content-Type: application/json" -d @src/main/java/json/entities/dse-sink-a.json "http://localhost:8083/connectors"

docker-compose exec dse cqlsh -e "select * from test.person limit 10;"

curl -X GET "http://localhost:8083/connectors/dse-connector-entity-example/status"



docker-compose exec broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 100 --topic phone --config retention.ms=-1

docker-compose exec broker kafka-console-producer --broker-list localhost:9092 --topic phone

{"phone": "5551234567", "type": "mobile"}
{"phone":  "2021234567", "type": "home"}

docker-compose exec broker kafka-console-consumer --bootstrap-server localhost:9092 --from-beginning --property print.key=true --max-messages 5 --topic phone

curl -X PUT -H "Content-Type: application/json" -d @src/main/java/json/entities/dse-sink-b.json "http://localhost:8083/connectors/dse-connector-entity-example/config"

docker-compose exec dse cqlsh -e "select * from test.phone limit 10;"

docker-compose exec broker kafka-topics --delete --zookeeper zookeeper:2181 --topic person