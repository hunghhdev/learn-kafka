package dev.hunghh.learnkafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.hunghh.learnkafka.service.LibraryEventsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class LibraryEventsConsumer {

    @Resource
    private LibraryEventsService libraryEventsService;

    @KafkaListener(topics = {"library-events"})
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        log.info("ConsumerRecord: {}", consumerRecord);
        libraryEventsService.processLibraryEvent(consumerRecord);
    }
}
