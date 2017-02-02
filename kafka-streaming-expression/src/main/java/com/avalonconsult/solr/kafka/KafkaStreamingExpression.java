/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.avalonconsult.solr.kafka;

import java.io.IOException;
import java.util.*;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.solr.client.solrj.io.Tuple;
import org.apache.solr.client.solrj.io.comp.StreamComparator;
import org.apache.solr.client.solrj.io.stream.StreamContext;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.apache.solr.client.solrj.io.stream.expr.Explanation;
import org.apache.solr.client.solrj.io.stream.expr.Expressible;
import org.apache.solr.client.solrj.io.stream.expr.StreamExplanation;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpression;
import org.apache.solr.client.solrj.io.stream.expr.StreamExpressionParameter;
import org.apache.solr.client.solrj.io.stream.expr.StreamFactory;

public class KafkaStreamingExpression extends TupleStream implements Expressible {
  private KafkaConsumer<String, String> consumer;
  private Deque<Tuple> tupleQueue = new ArrayDeque<>();

  public KafkaStreamingExpression() {
  }

  public KafkaStreamingExpression(StreamExpression expression, StreamFactory factory) throws IOException{
  }

  @Override
  public Map toMap(final Map<String, Object> map) {
    return super.toMap(map);
  }

  @Override
  public void setStreamContext(StreamContext context) {

  }

  @Override
  public List<TupleStream> children() {
    return null;
  }

  @Override
  public void open() throws IOException {
    Properties props = new Properties();
    props.put("bootstrap.servers", "kafka:9092");
    props.put("group.id", "solr");
    props.put("enable.auto.commit", "true");
    props.put("auto.commit.interval.ms", "1000");
    props.put("session.timeout.ms", "30000");
    props.put("key.deserializer", StringDeserializer.class);
    props.put("value.deserializer", StringDeserializer.class);
    consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Collections.singletonList("test"));
  }

  private Tuple createTuple(ConsumerRecord<String, String> record) {
    Map<String, Object> m = new HashMap<>();
    m.put("id", String.valueOf(UUID.randomUUID()));
    m.put("key", record.key());
    m.put("value", record.value());
    m.put("offset", record.offset());
    return new Tuple(m);
  }

  @Override
  public void close() throws IOException {
    if (consumer != null) {
      consumer.close();
    }
  }

  @Override
  public Tuple read() throws IOException {
    while(tupleQueue.isEmpty()) {
      ConsumerRecords<String, String> records = consumer.poll(100);
      for (ConsumerRecord<String, String> record : records) {
        tupleQueue.add(this.createTuple(record));
      }
    }
    return tupleQueue.pop();
  }

  @Override
  public StreamComparator getStreamSort() {
    return null;
  }

  @Override
  public StreamExpressionParameter toExpression(StreamFactory factory) throws IOException {
    return new StreamExpression(factory.getFunctionName(this.getClass()));
  }

  @Override
  public Explanation toExplanation(StreamFactory factory) throws IOException {
    return new StreamExplanation(getStreamNodeId().toString())
        .withFunctionName("kafkastreamingexpression")
        .withImplementingClass(this.getClass().getName())
        .withExpressionType(Explanation.ExpressionType.STREAM_SOURCE)
        .withExpression("--non-expressible--");
  }
}
