#!/usr/bin/env bash

set -e

docker-compose build --pull

docker-compose down -v || true

docker-compose up -d
sleep 10

docker-compose exec solr bin/solr create -c test -d basic_configs

curl -X POST -H 'Content-type:application/json' --data-binary '{
  "add-field":{"name":"key","type":"string","stored":true},
  "add-field":{"name":"value","type":"string","stored":true},
  "add-field":{"name":"offset","type":"string","stored":true}
}' http://localhost:8983/solr/test/schema

curl 'http://localhost:8983/solr/admin/collections?action=CREATE&name=.system'

(cd kafka-streaming-expression && mvn clean package)
curl -X POST -H 'Content-Type: application/octet-stream' --data-binary @kafka-streaming-expression/target/uber-kafka-streaming-expression-1.0-SNAPSHOT.jar 'http://localhost:8983/solr/.system/blob/kafka'

curl 'http://localhost:8983/solr/.system/blob?omitHeader=true'

curl 'http://localhost:8983/solr/test/config' -H 'Content-type:application/json' -d '{
   "add-runtimelib": { "name":"kafka", "version":1 }
}'

curl 'http://localhost:8983/solr/test/config' -H 'Content-type:application/json' -d '{
  "create-expressible": {
    "name": "kafkastreamingexpression",
    "class": "com.avalonconsult.solr.kafka.KafkaStreamingExpression",
    "runtimeLib": true
  }
}'

curl 'http://localhost:8983/solr/test/stream?expr=daemon(id=%22kafka%22,%20runInterval=%221000%22,%20update(test,%20batchSize=1,%20kafkastreamingexpression())%20)'

