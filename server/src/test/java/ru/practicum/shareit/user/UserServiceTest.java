package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.EmailAlreadyExistsException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserDto userDto;
    private Long userId;

    @BeforeEach
    public void setUp() {
        userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail("test@example.com");

        userDto = userService.createUser(userDto);
        userId = userDto.getId();
    }

    @Test
    void createUser() {
        UserDto newUser = UserDto.builder()
                .name("new name")
                .email("new@example.com")
                .build();

        UserDto createdUser = userService.createUser(newUser);
        assertNotNull(createdUser);
        assertEquals(newUser.getName(), createdUser.getName());
        assertEquals(newUser.getEmail(), createdUser.getEmail());
    }

    @Test
    void getUser() {
        UserDto user = userService.getUser(userId);
        assertNotNull(user);
        assertEquals(userId, user.getId());
        assertEquals(userDto.getName(), user.getName());
        assertEquals(userDto.getEmail(), user.getEmail());
    }

    @Test
    void updateUser() {
        UserDto updateForUser = UserDto.builder()
                .name("up name")
                .email("update@example.com")
                .build();

        UserDto updatedUser = userService.updateUser(userId, updateForUser);
        assertNotNull(updatedUser);
        assertEquals(userId, updatedUser.getId());
        assertEquals(updateForUser.getName(), updatedUser.getName());
        assertEquals(updateForUser.getEmail(), updatedUser.getEmail());
    }

    @Test
    void deleteUser() {
        userService.deleteUser(userId);
        assertThrows(NotFoundException.class, () -> userService.getUser(userId));
    }

    @Test
    void createUserWithDuplicateEmail() {
        UserDto duplicateUser = UserDto.builder()
                .name("Duplicate User")
                .email("test@example.com")
                .build();

        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.createUser(duplicateUser);
        });
    }
}
