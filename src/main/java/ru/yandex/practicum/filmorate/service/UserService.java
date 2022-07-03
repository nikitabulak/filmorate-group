package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.friends.FriendsStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FriendsStorage friendsStorage;
    private final EventStorage eventStorage;
    private Long id = 0L;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       FriendsStorage friendsStorage,
                       EventStorage eventStorage) {
        this.userStorage = userStorage;
        this.friendsStorage = friendsStorage;
        this.eventStorage = eventStorage;
    }

    private Long generateId() {
        return ++id;
    }

    public User addUser(User user) {
        return userStorage.add(user);
    }

    public User updateUser(User user) {
        return userStorage.update(user);
    }

    public Collection<User> getUsers() {
        System.out.println(userStorage.getAll());
        return userStorage.getAll();
    }

    public void deleteUserById(Long id){
        userStorage.deleteById(id);
    }

    public User getUserById(Long id) {
        return userStorage.getById(id).orElseThrow(() ->
                new UserNotFoundException(String.format("Request user with absent id = %d", id)));
    }

    public void addFriend(Long id, Long friendId) {
        friendsStorage.addFriend(id, friendId);
        eventStorage.addNewEvent(new Event.Builder()
                .setCurrentTimestamp()
                .setUserId(id)
                .setEventType(EventType.FRIEND)
                .setOperationType(OperationType.ADD)
                .setEntityId(friendId)
                .build());
        log.info("User id = {} added to friends user id={}", id, friendId);
    }

    public void deleteFriend(Long id, Long friendId) {
        friendsStorage.deleteFriend(id, friendId);
        eventStorage.addNewEvent(new Event.Builder()
                .setCurrentTimestamp()
                .setUserId(id)
                .setEventType(EventType.FRIEND)
                .setOperationType(OperationType.REMOVE)
                .setEntityId(friendId)
                .build());
        log.info("User id = {} deleted from friends user id={}", id, friendId);
    }

    public Collection<User> findFriends(Long id) {
        return friendsStorage.findFriends(id);
    }

    public Collection<User> findSharedFriends(Long id, Long otherId) {
        return findFriends(id).stream()
                .filter(x -> findFriends(otherId).contains(x))
                .collect(Collectors.toList());
    }

    public Collection<Event> getFeed(Long id) {
        return eventStorage.getEventsByUserId(id);
    }
}
