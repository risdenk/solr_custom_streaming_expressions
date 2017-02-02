# Solr Custom Streaming Expressions

## Overview
Currently registering custom streaming expressions requires that the jars be in place before starting Solr. Using the Solr Config API and Blob Store API, these jars can be added at runtime after Solr is started. This repo provides an example of loading a custom streaming expression jar into the blob store, registering the jar, adding the streaming expression, and testing it.

## Requirements
* Docker
* Bash

## Using this example
1. `./run.sh`
2. `curl 'http://localhost:8983/solr/test/stream?expr=daemon(id=%22kafka%22,%20runInterval=%221000%22,%20update(test,%20batchSize=1,%20kafkastreamingexpression())%20)'`
3. `docker-compose exec kafka bash -c "kafka-console-producer.sh --broker-list kafka:9092 --topic test"`

## Required Fix to the StreamHandler - SOLR-10087
* https://issues.apache.org/jira/browse/SOLR-10087
* https://github.com/apache/lucene-solr/blob/master/solr/core/src/java/org/apache/solr/handler/StreamHandler.java#L181

core.getResourceLoader() doesn't work with runtimeLib. This must be changed to core.getMemClassLoader().

This repo has solr-core recompiled with that change.

