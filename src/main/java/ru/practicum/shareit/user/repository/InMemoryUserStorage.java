package ru.practicum.shareit.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@RequiredArgsConstructor
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private Long id = 1L;

    @Override
    public User createUser(User user) {
        user.setId(id++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUser(Long userId) {
        return users.get(userId);
    }

    @Override
    public User updateUser(Long userId, User user) {
        User updatedUser = users.get(userId);
        if (updatedUser == null) {
            return null;
        }
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
        users.remove(userId);
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }
}
