package com.mts.metric.spark.service;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.mts.metric.spark.testutils.facade.TestAerospikeFacade;
import com.mts.metric.spark.testutils.facade.TestHiveUtils;
import com.mts.metric.spark.testutils.facade.table.SubscriberInfo;
import com.mts.metric.spark.testutils.suite.SparkIntegrationSuite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.mts.metric.spark.testutils.facade.table.SubscriberInfo.subscriberInfo;
import static org.junit.jupiter.api.Assertions.*;

class EnricherServiceIntegrationTest extends SparkIntegrationSuite {

    private static final String TEST_NAMESPACE = "test";
    private static final String TEST_SUBSCRIBER_ID = "12345";
    private static final String TEST_MSISDN = "79001234567";

    @Autowired
    private EnricherService enricherService;

    @Autowired
    private TestHiveUtils hiveUtils;

    @Autowired
    private TestAerospikeFacade aerospikeFacade;

    @BeforeEach
    void setUp() {
        hiveUtils.cleanHive();
        aerospikeFacade.deleteAll(TEST_NAMESPACE);
    }

    @Test
    void testEnrichmentFlow() {
        // Setup test data in Hive
        SubscriberInfo.Values testValues = new SubscriberInfo.Values()
                .setSubscriberId(TEST_SUBSCRIBER_ID)
                .setMsisdn(TEST_MSISDN);

        hiveUtils.insertInto(subscriberInfo())
                .values(testValues);

        // Setup test data in Aerospike
        Key testKey = new Key(TEST_NAMESPACE, "subscribers", TEST_SUBSCRIBER_ID);
        Bin testBin = new Bin("msisdn", TEST_MSISDN);
        aerospikeFacade.put(testKey, testBin);

        // Execute enrichment
        enricherService.proceedEnrichment();

        // Verify results
        var records = aerospikeFacade.scanAll(TEST_NAMESPACE);
        assertFalse(records.isEmpty(), "No records found in Aerospike after enrichment");

        var record = records.stream()
                .filter(kr -> kr.key.userKey.toString().equals(TEST_SUBSCRIBER_ID))
                .findFirst()
                .orElseThrow();

        assertEquals(TEST_MSISDN, record.record.getString("msisdn"));
    }

    @Test
    void testEnrichmentWithEmptyData() {
        // Execute enrichment with no data
        enricherService.proceedEnrichment();

        // Verify no records were created
        var records = aerospikeFacade.scanAll(TEST_NAMESPACE);
        assertTrue(records.isEmpty(), "Records found in Aerospike when none expected");
    }

    @Test
    void testEnrichmentWithMultipleRecords() {
        // Setup multiple test records
        String[][] testData = {
                { "1111", "79001111111" },
                { "2222", "79002222222" },
                { "3333", "79003333333" }
        };

        for (String[] data : testData) {
            SubscriberInfo.Values values = new SubscriberInfo.Values()
                    .setSubscriberId(data[0])
                    .setMsisdn(data[1]);

            hiveUtils.insertInto(subscriberInfo())
                    .values(values);

            Key key = new Key(TEST_NAMESPACE, "subscribers", data[0]);
            Bin bin = new Bin("msisdn", data[1]);
            aerospikeFacade.put(key, bin);
        }

        // Execute enrichment
        enricherService.proceedEnrichment();

        // Verify results
        var records = aerospikeFacade.scanAll(TEST_NAMESPACE);
        assertEquals(testData.length, records.size(),
                "Incorrect number of records after enrichment");
    }
}