package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingNewDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional
public class BookingServiceTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private Item item;
    private BookingNewDto bookingNewDto;
    private BookingDto bookingDto;
    private Long userId;
    private Long itemId;
    private Long bookingId;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setName("Name");
        user.setEmail("test@mail.ru");
        userRepository.save(user);
        userId = user.getId();

        item = new Item();
        item.setName("Name");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(user);
        itemRepository.save(item);
        itemId = item.getId();

        bookingNewDto = new BookingNewDto();
        bookingNewDto.setItemId(itemId);
        bookingNewDto.setStart(LocalDateTime.now().plusHours(1));
        bookingNewDto.setEnd(LocalDateTime.now().plusHours(2));

        bookingDto = BookingDto.builder()
                .id(1L)
                .start(bookingNewDto.getStart())
                .end(bookingNewDto.getEnd())
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void createBooking() {
        BookingDto bookingDto = bookingService.createBooking(userId, bookingNewDto);
        assertNotNull(bookingDto);
        assertEquals(bookingNewDto.getStart(), bookingDto.getStart());
        assertEquals(bookingNewDto.getEnd(), bookingDto.getEnd());
        assertEquals(itemId, bookingDto.getItem().getId());
        assertEquals(userId, bookingDto.getBooker().getId());
    }

    @Test
    void findBookingById() {
        BookingDto bookingDto = bookingService.createBooking(userId, bookingNewDto);
        BookingDto bookingDtoById = bookingService.findBookingByIdAndBookerIdOrOwnerId(userId, bookingDto.getId());
        assertNotNull(bookingDtoById);
        assertEquals(bookingDto.getId(), bookingDtoById.getId());
        assertEquals(bookingDto.getStart(), bookingDtoById.getStart());
        assertEquals(bookingDto.getEnd(), bookingDtoById.getEnd());
        assertEquals(bookingDto.getItem().getId(), bookingDtoById.getItem().getId());
        assertEquals(bookingDto.getBooker().getId(), bookingDtoById.getBooker().getId());
    }

    @Test
    void findBookingsByBookerId() {
        BookingDto bookingDto = bookingService.createBooking(userId, bookingNewDto);
        List<BookingDto> bookingDtoList = bookingService.findBookingsByState(userId, BookingState.ALL);
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(bookingDto.getId(), bookingDtoList.getFirst().getId());
        assertEquals(bookingDto.getStart(), bookingDtoList.getFirst().getStart());
        assertEquals(bookingDto.getEnd(), bookingDtoList.getFirst().getEnd());
        assertEquals(bookingDto.getItem().getId(), bookingDtoList.getFirst().getItem().getId());
        assertEquals(bookingDto.getBooker().getId(), bookingDtoList.getFirst().getBooker().getId());
    }

    @Test
    void findBookingsByOwnerId() {
        BookingDto bookingDto = bookingService.createBooking(userId, bookingNewDto);
        List<BookingDto> bookingDtoList = bookingService.findBookingsByOwnerId(userId, BookingState.ALL);
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(bookingDto.getId(), bookingDtoList.getFirst().getId());
        assertEquals(bookingDto.getStart(), bookingDtoList.getFirst().getStart());
        assertEquals(bookingDto.getEnd(), bookingDtoList.getFirst().getEnd());
        assertEquals(bookingDto.getItem().getId(), bookingDtoList.getFirst().getItem().getId());
        assertEquals(bookingDto.getBooker().getId(), bookingDtoList.getFirst().getBooker().getId());
    }

    @Test
    void findBookingByOwnerIdWithStateCurrent() {
        User user2 = new User();
        user2.setName("Name2");
        user2.setEmail("test2@mail.ru");
        userRepository.save(user2);
        Long userId2 = user2.getId();

        BookingNewDto pastBooking = new BookingNewDto();
        pastBooking.setItemId(itemId);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        BookingDto pastBookingDto = bookingService.createBooking(userId2, pastBooking);
        Long pastBookingId = pastBookingDto.getId();

        BookingNewDto currentBooking = new BookingNewDto();
        currentBooking.setItemId(itemId);
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        BookingDto currentBookingDto = bookingService.createBooking(userId2, currentBooking);
        Long currentBookingId = currentBookingDto.getId();

        BookingNewDto futureBooking = new BookingNewDto();
        futureBooking.setItemId(itemId);
        futureBooking.setStart(LocalDateTime.now().plusDays(2));
        futureBooking.setEnd(LocalDateTime.now().plusDays(3));
        BookingDto futureBookingDto = bookingService.createBooking(userId2, futureBooking);
        Long futureBookingId = futureBookingDto.getId();

        bookingService.updateBooking(userId, currentBookingId, true);

        List<BookingDto> bookingDtoList = bookingService.findBookingsByOwnerId(userId, BookingState.CURRENT);
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(currentBookingId, bookingDtoList.getFirst().getId());
    }

    @Test
    void findBookingByOwnerIdWithStateFuture() {
        User user2 = new User();
        user2.setName("Name2");
        user2.setEmail("test2@mail.ru");
        userRepository.save(user2);
        Long userId2 = user2.getId();

        BookingNewDto pastBooking = new BookingNewDto();
        pastBooking.setItemId(itemId);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        BookingDto pastBookingDto = bookingService.createBooking(userId2, pastBooking);
        Long pastBookingId = pastBookingDto.getId();

        BookingNewDto currentBooking = new BookingNewDto();
        currentBooking.setItemId(itemId);
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        BookingDto currentBookingDto = bookingService.createBooking(userId2, currentBooking);
        Long currentBookingId = currentBookingDto.getId();

        BookingNewDto futureBooking = new BookingNewDto();
        futureBooking.setItemId(itemId);
        futureBooking.setStart(LocalDateTime.now().plusDays(2));
        futureBooking.setEnd(LocalDateTime.now().plusDays(3));
        BookingDto futureBookingDto = bookingService.createBooking(userId2, futureBooking);
        Long futureBookingId = futureBookingDto.getId();

        bookingService.updateBooking(userId, futureBookingId, true);

        List<BookingDto> bookingDtoList = bookingService.findBookingsByOwnerId(userId, BookingState.FUTURE);
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(futureBookingId, bookingDtoList.getFirst().getId());
    }

    @Test
    void findBookingByOwnerIdWithStatePast() {
        User user2 = new User();
        user2.setName("Name2");
        user2.setEmail("test2@mail.ru");
        userRepository.save(user2);
        Long userId2 = user2.getId();

        BookingNewDto pastBooking = new BookingNewDto();
        pastBooking.setItemId(itemId);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        BookingDto pastBookingDto = bookingService.createBooking(userId2, pastBooking);
        Long pastBookingId = pastBookingDto.getId();

        BookingNewDto currentBooking = new BookingNewDto();
        currentBooking.setItemId(itemId);
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        BookingDto currentBookingDto = bookingService.createBooking(userId2, currentBooking);
        Long currentBookingId = currentBookingDto.getId();

        BookingNewDto futureBooking = new BookingNewDto();
        futureBooking.setItemId(itemId);
        futureBooking.setStart(LocalDateTime.now().plusDays(2));
        futureBooking.setEnd(LocalDateTime.now().plusDays(3));
        BookingDto futureBookingDto = bookingService.createBooking(userId2, futureBooking);
        Long futureBookingId = futureBookingDto.getId();

        bookingService.updateBooking(userId, pastBookingId, true);

        List<BookingDto> bookingDtoList = bookingService.findBookingsByOwnerId(userId, BookingState.PAST);
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(pastBookingId, bookingDtoList.getFirst().getId());
    }

    @Test
    void findBookingByOwnerIdWithStateWaiting() {
        User user2 = new User();
        user2.setName("Name2");
        user2.setEmail("test2@mail.ru");
        userRepository.save(user2);
        Long userId2 = user2.getId();

        BookingNewDto pastBooking = new BookingNewDto();
        pastBooking.setItemId(itemId);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        BookingDto pastBookingDto = bookingService.createBooking(userId2, pastBooking);
        Long pastBookingId = pastBookingDto.getId();

        BookingNewDto currentBooking = new BookingNewDto();
        currentBooking.setItemId(itemId);
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        BookingDto currentBookingDto = bookingService.createBooking(userId2, currentBooking);
        Long currentBookingId = currentBookingDto.getId();

        BookingNewDto futureBooking = new BookingNewDto();
        futureBooking.setItemId(itemId);
        futureBooking.setStart(LocalDateTime.now().plusDays(2));
        futureBooking.setEnd(LocalDateTime.now().plusDays(3));
        BookingDto futureBookingDto = bookingService.createBooking(userId2, futureBooking);
        Long futureBookingId = futureBookingDto.getId();

        List<BookingDto> bookingDtoList = bookingService.findBookingsByOwnerId(userId, BookingState.WAITING);
        assertNotNull(bookingDtoList);
        assertEquals(3, bookingDtoList.size());
    }

    @Test
    void findBookingByOwnerIdWithStateRejected() {
        User user2 = new User();
        user2.setName("Name2");
        user2.setEmail("test2@mail.ru");
        userRepository.save(user2);
        Long userId2 = user2.getId();

        BookingNewDto pastBooking = new BookingNewDto();
        pastBooking.setItemId(itemId);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        BookingDto pastBookingDto = bookingService.createBooking(userId2, pastBooking);
        Long pastBookingId = pastBookingDto.getId();

        BookingNewDto currentBooking = new BookingNewDto();
        currentBooking.setItemId(itemId);
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        BookingDto currentBookingDto = bookingService.createBooking(userId2, currentBooking);
        Long currentBookingId = currentBookingDto.getId();

        BookingNewDto futureBooking = new BookingNewDto();
        futureBooking.setItemId(itemId);
        futureBooking.setStart(LocalDateTime.now().plusDays(2));
        futureBooking.setEnd(LocalDateTime.now().plusDays(3));
        BookingDto futureBookingDto = bookingService.createBooking(userId2, futureBooking);
        Long futureBookingId = futureBookingDto.getId();

        bookingService.updateBooking(userId, futureBookingId, false);

        List<BookingDto> bookingDtoList = bookingService.findBookingsByOwnerId(userId, BookingState.REJECTED);
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(futureBookingId, bookingDtoList.getFirst().getId());
    }

    @Test
    void findBookingsByState() {
        BookingDto bookingDto = bookingService.createBooking(userId, bookingNewDto);
        List<BookingDto> bookingDtoList = bookingService.findBookingsByState(userId, BookingState.ALL);
        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(bookingDto.getId(), bookingDtoList.getFirst().getId());
        assertEquals(bookingDto.getStart(), bookingDtoList.getFirst().getStart());
        assertEquals(bookingDto.getEnd(), bookingDtoList.getFirst().getEnd());
        assertEquals(bookingDto.getItem().getId(), bookingDtoList.getFirst().getItem().getId());
        assertEquals(bookingDto.getBooker().getId(), bookingDtoList.getFirst().getBooker().getId());
    }

    @Test
    void updateBooking() {
        BookingDto bookingDto = bookingService.createBooking(userId, bookingNewDto);
        BookingDto bookingDtoUpdated = bookingService.updateBooking(userId, bookingDto.getId(), true);
        assertNotNull(bookingDtoUpdated);
        assertEquals(bookingDto.getId(), bookingDtoUpdated.getId());
        assertEquals(bookingDto.getStart(), bookingDtoUpdated.getStart());
        assertEquals(bookingDto.getEnd(), bookingDtoUpdated.getEnd());
        assertEquals(bookingDto.getItem().getId(), bookingDtoUpdated.getItem().getId());
        assertEquals(bookingDto.getBooker().getId(), bookingDtoUpdated.getBooker().getId());
        assertEquals(BookingStatus.APPROVED, bookingDtoUpdated.getStatus());
    }

    @Test
    void approveBookingByWrongUserId() {
        User user2 = new User();
        user2.setName("Name2");
        user2.setEmail("test2@mail.ru");
        userRepository.save(user2);
        Long userId2 = user2.getId();
        assertThrows(RuntimeException.class, () -> bookingService.updateBooking(userId2, bookingDto.getId(), true));
    }

    @Test
    void getByIdWithInvalidUserId() {
        BookingDto bookingDto = bookingService.createBooking(userId, bookingNewDto);
        assertThrows(NotFoundException.class, () -> bookingService.findBookingByIdAndBookerIdOrOwnerId(99L, bookingDto.getId()));
    }

    @Test
    void createBookingWithUnavailableItem() {
        item.setAvailable(false);
        itemRepository.save(item);
        assertThrows(RuntimeException.class, () -> bookingService.createBooking(userId, bookingNewDto));
    }

    @Test
    void getAllBookingsByStateCurrent() {
        User user2 = new User();
        user2.setName("Name2");
        user2.setEmail("test2@mail.ru");
        userRepository.save(user2);
        Long userId2 = user2.getId();

        BookingNewDto pastBooking = new BookingNewDto();
        pastBooking.setItemId(itemId);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        BookingDto pastBookingDto = bookingService.createBooking(userId2, pastBooking);
        Long pastBookingId = pastBookingDto.getId();

        BookingNewDto currentBooking = new BookingNewDto();
        currentBooking.setItemId(itemId);
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        BookingDto currentBookingDto = bookingService.createBooking(userId2, currentBooking);
        Long currentBookingId = currentBookingDto.getId();

        BookingNewDto futureBooking = new BookingNewDto();
        futureBooking.setItemId(itemId);
        futureBooking.setStart(LocalDateTime.now().plusDays(2));
        futureBooking.setEnd(LocalDateTime.now().plusDays(3));
        BookingDto futureBookingDto = bookingService.createBooking(userId2, futureBooking);
        Long futureBookingId = futureBookingDto.getId();

        BookingDto updatedBooking = bookingService.updateBooking(userId, currentBookingId, true);

        List<BookingDto> bookingDtoList = bookingService.findBookingsByState(userId2, BookingState.CURRENT);
        assertEquals(1, bookingDtoList.size());
        assertEquals(currentBookingId, bookingDtoList.getFirst().getId());
        assertEquals(updatedBooking.getItem().getId(), bookingDtoList.getFirst().getItem().getId());
        assertEquals(updatedBooking.getBooker().getId(), bookingDtoList.getFirst().getBooker().getId());
    }

    @Test
    void getAllBookingsByStateFuture() {
        User user2 = new User();
        user2.setName("Name2");
        user2.setEmail("test2@mail.ru");
        userRepository.save(user2);
        Long userId2 = user2.getId();

        BookingNewDto pastBooking = new BookingNewDto();
        pastBooking.setItemId(itemId);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        BookingDto pastBookingDto = bookingService.createBooking(userId2, pastBooking);
        Long pastBookingId = pastBookingDto.getId();

        BookingNewDto currentBooking = new BookingNewDto();
        currentBooking.setItemId(itemId);
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        BookingDto currentBookingDto = bookingService.createBooking(userId2, currentBooking);
        Long currentBookingId = currentBookingDto.getId();

        BookingNewDto futureBooking = new BookingNewDto();
        futureBooking.setItemId(itemId);
        futureBooking.setStart(LocalDateTime.now().plusDays(2));
        futureBooking.setEnd(LocalDateTime.now().plusDays(3));
        BookingDto futureBookingDto = bookingService.createBooking(userId2, futureBooking);
        Long futureBookingId = futureBookingDto.getId();

        BookingDto updatedBooking = bookingService.updateBooking(userId, futureBookingId, true);

        List<BookingDto> bookingDtoList = bookingService.findBookingsByState(userId2, BookingState.FUTURE);
        assertEquals(1, bookingDtoList.size());
        assertEquals(futureBookingId, bookingDtoList.getFirst().getId());
        assertEquals(updatedBooking.getItem().getId(), bookingDtoList.getFirst().getItem().getId());
        assertEquals(updatedBooking.getBooker().getId(), bookingDtoList.getFirst().getBooker().getId());
    }

    @Test
    void getAllBookingsByStatePast() {
        User user2 = new User();
        user2.setName("Name2");
        user2.setEmail("test2@mail.ru");
        userRepository.save(user2);
        Long userId2 = user2.getId();

        BookingNewDto pastBooking = new BookingNewDto();
        pastBooking.setItemId(itemId);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        BookingDto pastBookingDto = bookingService.createBooking(userId2, pastBooking);
        Long pastBookingId = pastBookingDto.getId();

        BookingNewDto currentBooking = new BookingNewDto();
        currentBooking.setItemId(itemId);
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        BookingDto currentBookingDto = bookingService.createBooking(userId2, currentBooking);
        Long currentBookingId = currentBookingDto.getId();

        BookingNewDto futureBooking = new BookingNewDto();
        futureBooking.setItemId(itemId);
        futureBooking.setStart(LocalDateTime.now().plusDays(2));
        futureBooking.setEnd(LocalDateTime.now().plusDays(3));
        BookingDto futureBookingDto = bookingService.createBooking(userId2, futureBooking);
        Long futureBookingId = futureBookingDto.getId();

        BookingDto updatedBooking = bookingService.updateBooking(userId, pastBookingId, true);

        List<BookingDto> bookingDtoList = bookingService.findBookingsByState(userId2, BookingState.PAST);
        assertEquals(1, bookingDtoList.size());
        assertEquals(pastBookingId, bookingDtoList.getFirst().getId());
        assertEquals(updatedBooking.getItem().getId(), bookingDtoList.getFirst().getItem().getId());
        assertEquals(updatedBooking.getBooker().getId(), bookingDtoList.getFirst().getBooker().getId());
    }

    @Test
    void getAllBookingsByStateAll() {
        User user2 = new User();
        user2.setName("Name2");
        user2.setEmail("test2@mail.ru");
        userRepository.save(user2);
        Long userId2 = user2.getId();

        BookingNewDto pastBooking = new BookingNewDto();
        pastBooking.setItemId(itemId);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        BookingDto pastBookingDto = bookingService.createBooking(userId2, pastBooking);
        Long pastBookingId = pastBookingDto.getId();

        BookingNewDto currentBooking = new BookingNewDto();
        currentBooking.setItemId(itemId);
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        BookingDto currentBookingDto = bookingService.createBooking(userId2, currentBooking);
        Long currentBookingId = currentBookingDto.getId();

        BookingNewDto futureBooking = new BookingNewDto();
        futureBooking.setItemId(itemId);
        futureBooking.setStart(LocalDateTime.now().plusDays(2));
        futureBooking.setEnd(LocalDateTime.now().plusDays(3));
        BookingDto futureBookingDto = bookingService.createBooking(userId2, futureBooking);
        Long futureBookingId = futureBookingDto.getId();

        List<BookingDto> bookingDtoList = bookingService.findBookingsByState(userId2, BookingState.ALL);
        assertEquals(3, bookingDtoList.size());
    }

    @Test
    void getBookingsByStatusWaiting() {
        User user2 = new User();
        user2.setName("Name2");
        user2.setEmail("test2@mail.ru");
        userRepository.save(user2);
        Long userId2 = user2.getId();

        BookingNewDto pastBooking = new BookingNewDto();
        pastBooking.setItemId(itemId);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        BookingDto pastBookingDto = bookingService.createBooking(userId2, pastBooking);
        Long pastBookingId = pastBookingDto.getId();

        BookingNewDto currentBooking = new BookingNewDto();
        currentBooking.setItemId(itemId);
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        BookingDto currentBookingDto = bookingService.createBooking(userId2, currentBooking);
        Long currentBookingId = currentBookingDto.getId();

        BookingNewDto futureBooking = new BookingNewDto();
        futureBooking.setItemId(itemId);
        futureBooking.setStart(LocalDateTime.now().plusDays(2));
        futureBooking.setEnd(LocalDateTime.now().plusDays(3));
        BookingDto futureBookingDto = bookingService.createBooking(userId2, futureBooking);
        Long futureBookingId = futureBookingDto.getId();

        bookingService.updateBooking(userId, pastBookingId, true);
        bookingService.updateBooking(userId, currentBookingId, true);

        List<BookingDto> bookingDtoList = bookingService.findBookingsByState(userId2, BookingState.WAITING);
        assertEquals(1, bookingDtoList.size());
        assertEquals(futureBookingId, bookingDtoList.getFirst().getId());
        assertEquals(futureBooking.getItemId(), bookingDtoList.getFirst().getItem().getId());
    }

    @Test
    void getBookingsByStatusRejected() {
        User user2 = new User();
        user2.setName("Name2");
        user2.setEmail("test2@mail.ru");
        userRepository.save(user2);
        Long userId2 = user2.getId();

        BookingNewDto pastBooking = new BookingNewDto();
        pastBooking.setItemId(itemId);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        BookingDto pastBookingDto = bookingService.createBooking(userId2, pastBooking);
        Long pastBookingId = pastBookingDto.getId();

        BookingNewDto currentBooking = new BookingNewDto();
        currentBooking.setItemId(itemId);
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        BookingDto currentBookingDto = bookingService.createBooking(userId2, currentBooking);
        Long currentBookingId = currentBookingDto.getId();

        BookingNewDto futureBooking = new BookingNewDto();
        futureBooking.setItemId(itemId);
        futureBooking.setStart(LocalDateTime.now().plusDays(2));
        futureBooking.setEnd(LocalDateTime.now().plusDays(3));
        BookingDto futureBookingDto = bookingService.createBooking(userId2, futureBooking);
        Long futureBookingId = futureBookingDto.getId();

        BookingDto updatedBooking = bookingService.updateBooking(userId, currentBookingId, false);

        List<BookingDto> bookingDtoList = bookingService.findBookingsByState(userId2, BookingState.REJECTED);
        assertEquals(1, bookingDtoList.size());
        assertEquals(currentBookingId, bookingDtoList.getFirst().getId());
        assertEquals(updatedBooking.getItemId(), bookingDtoList.getFirst().getItem().getId());
    }
}
