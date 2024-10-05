package rs.ac.kg.fin.albus.hagrid.event.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record KafkaConfig(@Value("${spring.kafka.producer.topic}") String producerTopic,
                          @Value("${spring.kafka.consumer.concurrency}") int numOfConsumers) {
}
