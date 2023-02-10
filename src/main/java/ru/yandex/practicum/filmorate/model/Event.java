package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private Long eventId;
    private Long timestamp;
    private Long userId;
    private EventType eventType;
    private OperationType operation;
    private Long entityId;

    public Event(Long timestamp, Long userId, EventType eventType, OperationType operation, Long entityId) {
        this.timestamp = timestamp;
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("timestamp", timestamp);
        values.put("user_id", userId);
        values.put("event_type", eventType);
        values.put("operation", operation);
        values.put("entity_id", entityId);
        return values;
    }

    public static class Builder {
        private Long timestamp;
        private Long userId;
        private EventType eventType;
        private OperationType operation;
        private Long entityId;

        public Builder setCurrentTimestamp() {
            this.timestamp = System.currentTimeMillis();
            return this;
        }

        public Builder setUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setEventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder setOperationType(OperationType operation) {
            this.operation = operation;
            return this;
        }

        public Builder setEntityId(Long entityId) {
            this.entityId = entityId;
            return this;
        }

        public Event build() {
            return new Event(timestamp, userId, eventType, operation, entityId);
        }
    }


}
