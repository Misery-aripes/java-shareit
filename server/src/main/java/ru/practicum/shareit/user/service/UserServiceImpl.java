package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.EmailAlreadyExistsException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
        }

        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не найден"));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
                throw new EmailAlreadyExistsException("Пользователь с таким email уже существует");
            }
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        userRepository.deleteById(userId);
    }
}
