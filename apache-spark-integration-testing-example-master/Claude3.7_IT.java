package com.mts.metric.spark.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.query.KeyRecord;
import com.mts.metric.spark.testutils.facade.TestAerospikeFacade;
import com.mts.metric.spark.testutils.facade.TestHiveUtils;
import com.mts.metric.spark.testutils.facade.table.SubscriberInfo;
import com.mts.metric.spark.testutils.suite.SparkIntegrationSuite;

class EnricherServiceIntegrationTest extends SparkIntegrationSuite {

    @Autowired
    private EnricherService enricherService;

    @Autowired
    private TestHiveUtils testHiveUtils;

    @Autowired
    private TestAerospikeFacade aerospikeFacade;
    
    @Value("${aerospike.namespace}")
    private String aerospikeNamespace;
    
    @Value("${aerospike.set}")
    private String aerospikeSet;

    @BeforeEach
    void setUp() {
        // Clean Hive tables
        testHiveUtils.cleanHive();
        
        // Clean Aerospike namespace
        aerospikeFacade.deleteAll(aerospikeNamespace);
        
        // Prepare test data in Hive
        testHiveUtils.insertInto(SubscriberInfo.subscriberInfo())
            .values(
                new SubscriberInfo.Values()
                    .setSubscriberId("1001")
                    .setMsisdn("79001112233"),
                new SubscriberInfo.Values()
                    .setSubscriberId("1002")
                    .setMsisdn("79004445566")
            );
        
        // Prepare test data in Aerospike
        Key key1 = new Key(aerospikeNamespace, aerospikeSet, "user-1001");
        aerospikeFacade.put(key1, 
            new Bin("subscriberId", "1001"),
            new Bin("status", "active"),
            new Bin("plan", "premium")
        );
        
        Key key2 = new Key(aerospikeNamespace, aerospikeSet, "user-1002");
        aerospikeFacade.put(key2, 
            new Bin("subscriberId", "1002"),
            new Bin("status", "inactive"),
            new Bin("plan", "basic")
        );
    }

    @Test
    void shouldSuccessfullyEnrichData() {
        // When
        assertDoesNotThrow(() -> enricherService.proceedEnrichment());
        
        // Then
        List<KeyRecord> records = aerospikeFacade.scanAll(aerospikeNamespace);
        
        // Verify records were properly enriched
        assertThat(records).hasSize(2);
        
        // Find specific records and verify their content
        records.stream()
            .filter(kr -> kr.key.userKey.toString().equals("user-1001"))
            .findFirst()
            .ifPresent(record -> {
                assertThat(record.record.getString("subscriberId")).isEqualTo("1001");
                assertThat(record.record.getString("msisdn")).isEqualTo("79001112233");
                assertThat(record.record.getString("status")).isEqualTo("active");
                assertThat(record.record.getString("plan")).isEqualTo("premium");
            });
        
        records.stream()
            .filter(kr -> kr.key.userKey.toString().equals("user-1002"))
            .findFirst()
            .ifPresent(record -> {
                assertThat(record.record.getString("subscriberId")).isEqualTo("1002");
                assertThat(record.record.getString("msisdn")).isEqualTo("79004445566");
                assertThat(record.record.getString("status")).isEqualTo("inactive");
                assertThat(record.record.getString("plan")).isEqualTo("basic");
            });
    }
    
    @Test
    void shouldHandleEmptyData() {
        // Clean all data
        testHiveUtils.cleanHive();
        aerospikeFacade.deleteAll(aerospikeNamespace);
        
        // When
        assertDoesNotThrow(() -> enricherService.proceedEnrichment());
        
        // Then
        List<KeyRecord> records = aerospikeFacade.scanAll(aerospikeNamespace);
        assertThat(records).isEmpty();
    }
    
    @Test
    void shouldHandlePartialData() {
        // Prepare only Hive data without matching Aerospike records
        testHiveUtils.cleanHive();
        aerospikeFacade.deleteAll(aerospikeNamespace);
        
        testHiveUtils.insertInto(SubscriberInfo.subscriberInfo())
            .values(
                new SubscriberInfo.Values()
                    .setSubscriberId("2001")
                    .setMsisdn("79007778899")
            );
        
        // When
        assertDoesNotThrow(() -> enricherService.proceedEnrichment());
        
        // Then - verify how the service handles this case
        // (This assertion depends on the actual implementation of EnricherService)
        List<KeyRecord> records = aerospikeFacade.scanAll(aerospikeNamespace);
        // Check records based on expected behavior
    }
}