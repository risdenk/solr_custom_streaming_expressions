# Solr Custom Streaming Expressions

## Requirements
* Docker
* Bash

## Using this example
`./run.sh`

## Fixes to the StreamHandler
https://github.com/apache/lucene-solr/blob/master/solr/core/src/java/org/apache/solr/handler/StreamHandler.java#L181

core.getResourceLoader() doesn't work with runtimeLib. This must be changed to core.getMemClassLoader().

This repo has solr-core recompiled with that change.

