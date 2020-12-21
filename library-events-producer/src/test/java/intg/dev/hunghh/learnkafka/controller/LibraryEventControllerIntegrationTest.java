package dev.hunghh.learnkafka.controller;

import dev.hunghh.learnkafka.domain.Book;
import dev.hunghh.learnkafka.domain.LibraryEvent;
import dev.hunghh.learnkafka.domain.LibraryEventType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
//@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
//            "spring.kafka.admin.properties.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@TestPropertySource(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventControllerIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void postLibraryEvent() {
        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("hunghh.dev")
                .bookName("Kafka using spring boot")
                .build();

        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .book(book)
                .libraryEventType(LibraryEventType.NEW)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, headers);

        ResponseEntity<LibraryEvent> responseEntity = restTemplate.exchange("/v1/library-event", HttpMethod.POST, request, LibraryEvent.class);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }
}