package ru.practicum.shareit.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exeption.EmailAlreadyExistsException;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long id = 1L;

    @Override
    public User createUser(User user) {
        if (users.values().stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
        }

        user.setId(id++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUser(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        return users.get(userId);
    }

    @Override
    public User updateUser(Long userId, User user) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }

        if (user.getEmail() != null
                && users.values().stream()
                .filter(u -> !u.getId().equals(userId))
                .anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
        }

        User updatedUser = users.get(userId);
        if (user.getName() != null) {
            updatedUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            updatedUser.setEmail(user.getEmail());
        }
        users.put(userId, updatedUser);
        return updatedUser;
    }

    @Override
    public void deleteUser(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        users.remove(userId);
    }
}
