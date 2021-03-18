//package com.megaease.easeagent.sniffer.kafka.v2d3;
//
//import org.apache.kafka.clients.consumer.OffsetAndMetadata;
//import org.apache.kafka.clients.producer.Callback;
//import org.apache.kafka.clients.producer.Producer;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.apache.kafka.clients.producer.RecordMetadata;
//import org.apache.kafka.common.Metric;
//import org.apache.kafka.common.MetricName;
//import org.apache.kafka.common.PartitionInfo;
//import org.apache.kafka.common.TopicPartition;
//import org.apache.kafka.common.errors.ProducerFencedException;
//
//import java.time.Duration;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.Future;
//import java.util.concurrent.TimeUnit;
//
//public class AgentKafkaProducer<K, V> implements Producer<K, V> {
//
//    @Override
//    public void initTransactions() {
//
//    }
//
//    @Override
//    public void beginTransaction() throws ProducerFencedException {
//
//    }
//
//    @Override
//    public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets, String consumerGroupId) throws ProducerFencedException {
//
//    }
//
//    @Override
//    public void commitTransaction() throws ProducerFencedException {
//
//    }
//
//    @Override
//    public void abortTransaction() throws ProducerFencedException {
//
//    }
//
//    @Override
//    public Future<RecordMetadata> send(ProducerRecord<K, V> record) {
//        return null;
//    }
//
//    @Override
//    public Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback) {
//        return null;
//    }
//
//    @Override
//    public void flush() {
//
//    }
//
//    @Override
//    public List<PartitionInfo> partitionsFor(String topic) {
//        return null;
//    }
//
//    @Override
//    public Map<MetricName, ? extends Metric> metrics() {
//        return null;
//    }
//
//    @Override
//    public void close() {
//
//    }
//
//    @Override
//    public void close(long timeout, TimeUnit unit) {
//
//    }
//
//    @Override
//    public void close(Duration timeout) {
//
//    }
//}
