#!/usr/bin/env bash

set -e

docker-compose pull

docker-compose down -v || true

docker-compose up -d
sleep 10

docker-compose exec solr bin/solr create -c test

curl 'http://localhost:8983/solr/admin/collections?action=CREATE&name=.system'

(cd custom-streaming-expression && mvn clean package)
curl -X POST -H 'Content-Type: application/octet-stream' --data-binary @custom-streaming-expression/target/custom-streaming-expression-1.0-SNAPSHOT.jar 'http://localhost:8983/solr/.system/blob/test'

curl 'http://localhost:8983/solr/.system/blob?omitHeader=true'

curl 'http://localhost:8983/solr/test/config' -H 'Content-type:application/json' -d '{
   "add-runtimelib": { "name":"test", "version":1 }
}'

curl 'http://localhost:8983/solr/test/config' -H 'Content-type:application/json' -d '{
  "create-expressible": {
    "name": "customstreamingexpression",
    "class": "com.avalonconsult.solr.CustomStreamingExpression",
    "runtimeLib": true
  }
}'

curl 'http://localhost:8983/solr/test/stream?expr=customstreamingexpression()'

docker-compose down -v

