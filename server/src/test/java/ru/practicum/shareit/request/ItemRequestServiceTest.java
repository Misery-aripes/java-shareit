package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class ItemRequestServiceTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private ItemRequestDto itemRequestDto;
    private Long userId = 1L;
    private Long requestId = 1L;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("user");
        user.setEmail("9lS0e@example.com");
        userRepository.save(user);
        userId = user.getId();

        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription("Test Request Description");

        itemRequestDto = itemRequestService.addItemRequest(userId, itemRequestDto);
        requestId = itemRequestDto.getId();
    }

    @Test
    void addItemRequest() {
        ItemRequestDto newItemRequest = new ItemRequestDto();
        newItemRequest.setDescription("New Test Request Description");

        ItemRequestDto savedItemRequest = itemRequestService.addItemRequest(userId, newItemRequest);
        assertNotNull(savedItemRequest);
        assertEquals(newItemRequest.getDescription(), savedItemRequest.getDescription());
    }

    @Test
    void getRequestById() {
        ItemRequestDto itemRequest = itemRequestService.getRequestById(userId, requestId);
        assertNotNull(itemRequest);
        assertEquals(requestId, itemRequest.getId());
        assertEquals(itemRequestDto.getDescription(), itemRequest.getDescription());
    }

    @Test
    void getAllRequestsByUserId() {
        List<ItemRequestDto> itemRequests = itemRequestService.getAllUserRequests(userId);
        assertNotNull(itemRequests);
        assertEquals(1, itemRequests.size());
        assertEquals(requestId, itemRequests.getFirst().getId());
        assertEquals(itemRequestDto.getDescription(), itemRequests.getFirst().getDescription());
    }

    @Test
    void testRequestWithNotFoundException() {
        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(userId, 99L));
    }

    @Test
    void getAllRequests() {
        List<ItemRequestDto> itemRequests = itemRequestService.getAllRequests(userId);
        assertNotNull(itemRequests);
        assertEquals(1, itemRequests.size());
        assertEquals(requestId, itemRequests.getFirst().getId());
        assertEquals(itemRequestDto.getDescription(), itemRequests.getFirst().getDescription());
    }
}
