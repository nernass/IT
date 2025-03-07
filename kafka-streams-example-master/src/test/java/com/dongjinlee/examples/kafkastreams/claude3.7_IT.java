package com.dongjinlee.examples.kafkastreams;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.test.TestInputTopic;
import org.apache.kafka.streams.test.TestOutputTopic;
import org.apache.kafka.streams.test.TestRecord;
import org.apache.kafka.streams.test.TestTopology;
import org.apache.kafka.test.EmbeddedKafkaCluster;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WordCountDemoTest {

  private static final String INPUT_TOPIC = "streams-plaintext-input";
  private static final String OUTPUT_TOPIC = "streams-wordcount-output";
  private static final EmbeddedKafkaCluster CLUSTER = new EmbeddedKafkaCluster(1);

  private KafkaStreams streams;
  private KafkaProducer<String, String> producer;
  private KafkaConsumer<String, Long> consumer;

  @BeforeEach
  public void setup() {
    CLUSTER.start();

    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-wordcount");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    Topology topology = WordCountDemo.topology(INPUT_TOPIC, OUTPUT_TOPIC);
    streams = new KafkaStreams(topology, props);
    streams.start();

    Properties producerProps = new Properties();
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producer = new KafkaProducer<>(producerProps);

    Properties consumerProps = new Properties();
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Serdes.Long().deserializer().getClass().getName());
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumer = new KafkaConsumer<>(consumerProps);
    consumer.subscribe(Arrays.asList(OUTPUT_TOPIC));
  }

  @AfterEach
  public void tearDown() {
    streams.close();
    producer.close();
    consumer.close();
    CLUSTER.stop();
  }

  @Test
  public void testWordCount() throws InterruptedException {
    producer.send(new ProducerRecord<>(INPUT_TOPIC, "key", "hello world"));
    producer.send(new ProducerRecord<>(INPUT_TOPIC, "key", "hello kafka streams"));
    producer.flush();

    ConsumerRecord<String, Long> record = consumer.poll(Duration.ofSeconds(10)).iterator().next();
    assertEquals("hello", record.key());
    assertEquals(2L, record.value());

    record = consumer.poll(Duration.ofSeconds(10)).iterator().next();
    assertEquals("world", record.key());
    assertEquals(1L, record.value());

    record = consumer.poll(Duration.ofSeconds(10)).iterator().next();
    assertEquals("kafka", record.key());
    assertEquals(1L, record.value());

    record = consumer.poll(Duration.ofSeconds(10)).iterator().next();
    assertEquals("streams", record.key());
    assertEquals(1L, record.value());
  }
}