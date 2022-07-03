package ru.yandex.practicum.filmorate.storage.event;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Repository
public class EventDbStorage implements EventStorage {
    private static final String GET_ALL_USER_EVENTS = "SELECT * FROM EVENTS WHERE USER_ID = ?";
    private final JdbcTemplate jdbcTemplate;

    public EventDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Event> getEventsByUserId(Long id) {
        return jdbcTemplate.query(GET_ALL_USER_EVENTS, new EventJdbcMapper(), id);
    }

    @Override
    public void addNewEvent(Event event) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("EVENTS")
                .usingGeneratedKeyColumns("event_id");
        simpleJdbcInsert.execute(event.toMap());
    }
}
