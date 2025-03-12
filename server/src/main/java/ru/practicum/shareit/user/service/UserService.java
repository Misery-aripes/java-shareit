package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto getUser(Long userId);

    UserDto updateUser(Long userId, UserDto userDto);

    void deleteUser(Long userId);
}
