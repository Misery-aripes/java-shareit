package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
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
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                             @RequestBody @Valid BookingNewDto createBookingDto) {
        return bookingClient.addBooking(userId, createBookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> patchBooking(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                               @PathVariable Long bookingId,
                                               @RequestParam(name = "approved") Boolean approved) {
        return bookingClient.patchBooking(userId, bookingId, approved);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByBooker(@RequestHeader(name = "X-Sharer-User-Id") Long bookerId,
                                                      @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                      @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                                      @Positive @RequestParam(name = "size", defaultValue = "10") int size) {

        State state = bookingService.parseState(stateParam);
        return bookingClient.getBookingsByBooker(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwner(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                                     @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
                                                     @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
                                                     @Positive @RequestParam(name = "size", defaultValue = "10") int size) {

        State state = bookingService.parseState(stateParam);
        return bookingClient.getBookingsByOwner(userId, state, from, size);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingByBookerOrOwnerItem(@RequestHeader(name = "X-Sharer-User-Id") Long userId,
                                                                @PathVariable Long bookingId) {
        return bookingClient.getBookingByBookerOrOwnerItem(userId, bookingId);
    }
}
