package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface EventStorage {
    List<Event> getEventsByUserId(Long id);

    void addNewEvent(Event event);

    Event mapRow(ResultSet rs, int rowNum) throws SQLException;
}
