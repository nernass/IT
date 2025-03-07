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
import org.apache.kafka.streams.Topology;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WordCountDemoIntegrationTest {

    private static final String INPUT_TOPIC = "streams-plaintext-input";
    private static final String OUTPUT_TOPIC = "streams-wordcount-output";
    private static KafkaContainer kafka;
    private static KafkaStreams streams;
    private static KafkaProducer<String, String> producer;
    private static KafkaConsumer<String, Long> consumer;

    @BeforeAll
    static void setup() {
        // Start Kafka container
        kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
        kafka.start();

        // Configure and start Kafka Streams
        Properties streamProps = WordCountDemo.properties("test-streams-wordcount", kafka.getBootstrapServers());
        Topology topology = WordCountDemo.topology(INPUT_TOPIC, OUTPUT_TOPIC);
        streams = new KafkaStreams(topology, streamProps);
        streams.start();

        // Configure producer
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producer = new KafkaProducer<>(producerProps);

        // Configure consumer
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(OUTPUT_TOPIC));
    }

    @Test
    void testWordCountStreamProcessing() throws InterruptedException {
        // Send test input
        String[] messages = {
                "hello world",
                "hello kafka streams",
                "world of streaming"
        };

        for (String message : messages) {
            producer.send(new ProducerRecord<>(INPUT_TOPIC, message));
        }
        producer.flush();

        // Expected word counts
        Map<String, Long> expectedWordCounts = new HashMap<>();
        expectedWordCounts.put("hello", 2L);
        expectedWordCounts.put("world", 2L);
        expectedWordCounts.put("kafka", 1L);
        expectedWordCounts.put("streams", 1L);
        expectedWordCounts.put("of", 1L);
        expectedWordCounts.put("streaming", 1L);

        // Wait for processing and verify results
        Map<String, Long> actualWordCounts = new HashMap<>();
        int maxAttempts = 10;
        int attempt = 0;

        while (attempt < maxAttempts && actualWordCounts.size() < expectedWordCounts.size()) {
            ConsumerRecords<String, Long> records = consumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, Long> record : records) {
                actualWordCounts.put(record.key(), record.value());
            }
            attempt++;
        }

        assertEquals(expectedWordCounts, actualWordCounts, "Word counts should match expected values");
    }

    @AfterAll
    static void tearDown() {
        if (streams != null) {
            streams.close();
        }
        if (producer != null) {
            producer.close();
        }
        if (consumer != null) {
            consumer.close();
        }
        if (kafka != null) {
            kafka.stop();
        }
    }
}