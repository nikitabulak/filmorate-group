package ru.yandex.practicum.filmorate.storage.event;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EventDbStorage implements EventStorage, RowMapper<Event> {
    private static final String GET_ALL_USER_EVENTS = "SELECT * FROM EVENTS WHERE USER_ID = ?";
    private final JdbcTemplate jdbcTemplate;

    public EventDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Event> getEventsByUserId(Long id) {
        return jdbcTemplate.query(GET_ALL_USER_EVENTS, this, id);
    }

    @Override
    public void addNewEvent(Event event) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("EVENTS")
                .usingGeneratedKeyColumns("event_id");
        simpleJdbcInsert.execute(event.toMap());
    }

    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Event(
                rs.getLong("event_id"),
                rs.getLong("timestamp"),
                rs.getLong("user_id"),
                EventType.valueOf(rs.getString("event_type")),
                OperationType.valueOf(rs.getString("operation")),
                rs.getLong("entity_id")
        );
    }
}
