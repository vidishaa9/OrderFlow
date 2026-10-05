package com.vidisha.OrderFlow.config;

import com.vidisha.OrderFlow.event.InventoryConfirmedEvent;
import com.vidisha.OrderFlow.event.InventoryRejectedEvent;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    // ==================== KAFKA SERVER ====================

    @Value("${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}")
    private String bootstrapServers;


    // ==================== TOPICS ====================

    @Bean
    public NewTopic orderPlacedTopic() {
        return new NewTopic(
                "order-placed",
                1,
                (short) 1
        );
    }

    @Bean
    public NewTopic inventoryConfirmedTopic() {
        return new NewTopic(
                "inventory-confirmed",
                1,
                (short) 1
        );
    }

    @Bean
    public NewTopic inventoryRejectedTopic() {
        return new NewTopic(
                "inventory-rejected",
                1,
                (short) 1
        );
    }


    // ==================== PRODUCER ====================

    @Bean
    public ProducerFactory<String, Object> producerFactory() {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        properties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        properties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(properties);
    }


    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> producerFactory) {

        return new KafkaTemplate<>(producerFactory);
    }


    // ==================== CONFIRMED CONSUMER ====================

    @Bean
    public ConsumerFactory<String, InventoryConfirmedEvent>
    confirmedConsumerFactory() {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "order-service-confirmed-group"
        );

        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        properties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class
        );

        JsonDeserializer<InventoryConfirmedEvent> deserializer =
                new JsonDeserializer<>(
                        InventoryConfirmedEvent.class,
                        false
                );

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                deserializer
        );
    }


    @Bean(name = "confirmedKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<
            String, InventoryConfirmedEvent>
    confirmedKafkaListenerContainerFactory(
            ConsumerFactory<String, InventoryConfirmedEvent>
                    confirmedConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<
                String, InventoryConfirmedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(confirmedConsumerFactory);

        return factory;
    }


    // ==================== REJECTED CONSUMER ====================

    @Bean
    public ConsumerFactory<String, InventoryRejectedEvent>
    rejectedConsumerFactory() {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "order-service-rejected-group"
        );

        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        properties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class
        );

        JsonDeserializer<InventoryRejectedEvent> deserializer =
                new JsonDeserializer<>(
                        InventoryRejectedEvent.class,
                        false
                );

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                deserializer
        );
    }


    @Bean(name = "rejectedKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<
            String, InventoryRejectedEvent>
    rejectedKafkaListenerContainerFactory(
            ConsumerFactory<String, InventoryRejectedEvent>
                    rejectedConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<
                String, InventoryRejectedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(rejectedConsumerFactory);

        return factory;
    }
}