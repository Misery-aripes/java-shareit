package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingNewDto;
import ru.practicum.shareit.booking.dto.State;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingClient bookingClient;
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader(name = HEADER_USER_ID) Long userId,
                                             @RequestBody @Valid BookingNewDto createBookingDto) {
        if (createBookingDto.getStart().equals(createBookingDto.getEnd())) {
            throw new ValidationException("Начало бронирования не может быть равно его окончанию");
        }
        return bookingClient.addBooking(userId, createBookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> patchBooking(@RequestHeader(name = HEADER_USER_ID) Long userId,
                                               @PathVariable Long bookingId,
                                               @RequestParam boolean approved) {
        return bookingClient.patchBooking(userId, bookingId, approved);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByBooker(@RequestHeader(name = HEADER_USER_ID) Long bookerId,
                                                      @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                      @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                                      @Positive @RequestParam(name = "size", defaultValue = "10") int size) {

        State state = State.fromString(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getBookingsByBooker(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwner(@RequestHeader(name = HEADER_USER_ID) Long userId,
                                                     @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                     @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                                     @Positive @RequestParam(name = "size", defaultValue = "10") int size) {

        State state = State.fromString(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getBookingsByOwner(userId, state, from, size);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingByBookerOrOwnerItem(@RequestHeader(name = HEADER_USER_ID) Long userId,
                                                                @PathVariable Long bookingId) {
        return bookingClient.getBookingByBookerOrOwnerItem(userId, bookingId);
    }
}
