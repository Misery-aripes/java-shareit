package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingNewDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public BookingDto createBooking(Long bookerId, BookingNewDto bookingDto) {
        User booker = getUser(bookerId);
        Item item = getItem(bookingDto.getItemId());

        validateItemAvailability(item);
        validateBookingDates(bookingDto);

        Booking booking = BookingMapper.toBooking(bookingDto, item, booker, BookingStatus.WAITING);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto updateBooking(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = getBooking(bookingId);
        validateBookingOwner(ownerId, booking);
        validateBookingStatus(booking);

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    public BookingDto findBookingByIdAndBookerIdOrOwnerId(Long userId, Long bookingId) {
        return bookingRepository.findById(bookingId)
                .filter(booking -> isBookerOrOwner(userId, booking))
                .map(BookingMapper::toBookingDto)
                .orElseThrow(() -> new NotFoundException("Бронирование с таким id не найдено"));
    }

    public List<BookingDto> findBookingsByState(Long bookerId, BookingState state) {
        return findBookings(state, bookerId, false);
    }

    public List<BookingDto> findBookingsByOwnerId(Long ownerId, BookingState state) {
        return findBookings(state, ownerId, true);
    }

    private List<BookingDto> findBookings(BookingState state, Long userId, boolean isOwner) {
        User user = getUser(userId);
        List<Booking> bookings = getBookingQueryMap(isOwner).get(state).apply(userId);
        return bookings.stream().map(BookingMapper::toBookingDto).toList();
    }

    private Map<BookingState, Function<Long, List<Booking>>> getBookingQueryMap(boolean isOwner) {
        return Map.of(
                BookingState.ALL, id -> isOwner
                        ? bookingRepository.findAllByItemOwnerIdOrderByStartDesc(id)
                        : bookingRepository.findAllByBookerIdOrderByStartDesc(id),
                BookingState.CURRENT, id -> isOwner
                        ? bookingRepository.findAllByItemOwnerIdAndStatusAndEndIsAfterOrderByStartDesc(id, BookingStatus.APPROVED, LocalDateTime.now())
                        : bookingRepository.findAllByBookerIdAndStatusAndEndIsAfterOrderByStartDesc(id, BookingStatus.APPROVED, LocalDateTime.now()),
                BookingState.PAST, id -> isOwner
                        ? bookingRepository.findAllByItemOwnerIdAndStatusAndEndIsBeforeOrderByStartDesc(id, BookingStatus.APPROVED, LocalDateTime.now())
                        : bookingRepository.findAllByBookerIdAndStatusAndEndIsBeforeOrderByStartDesc(id, BookingStatus.APPROVED, LocalDateTime.now()),
                BookingState.FUTURE, id -> isOwner
                        ? bookingRepository.findAllByItemOwnerIdAndStatusAndStartIsAfterOrderByStartDesc(id, BookingStatus.APPROVED, LocalDateTime.now())
                        : bookingRepository.findAllByBookerIdAndStatusAndStartIsAfterOrderByStartDesc(id, BookingStatus.APPROVED, LocalDateTime.now()),
                BookingState.WAITING, id -> isOwner
                        ? bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(id, BookingStatus.WAITING)
                        : bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(id, BookingStatus.WAITING),
                BookingState.REJECTED, id -> isOwner
                        ? bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(id, BookingStatus.REJECTED)
                        : bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(id, BookingStatus.REJECTED)
        );
    }

    private void validateItemAvailability(Item item) {
        if (!item.getAvailable()) {
            throw new RuntimeException("Предмет уже забронирован");
        }
    }

    private void validateBookingDates(BookingNewDto bookingDto) {
        boolean hasOverlappingBookings = !bookingRepository.findAllWithIntersectionDates(
                bookingDto.getItemId(),
                Set.of(BookingStatus.APPROVED),
                bookingDto.getStart(),
                bookingDto.getEnd()
        ).isEmpty();

        if (hasOverlappingBookings) {
            throw new NotFoundException("Предмет занят в указанные даты");
        }
    }

    private void validateBookingOwner(Long ownerId, Booking booking) {
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Пользователь не является владельцем предмета");
        }
    }

    private void validateBookingStatus(Booking booking) {
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new RuntimeException("Бронирование уже подтверждено");
        }
    }

    private boolean isBookerOrOwner(Long userId, Booking booking) {
        return booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не найден"));
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с таким id не найден"));
    }

    private Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с таким id не найдено"));
    }
}
