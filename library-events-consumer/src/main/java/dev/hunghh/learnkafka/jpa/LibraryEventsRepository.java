package dev.hunghh.learnkafka.jpa;

import dev.hunghh.learnkafka.entity.LibraryEvent;
import org.springframework.data.repository.CrudRepository;

public interface LibraryEventsRepository extends CrudRepository<LibraryEvent, Integer> {
}
