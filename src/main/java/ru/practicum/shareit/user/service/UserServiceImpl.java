package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exeption.EmailAlreadyExistsException;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public UserDto createUser(UserDto userDto) {
        String email = userDto.getEmail();
        boolean emailExists = userStorage.findAll()
                .stream()
                .anyMatch(u -> u.getEmail().equals(email));
        if (emailExists) {
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
        }

        User user = UserMapper.toUser(userDto);
        User createdUser = userStorage.createUser(user);
        return UserMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto getUser(Long userId) {
        User user = userStorage.getUser(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User storedUser = userStorage.getUser(userId);
        if (storedUser == null) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }

        String newEmail = userDto.getEmail();
        if (newEmail != null) {
            boolean emailExists = userStorage.findAll()
                    .stream()
                    .filter(u -> !u.getId().equals(userId))
                    .anyMatch(u -> u.getEmail().equals(newEmail));
            if (emailExists) {
                throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
            }
        }

        User user = UserMapper.toUser(userDto);
        User updatedUser = userStorage.updateUser(userId, user);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userStorage.getUser(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        userStorage.deleteUser(userId);
    }
}
