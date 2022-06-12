package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private Long id = 0L;

    @Autowired
    public UserService(@Qualifier("inMemoryUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private Long generateId() {
        return ++id;
    }

    public User addUser(User user) {
        user.setId(generateId());
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }
    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }
    public User deleteUser(User user) {
        return userStorage.deleteUser(user);
    }
    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }
    public void addFriend(Long id, Long friendId) {
        User user = userStorage.getUserById(id);
        User userFriend = userStorage.getUserById(friendId);
        user.getFriends().add(friendId);
        userFriend.getFriends().add(id);
        log.info("User id = {} added to friends user id={}", id, friendId);
    }

    public void deleteFriend(Long id, Long friendId) {
        User user = userStorage.getUserById(id);
        User userFriend = userStorage.getUserById(friendId);
        user.getFriends().remove(friendId);
        userFriend.getFriends().remove(id);
        log.info("User id = {} deleted from friends user id={}", id, friendId);
    }

    public Collection<User> findFriends(Long id) {
        Collection<User> friends = new ArrayList<>();
        for (Long friendId : userStorage.getUserById(id).getFriends()) {
            if (userStorage.getUserById(friendId) != null)
                friends.add(userStorage.getUserById(friendId));
        }
        return friends;
    }

    public Collection<User> findSharedFriends(Long id, Long otherId) {
        User user = userStorage.getUserById(id);
        User otherUser = userStorage.getUserById(otherId);
        List<Long> idUsers = userStorage.getUsers().stream().map(User::getId).collect(Collectors.toList());
        return user.getFriends().stream()
                .filter(x -> otherUser.getFriends().contains(x))
                .filter(idUsers::contains)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }
}
