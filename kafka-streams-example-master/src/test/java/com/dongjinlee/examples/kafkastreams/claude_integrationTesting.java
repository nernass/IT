package com.dongjinlee.examples.kafkastreams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class WordCountDemoIT {

    private static final String INPUT_TOPIC = "streams-plaintext-input";
    private static final String OUTPUT_TOPIC = "streams-wordcount-output";
    private KafkaStreams streams;
    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, Long> consumer;

    @Container
    private static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @BeforeEach
    void setUp() {
        // Configure and start streams
        Properties streamProps = WordCountDemo.properties(
                "streams-wordcount-test",
                kafka.getBootstrapServers());
        streams = new KafkaStreams(
                WordCountDemo.topology(INPUT_TOPIC, OUTPUT_TOPIC),
                streamProps);
        streams.start();

        // Configure producer
        Map<String, Object> producerConfig = new HashMap<>();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producer = new KafkaProducer<>(producerConfig);

        // Configure consumer
        Map<String, Object> consumerConfig = new HashMap<>();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        consumer = new KafkaConsumer<>(consumerConfig);
        consumer.subscribe(Collections.singletonList(OUTPUT_TOPIC));
    }

    @AfterEach
    void tearDown() {
        if (streams != null)
            streams.close();
        if (producer != null)
            producer.close();
        if (consumer != null)
            consumer.close();
    }

    @Test
    void shouldCountWords() throws InterruptedException {
        // Given
        String input = "hello world hello";
        producer.send(new ProducerRecord<>(INPUT_TOPIC, null, input));
        producer.flush();

        // When
        Thread.sleep(10000); // Wait for processing

        // Then
        ConsumerRecords<String, Long> records = consumer.poll(Duration.ofSeconds(10));
        Map<String, Long> wordCounts = new HashMap<>();

        for (ConsumerRecord<String, Long> record : records) {
            wordCounts.put(record.key(), record.value());
        }

        assertEquals(2L, wordCounts.get("hello"));
        assertEquals(1L, wordCounts.get("world"));
    }

    @Test
    void shouldHandleEmptyInput() throws InterruptedException {
        // Given
        String input = "";
        producer.send(new ProducerRecord<>(INPUT_TOPIC, null, input));
        producer.flush();

        // When
        Thread.sleep(5000); // Wait for processing

        // Then
        ConsumerRecords<String, Long> records = consumer.poll(Duration.ofSeconds(5));
        assertEquals(0, records.count());
    }
}