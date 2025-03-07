package com.mts.metric.spark.integration;

import com.mts.metric.spark.service.EnricherService;
import com.mts.metric.spark.testutils.facade.TestAerospikeFacade;
import com.mts.metric.spark.testutils.facade.TestHiveUtils;
import com.mts.metric.spark.testutils.facade.table.SubscriberInfo;
import com.mts.metric.spark.testutils.suite.SparkIntegrationSuite;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = SparkIntegrationSuite.class)
public class EnricherServiceIntegrationTest {

    @Autowired
    private EnricherService enricherService;

    @Autowired
    private TestHiveUtils testHiveUtils;

    @Autowired
    private TestAerospikeFacade testAerospikeFacade;

    @Autowired
    private SparkSession sparkSession;

    @BeforeEach
    public void setUp() {
        testHiveUtils.cleanHive();
        testAerospikeFacade.deleteAll("testNamespace");
    }

    @Test
    public void testEnrichmentProcess() {
        // Insert initial data into Hive
        SubscriberInfo subscriberInfo = testHiveUtils.insertInto(SubscriberInfo.subscriberInfo());
        subscriberInfo.values(
                new SubscriberInfo.Values().setSubscriberId("12345").setMsisdn("9876543210"));

        // Perform enrichment
        enricherService.proceedEnrichment();

        // Validate data in Aerospike
        var keyRecords = testAerospikeFacade.scanAll("testNamespace");
        assertEquals(1, keyRecords.size());
        var record = keyRecords.get(0).record;
        assertTrue(record.bins.containsKey("subscriberId"));
        assertTrue(record.bins.containsKey("msisdn"));
        assertEquals("12345", record.bins.get("subscriberId"));
        assertEquals("9876543210", record.bins.get("msisdn"));
    }
}