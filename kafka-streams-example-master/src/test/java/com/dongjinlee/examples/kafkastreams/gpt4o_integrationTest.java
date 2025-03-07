package com.dongjinlee.examples.kafkastreams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.apache.kafka.streams.test.TestRecord;
import org.apache.kafka.streams.test.TestInputTopic;
import org.apache.kafka.streams.test.TestOutputTopic;
import org.junit.jupiter.api.*;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class WordCountDemoTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, Long> outputTopic;

    @BeforeEach
    public void setup() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Topology topology = WordCountDemo.topology("input-topic", "output-topic");
        testDriver = new TopologyTestDriver(topology, props);

        inputTopic = testDriver.createInputTopic("input-topic", Serdes.String().serializer(),
                Serdes.String().serializer());
        outputTopic = testDriver.createOutputTopic("output-topic", Serdes.String().deserializer(),
                Serdes.Long().deserializer());
    }

    @AfterEach
    public void tearDown() {
        testDriver.close();
    }

    @Test
    public void testWordCount() {
        inputTopic.pipeInput("key1", "Hello Kafka Streams");
        inputTopic.pipeInput("key2", "Hello Kafka");

        OutputVerifier.compareKeyValue(outputTopic.readKeyValue(), "hello", 1L);
        OutputVerifier.compareKeyValue(outputTopic.readKeyValue(), "kafka", 1L);
        OutputVerifier.compareKeyValue(outputTopic.readKeyValue(), "streams", 1L);
        OutputVerifier.compareKeyValue(outputTopic.readKeyValue(), "hello", 2L);
        OutputVerifier.compareKeyValue(outputTopic.readKeyValue(), "kafka", 2L);
        assertTrue(outputTopic.isEmpty());
    }

    @Test
    public void testEmptyInput() {
        inputTopic.pipeInput("key1", "");

        assertTrue(outputTopic.isEmpty());
    }

    @Test
    public void testErrorHandling() {
        // Add any specific error handling tests if applicable
    }
}