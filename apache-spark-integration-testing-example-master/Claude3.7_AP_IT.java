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
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnricherServiceIntegrationTest extends SparkIntegrationSuite {

    @Autowired
    private EnricherService enricherService;
    
    @Autowired
    private TestHiveUtils hiveUtils;
    
    @Autowired
    private TestAerospikeFacade aerospikeFacade;
    
    @Value("${aerospike.namespace}")
    private String namespace;
    
    @Value("${aerospike.enriched.data.set}")
    private String enrichedDataSet;

    @BeforeEach
    public void setUp() {
        // Clean both data sources before each test
        hiveUtils.cleanHive();
        aerospikeFacade.deleteAll(namespace);
    }

    @Test
    public void testEnrichmentWithSubscriberInfo() {
        // 1. Prepare test data in Hive
        String testSubscriberId = "test-subscriber-123";
        String testMsisdn = "79991112233";
        
        // Insert test data into Hive
        hiveUtils.insertInto(SubscriberInfo.subscriberInfo())
                .values(new SubscriberInfo.Values()
                        .setSubscriberId(testSubscriberId)
                        .setMsisdn(testMsisdn));
        
        // 2. Prepare test data in Aerospike (if needed)
        Key userKey = new Key(namespace, "users", testSubscriberId);
        aerospikeFacade.put(userKey, 
                new Bin("subscriber_id", testSubscriberId),
                new Bin("status", "active"));
        
        // 3. Execute enrichment process
        enricherService.proceedEnrichment();
        
        // 4. Verify the results
        List<com.aerospike.client.query.KeyRecord> enrichedRecords = aerospikeFacade.scanAll(namespace);
        
        // Verify that we have at least the record we're expecting
        assertTrue(enrichedRecords.size() >= 1, "At least one record should be in Aerospike");
        
        // Find our test record
        boolean foundEnrichedRecord = enrichedRecords.stream()
                .anyMatch(kr -> {
                    String subscriberId = kr.record.getString("subscriber_id");
                    String msisdn = kr.record.getString("msisdn");
                    String status = kr.record.getString("status");
                    
                    return testSubscriberId.equals(subscriberId) && 
                           testMsisdn.equals(msisdn) && 
                           "active".equals(status);
                });
                
        assertTrue(foundEnrichedRecord, "Enriched record with expected data should exist in Aerospike");
    }
    
    @Test
    public void testEnrichmentWithMultipleSubscribers() {
        // 1. Prepare multiple test records in Hive
        SubscriberInfo.Values[] subscribers = {
            new SubscriberInfo.Values().setSubscriberId("sub-1").setMsisdn("7999111001"),
            new SubscriberInfo.Values().setSubscriberId("sub-2").setMsisdn("7999111002"),
            new SubscriberInfo.Values().setSubscriberId("sub-3").setMsisdn("7999111003")
        };
        
        hiveUtils.insertInto(SubscriberInfo.subscriberInfo()).values(subscribers);
        
        // 2. Execute enrichment
        enricherService.proceedEnrichment();
        
        // 3. Verify results
        List<com.aerospike.client.query.KeyRecord> enrichedRecords = aerospikeFacade.scanAll(namespace);
        
        // Count how many of our test records were properly enriched
        long enrichedCount = enrichedRecords.stream()
                .filter(kr -> {
                    String subscriberId = kr.record.getString("subscriber_id");
                    String msisdn = kr.record.getString("msisdn");
                    
                    return subscriberId != null && subscriberId.startsWith("sub-") && 
                           msisdn != null && msisdn.startsWith("79991110");
                })
                .count();
                
        assertEquals(3, enrichedCount, "All three test subscribers should be enriched");
    }
    
    @Test
    public void testRepeatedEnrichmentDoesNotDuplicateData() {
        // 1. Insert test data
        String testSubscriberId = "repeat-test-123";
        String testMsisdn = "79991119999";
        
        hiveUtils.insertInto(SubscriberInfo.subscriberInfo())
                .values(new SubscriberInfo.Values()
                        .setSubscriberId(testSubscriberId)
                        .setMsisdn(testMsisdn));
        
        // 2. Run enrichment twice
        enricherService.proceedEnrichment();
        enricherService.proceedEnrichment();
        
        // 3. Verify we don't have duplicates
        List<com.aerospike.client.query.KeyRecord> enrichedRecords = aerospikeFacade.scanAll(namespace);
        
        long matchCount = enrichedRecords.stream()
                .filter(kr -> testSubscriberId.equals(kr.record.getString("subscriber_id")))
                .count();
                
        assertEquals(1, matchCount, "Should have exactly one record for the test subscriber after multiple enrichments");
    }
}