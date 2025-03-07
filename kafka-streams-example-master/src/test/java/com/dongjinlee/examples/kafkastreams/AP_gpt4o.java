import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class WordCountDemoIntegrationTest {

    private TopologyTestDriver testDriver;
    private ConsumerRecordFactory<String, String> recordFactory;

    @BeforeEach
    public void setup() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        testDriver = new TopologyTestDriver(WordCountDemo.topology("input-topic", "output-topic"), props);
        recordFactory = new ConsumerRecordFactory<>(Serdes.String().serializer(), Serdes.String().serializer());
    }

    @AfterEach
    public void tearDown() {
        testDriver.close();
    }

    @Test
    public void testWordCountSuccess() {
        testDriver.pipeInput(recordFactory.create("input-topic", null, "hello world"));
        OutputVerifier.compareKeyValue(
                testDriver.readOutput("output-topic", Serdes.String().deserializer(), Serdes.Long().deserializer()),
                "hello", 1L);
        OutputVerifier.compareKeyValue(
                testDriver.readOutput("output-topic", Serdes.String().deserializer(), Serdes.Long().deserializer()),
                "world", 1L);
    }

    @Test
    public void testWordCountWithEmptyInput() {
        testDriver.pipeInput(recordFactory.create("input-topic", null, ""));
        assertThrows(NullPointerException.class, () -> testDriver.readOutput("output-topic",
                Serdes.String().deserializer(), Serdes.Long().deserializer()));
    }

    @Test
    public void testWordCountWithInvalidInput() {
        testDriver.pipeInput(recordFactory.create("input-topic", null, null));
        assertThrows(NullPointerException.class, () -> testDriver.readOutput("output-topic",
                Serdes.String().deserializer(), Serdes.Long().deserializer()));
    }
}