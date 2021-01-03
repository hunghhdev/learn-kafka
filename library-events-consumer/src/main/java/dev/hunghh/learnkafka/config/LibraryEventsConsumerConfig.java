package dev.hunghh.learnkafka.config;

import dev.hunghh.learnkafka.service.LibraryEventsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class LibraryEventsConsumerConfig {

    @Resource
    private LibraryEventsService libraryEventsService;

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory
    ){
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setConcurrency(3);
        factory.setErrorHandler((throwException,data)->{
            log.info("Exception in consumerConfig is {} amd the record is {}", throwException.getMessage(), data);
        });
        factory.setRetryTemplate(retryTemplate());
        factory.setRecoveryCallback((context -> {
            if (context.getLastThrowable().getCause() instanceof RecoverableDataAccessException){
                // invoke recovery logic
                log.info("Inside the recoverable logic");
//                Arrays.asList(context.attributeNames())
//                        .forEach(attributeName -> {
//                            log.info("attribute name is: {}", attributeName);
//                            log.info("attribute value is: {}", context.getAttribute(attributeName));
//                        });
                ConsumerRecord<Integer, String> consumerRecord = (ConsumerRecord<Integer, String>) context.getAttribute("record");
                libraryEventsService.handleRecovery(consumerRecord);

            } else {
                log.info("Inside the non recoverable logic");
                throw new RuntimeException(context.getLastThrowable().getMessage());
            }
            return null;
        }));
        return factory;
    }

    private RetryTemplate retryTemplate() {
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(1000);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(simpleRetryPolicy());
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        return retryTemplate;
    }

    private RetryPolicy simpleRetryPolicy() {
//        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
//        simpleRetryPolicy.setMaxAttempts(3);
        Map<Class<? extends Throwable>, Boolean> ex = new HashMap<>();
        ex.put(IllegalArgumentException.class, false);
        ex.put(RecoverableDataAccessException.class, true);
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(3, ex, true);
        return simpleRetryPolicy;
    }
}
