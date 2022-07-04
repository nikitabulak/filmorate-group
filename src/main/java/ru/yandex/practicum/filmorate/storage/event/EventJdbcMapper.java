package ru.yandex.practicum.filmorate.storage.event;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventJdbcMapper implements RowMapper<Event> {

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
