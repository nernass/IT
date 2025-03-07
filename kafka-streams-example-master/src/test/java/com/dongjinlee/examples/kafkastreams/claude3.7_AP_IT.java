package com.dongjinlee.examples.kafkastreams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class WordCountDemoIntegrationTest {

    private static final String INPUT_TOPIC = "streams-plaintext-input";
    private static final String OUTPUT_TOPIC = "streams-wordcount-output";
    private static final String APP_ID = "wordcount-integration-test";

    @Container
    private final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"));

    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, Long> consumer;
    private KafkaStreams kafkaStreams;

    @BeforeEach
    public void setup() {
        // Configure and create producer
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(producerProps);

        // Configure and create consumer
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(OUTPUT_TOPIC));

        // Configure and start the Kafka Streams application
        Properties streamProps = WordCountDemo.properties(APP_ID, kafka.getBootstrapServers());
        Topology topology = WordCountDemo.topology(INPUT_TOPIC, OUTPUT_TOPIC);
        kafkaStreams = new KafkaStreams(topology, streamProps);
        kafkaStreams.start();
    }

    @AfterEach
    public void tearDown() {
        if (producer != null) {
            producer.close();
        }
        if (consumer != null) {
            consumer.close();
        }
        if (kafkaStreams != null) {
            kafkaStreams.close();
            kafkaStreams.cleanUp();
        }
    }

    @Test
    public void testWordCountProcessing() throws ExecutionException, InterruptedException, TimeoutException {
        // Send test data to input topic
        producer.send(new ProducerRecord<>(INPUT_TOPIC, "hello world")).get(10, TimeUnit.SECONDS);
        producer.send(new ProducerRecord<>(INPUT_TOPIC, "hello kafka streams")).get(10, TimeUnit.SECONDS);
        producer.send(new ProducerRecord<>(INPUT_TOPIC, "world of streams")).get(10, TimeUnit.SECONDS);
        producer.flush();

        // Expected word counts
        Map<String, Long> expectedWordCounts = new HashMap<>();
        expectedWordCounts.put("hello", 2L);
        expectedWordCounts.put("world", 2L);
        expectedWordCounts.put("kafka", 1L);
        expectedWordCounts.put("streams", 2L);
        expectedWordCounts.put("of", 1L);

        // Poll the output topic and verify results
        Map<String, Long> actualWordCounts = new HashMap<>();
        long endTime = System.currentTimeMillis() + 30000; // 30 seconds timeout

        while (System.currentTimeMillis() < endTime && actualWordCounts.size() < expectedWordCounts.size()) {
            ConsumerRecords<String, Long> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, Long> record : records) {
                actualWordCounts.put(record.key(), record.value());
            }
        }

        // Verify results
        assertEquals(expectedWordCounts.size(), actualWordCounts.size(),
                "Expected to receive counts for all words");

        for (Map.Entry<String, Long> entry : expectedWordCounts.entrySet()) {
            String word = entry.getKey();
            Long expectedCount = entry.getValue();
            assertTrue(actualWordCounts.containsKey(word),
                    "Expected to receive count for word: " + word);
            assertEquals(expectedCount, actualWordCounts.get(word),
                    "Word count for '" + word + "' does not match expected value");
        }
    }

    @Test
    public void testEmptyInput() throws ExecutionException, InterruptedException, TimeoutException {
        // Send an empty string
        producer.send(new ProducerRecord<>(INPUT_TOPIC, "")).get(10, TimeUnit.SECONDS);
        producer.flush();

        // No words should be counted
        ConsumerRecords<String, Long> records = consumer.poll(Duration.ofMillis(5000));
        int recordCount = records.count();
        assertEquals(0, recordCount, "No words should be counted from empty input");
    }

    @Test
    public void testSpecialCharactersAndPunctuation()
            throws ExecutionException, InterruptedException, TimeoutException {
        // Send text with special characters and punctuation
        producer.send(new ProducerRecord<>(INPUT_TOPIC, "hello, world! kafka-streams...")).get(10, TimeUnit.SECONDS);
        producer.flush();

        // Expected parsed words (note: punctuation is treated as word separators)
        Map<String, Long> expectedWordCounts = new HashMap<>();
        expectedWordCounts.put("hello", 1L);
        expectedWordCounts.put("world", 1L);
        expectedWordCounts.put("kafka-streams", 1L);

        // Poll and verify
        Map<String, Long> actualWordCounts = new HashMap<>();
        long endTime = System.currentTimeMillis() + 30000;

        while (System.currentTimeMillis() < endTime && actualWordCounts.size() < expectedWordCounts.size()) {
            ConsumerRecords<String, Long> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, Long> record : records) {
                actualWordCounts.put(record.key(), record.value());
            }
        }

        // Verify the expected words are counted correctly
        for (Map.Entry<String, Long> entry : expectedWordCounts.entrySet()) {
            String word = entry.getKey();
            Long expectedCount = entry.getValue();
            assertTrue(actualWordCounts.containsKey(word),
                    "Expected to receive count for word: " + word);
            assertEquals(expectedCount, actualWordCounts.get(word),
                    "Word count for '" + word + "' does not match expected value");
        }
    }
}