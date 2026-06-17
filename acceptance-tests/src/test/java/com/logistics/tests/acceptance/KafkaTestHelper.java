package com.logistics.tests.acceptance;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public final class KafkaTestHelper {

    private KafkaTestHelper() {}

    // Poll a topic until a record with the given key appears or timeoutMs elapses.
    // Uses auto.offset.reset=earliest so records published before the consumer starts are visible.
    public static ConsumerRecord<String, String> pollUntilKey(String topic, String key, long timeoutMs) {
        Properties props = new Properties();
        props.put("bootstrap.servers", AcceptanceTestBase.KAFKA_SERVERS);
        props.put("group.id", "acceptance-test-" + UUID.randomUUID());
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("auto.offset.reset", "earliest");
        props.put("enable.auto.commit", "false");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of(topic));
            Instant deadline = Instant.now().plusMillis(timeoutMs);
            while (Instant.now().isBefore(deadline)) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    if (key.equals(record.key())) {
                        return record;
                    }
                }
            }
        }
        return null;
    }

    // Convenience overload with 5-second default timeout.
    public static ConsumerRecord<String, String> pollUntilKey(String topic, String key) {
        return pollUntilKey(topic, key, 5_000);
    }
}
