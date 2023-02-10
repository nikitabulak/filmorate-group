package ru.yandex.practicum.filmorate.storage.friends;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface FriendsStorage {
    void addFriend(Long id, Long friendId);

    Collection<User> findFriends(Long id);

    void deleteFriend(Long id, Long friendId);
}
