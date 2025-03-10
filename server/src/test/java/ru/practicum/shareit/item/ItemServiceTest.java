package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentNewDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
public class ItemServiceTest {

    @Autowired
    private ItemServiceImpl itemService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private ItemDto itemDto;
    private CommentDto commentDto;
    private Long userId;
    private Long itemId;
    private Long bookingId;

    @BeforeEach
    public void setUp() {
        User user = new User();
        user.setName("user");
        user.setEmail("9lS0e@example.com");
        userRepository.save(user);
        userId = user.getId();

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription("description");
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setRequester(user);
        itemRequestRepository.save(itemRequest);
        Long itemRequestId = itemRequest.getId();

        itemDto = new ItemDto();
        itemDto.setName("name");
        itemDto.setDescription("description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(itemRequestId);

        itemDto = itemService.createItem(userId, itemDto);
        itemId = itemDto.getId();

        commentDto = CommentDto.builder()
                .id(1L)
                .text("comment")
                .created(LocalDateTime.now())
                .build();

        Booking booking = new Booking();
        booking.setItem(itemRepository.findById(itemId).orElseThrow());
        booking.setBooker(user);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
        bookingId = booking.getId();
    }

    @Test
    void createItem() {
        ItemDto newItem = ItemDto.builder()
                .name("new name")
                .description("new description")
                .available(true)
                .build();

        ItemDto createdItem = itemService.createItem(userId, newItem);
        assertNotNull(createdItem);
        assertEquals(newItem.getName(), createdItem.getName());
        assertEquals(newItem.getDescription(), createdItem.getDescription());
        assertEquals(newItem.getAvailable(), createdItem.getAvailable());
    }

    @Test
    void updateItem() {
        ItemDto updateInputItem = ItemDto.builder()
                .name("update name")
                .description("update description")
                .available(false)
                .build();

        ItemDto updatedItem = itemService.updateItem(userId, itemId, updateInputItem);
        assertNotNull(updatedItem);
        assertEquals(updateInputItem.getName(), updatedItem.getName());
        assertEquals(updateInputItem.getDescription(), updatedItem.getDescription());
        assertEquals(updateInputItem.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void findItemById() {
        ItemDto itemDtoById = itemService.getItem(userId, itemId);
        assertNotNull(itemDtoById);
        assertEquals(itemDto.getName(), itemDtoById.getName());
        assertEquals(itemDto.getDescription(), itemDtoById.getDescription());
        assertEquals(itemDto.getAvailable(), itemDtoById.getAvailable());
    }

    @Test
    void fingByText() {
        List<ItemDto> items = itemService.searchItems("name");
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(itemDto.getName(), items.getFirst().getName());
    }

    @Test
    void findItemsByUserId() {
        List<ItemDto> items = itemService.getAllItems(userId);
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(itemDto.getName(), items.getFirst().getName());
    }

    @Test
    void deleteItem() {
        itemService.deleteItem(userId, itemId);
        assertThrows(NotFoundException.class, () -> itemService.getItem(userId, itemId));
    }

    @Test
    void addComment() {
        CommentDto comment = itemService.addComment(userId, itemId,
                new CommentNewDto(commentDto.getText()));
        assertNotNull(comment);
        assertEquals(commentDto.getText(), comment.getText());
    }

    @Test
    void addCommentWithNonExistingItem() {
        assertThrows(NotFoundException.class, () -> itemService.addComment(userId, 99L,
                new CommentNewDto(commentDto.getText())));
    }

    @Test
    void addCommentWithNonExistingUser() {
        assertThrows(NotFoundException.class, () -> itemService.addComment(99L, itemId,
                new CommentNewDto(commentDto.getText())));
    }

    @Test
    void addCommentWithNonBookedItem() {
        bookingRepository.deleteById(bookingId);
        assertThrows(RuntimeException.class, () -> itemService.addComment(userId, itemId,
                new CommentNewDto(commentDto.getText())));
    }
}
