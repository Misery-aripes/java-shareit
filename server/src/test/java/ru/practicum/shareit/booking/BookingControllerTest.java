package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingNewDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    BookingRepository bookingRepository;

    @Autowired
    private MockMvc mvc;

    private Booking booking;
    private BookingNewDto createBookingDto;
    private BookingDto bookingDto;
    private ItemDto itemDto;
    private UserDto userDto;
    private List<BookingDto> bookingDtoList;
    private List<ItemDto> itemDtoList;
    private List<UserDto> userDtoList;

    private static final Long ENTITIES_COUNT = 10L;
    private static final Random RANDOM = new Random();

    @BeforeEach
    void setUp() {

        itemDto = ItemDto.builder()
                .id(1L)
                .name(generateRandomString(5))
                .description(generateRandomString(5))
                .available(true)
                .requestId(null)
                .lastBooking(null)
                .nextBooking(null)
                .comments(null)
                .build();

        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setRequest(null);

        itemDtoList = new ArrayList<>();
        for (long i = 1; i <= ENTITIES_COUNT; i++) {
            itemDtoList.add(ItemDto.builder()
                    .id(i)
                    .name(generateRandomString(5))
                    .description(generateRandomString(5))
                    .available(true)
                    .lastBooking(null)
                    .nextBooking(null)
                    .comments(null)
                    .build());
        }

        userDto = UserDto.builder()
                .id(1L)
                .name(generateRandomString(5))
                .email(generateRandomEmail(5))
                .build();

        User user = new User();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        userDtoList = new ArrayList<>();
        for (long i = 1; i <= ENTITIES_COUNT; i++) {
            userDtoList.add(UserDto.builder()
                    .id(i)
                    .name(generateRandomString(5))
                    .email(generateRandomEmail(5))
                    .build());
        }

        bookingDto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusMinutes(1))
                .end(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(bookingDto.getStatus());

        createBookingDto = new BookingNewDto();
        createBookingDto.setStart(bookingDto.getStart());
        createBookingDto.setEnd(bookingDto.getEnd());
        createBookingDto.setItemId(bookingDto.getItem().getId());

        bookingDtoList = new ArrayList<>();
        for (long i = 1; i <= ENTITIES_COUNT; i++) {
            bookingDtoList.add(BookingDto.builder()
                    .id(i)
                    .start(LocalDateTime.now().plusMinutes(1))
                    .end(LocalDateTime.now().plusDays(1))
                    .item(item)
                    .booker(user)
                    .status(BookingStatus.WAITING)
                    .build());
        }
    }

    @Test
    void createBookingTest() throws Exception {
        Long userId = 1L;

        when(bookingService.createBooking(anyLong(), any(BookingNewDto.class)))
                .thenReturn(bookingDto);

        mvc.perform(post("/bookings")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBookingDto))
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().name())));

        verify(bookingService, times(1)).createBooking(eq(userId), eq(createBookingDto));
    }

    @Test
    void updateBookingStatusTest() throws Exception {
        Long bookingId = 1L;
        Long userId = 1L;
        Boolean approved = true;
        bookingDto.setStatus(BookingStatus.APPROVED);

        when(bookingService.updateBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(bookingDto);

        mvc.perform(patch("/bookings/" + bookingDto.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", bookingDto.getBooker().getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().name())));

        verify(bookingService, times(1)).updateBooking(eq(bookingId), eq(userId), eq(approved));
    }

    @Test
    void getBookingByIdTest() throws Exception {
        when(bookingService.findBookingByIdAndBookerIdOrOwnerId(anyLong(), anyLong()))
                .thenReturn(bookingDto);

        mvc.perform(get("/bookings/" + bookingDto.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookingDto.getBooker().getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class));

        verify(bookingService, times(1)).findBookingByIdAndBookerIdOrOwnerId(eq(bookingDto.getId()), eq(bookingDto.getBooker().getId()));
    }

    @Test
    void getAllUsersBookingByStatusTest() throws Exception {
        Long userId = 1L;

        when(bookingService.findBookingsByState(Mockito.anyLong(), Mockito.any()))
                .thenReturn(bookingDtoList);

        mvc.perform(get("/bookings")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookingDto.getBooker().getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(bookingDtoList.size())))
                .andExpect(jsonPath("$[0].id", is(bookingDtoList.getFirst().getId().intValue())));

        verify(bookingService, times(1)).findBookingsByState(eq(userId), eq(BookingState.ALL));
    }

    @Test
    void getAllBookingForUserItemsByStatusTest() throws Exception {
        Long userId = 1L;

        when(bookingService.findBookingsByOwnerId(anyLong(), any()))
                .thenReturn(bookingDtoList);

        mvc.perform(get("/bookings/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", bookingDto.getBooker().getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()", is(bookingDtoList.size())))
                .andExpect(jsonPath("$[0].id", is(bookingDtoList.getFirst().getId().intValue())));

        verify(bookingService, times(1)).findBookingsByOwnerId(eq(userId), eq(BookingState.ALL));
    }

    private String generateRandomString(int targetStringLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        return RANDOM.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private String generateRandomEmail(int targetStringLength) {
        return generateRandomString(targetStringLength) + "@example.com";
    }

    private LocalDateTime generateRandomDate() {
        long minDay = LocalDateTime.of(2000, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC);
        long maxDay = LocalDateTime.of(2023, 12, 31, 23, 59).toEpochSecond(ZoneOffset.UTC);
        long randomDay = minDay + RANDOM.nextLong() % (maxDay - minDay);
        return LocalDateTime.ofEpochSecond(randomDay, 0, ZoneOffset.UTC);
    }
}