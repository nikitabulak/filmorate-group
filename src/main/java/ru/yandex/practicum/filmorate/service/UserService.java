package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friends.FriendsStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FriendsStorage friendsStorage;
    private Long id = 0L;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, FriendsStorage friendsStorage) {
        this.userStorage = userStorage;
        this.friendsStorage = friendsStorage;
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
    public User deleteUser(User user) {
        return userStorage.delete(user);
    }
    public User getUserById(Long id) {
        return userStorage.getById(id).orElseThrow(() ->
                new UserNotFoundException(String.format("Request user with absent id = %d", id)));
    }
    public void addFriend(Long id, Long friendId) {
        friendsStorage.addFriend(id, friendId);
        log.info("User id = {} added to friends user id={}", id, friendId);
    }

    public void deleteFriend(Long id, Long friendId) {
        friendsStorage.deleteFriend(id, friendId);
        log.info("User id = {} deleted from friends user id={}", id, friendId);
    }

    public Collection<User> findFriends(Long id) {
        return friendsStorage.findFriends(id);
    }

    public Collection<User> findSharedFriends(Long id, Long otherId) {
        return findFriends(id).stream()
                .filter(x -> findFriends(otherId).contains(x))
                .collect(Collectors.toList());

        /*User user = userStorage.getUserById(id);
        User otherUser = userStorage.getUserById(otherId);
        List<Long> idUsers = userStorage.getUsers().stream().map(User::getId).collect(Collectors.toList());
        return user.getFriends().stream()
                .filter(x -> otherUser.getFriends().contains(x))
                .filter(idUsers::contains)
                .map(userStorage::getUserById)
                .collect(Collectors.toList()); */
    }
}
