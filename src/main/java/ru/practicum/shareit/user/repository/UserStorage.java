package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

public interface UserStorage {

    User createUser(User userDto);

    User getUser(Long userId);

    User updateUser(Long userId, User userDto);

    void deleteUser(Long userId);
}
