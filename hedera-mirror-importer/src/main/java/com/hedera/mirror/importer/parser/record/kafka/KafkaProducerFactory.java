package com.hedera.mirror.importer.parser.record.kafka;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@RequiredArgsConstructor
public class KafkaProducerFactory {
    private final KafkaProperties properties;

    @Bean
    KafkaTemplate<String, byte[]> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    ProducerFactory<String, byte[]> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    ImmutableMap<String, Object> producerConfigs() {
        ImmutableMap.Builder<String, Object> propertyBuilder = ImmutableMap.<String, Object>builder()
                .put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers())
                .put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class)
                .put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class)
                .put(ProducerConfig.ACKS_CONFIG, "all") // may need set to "1" if throughput becomes an issue
                .put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 2097164);
        String apiKey = properties.getProducerApiKey();
        if (apiKey != null && !apiKey.isBlank())
        {
            propertyBuilder.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name)
                .put(SaslConfigs.SASL_MECHANISM, "PLAIN")
                .put(SaslConfigs.SASL_JAAS_CONFIG, getSaslJaasConfig());
        }
        return propertyBuilder.build();
    }

    private String getSaslJaasConfig() {
        return String.format(
                "org.apache.kafka.common.security.plain.PlainLoginModule required username='%s' password='%s';",
                properties.getProducerApiKey(), properties.getProducerApiKeySecret());
    }
}
