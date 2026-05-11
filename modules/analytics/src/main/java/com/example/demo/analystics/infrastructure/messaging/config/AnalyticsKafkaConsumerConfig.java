package com.example.demo.analystics.infrastructure.messaging.config;

import com.example.demo.analystics.application.usecase.restore.PremiumDetailRestoreService;
import com.example.demo.analystics.application.usecase.restore.PremiumRestoreService;
import com.example.demo.analystics.application.usecase.restore.TickRestoreService;
import com.example.demo.analystics.application.usecase.revoke.PremiumDetailRevokeService;
import com.example.demo.analystics.application.usecase.revoke.PremiumRevokeService;
import com.example.demo.analystics.application.usecase.revoke.TickRevokeService;
import com.example.demo.analystics.infrastructure.messaging.balancer.AnalyticsPartitionLifecycleListener;
import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import com.example.demo.contracts.message.price_value.PremiumMessage;
import com.example.demo.contracts.message.price_value.TickMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AnalyticsKafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> commonConsumerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "statistics-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return config;
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factoryFor(
            Class<T> clazz,
            ConsumerAwareRebalanceListener rebalanceListener) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        JsonDeserializer<T> valueDeserializer = new JsonDeserializer<>(clazz, false);
        valueDeserializer.addTrustedPackages(
                "com.example.demo.contracts.message"
        );

        factory.setConsumerFactory(
                new DefaultKafkaConsumerFactory<>(
                        commonConsumerConfig(),
                        new StringDeserializer(),
                        valueDeserializer
                )
        );
        factory.getContainerProperties().setConsumerRebalanceListener(rebalanceListener);
        return factory;
    }

    @Bean
    public AnalyticsPartitionLifecycleListener tickAnalyticsPartitionLifecycleListener(
            TickRestoreService restoreService,
            TickRevokeService revokeService) {
        return new AnalyticsPartitionLifecycleListener("tick", restoreService, revokeService);
    }

    @Bean
    public AnalyticsPartitionLifecycleListener premiumAnalyticsPartitionLifecycleListener(
            PremiumRestoreService restoreService,
            PremiumRevokeService revokeService) {
        return new AnalyticsPartitionLifecycleListener("premium", restoreService, revokeService);
    }

    @Bean
    public AnalyticsPartitionLifecycleListener premiumDetailAnalyticsPartitionLifecycleListener(
            PremiumDetailRestoreService restoreService,
            PremiumDetailRevokeService revokeService) {
        return new AnalyticsPartitionLifecycleListener("premium-detail", restoreService, revokeService);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TickMessage> tickKafkaListenerContainerFactory(
            @Qualifier("tickAnalyticsPartitionLifecycleListener")
            AnalyticsPartitionLifecycleListener lifecycleListener) {
        return factoryFor(TickMessage.class, lifecycleListener);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PremiumMessage> premiumKafkaListenerContainerFactory(
            @Qualifier("premiumAnalyticsPartitionLifecycleListener")
            AnalyticsPartitionLifecycleListener lifecycleListener) {
        return factoryFor(PremiumMessage.class, lifecycleListener);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PremiumDetailMessage> premiumDetailKafkaListenerContainerFactory(
            @Qualifier("premiumDetailAnalyticsPartitionLifecycleListener")
            AnalyticsPartitionLifecycleListener lifecycleListener) {
        return factoryFor(PremiumDetailMessage.class, lifecycleListener);
    }
}
