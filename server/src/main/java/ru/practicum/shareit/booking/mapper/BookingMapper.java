package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingNewDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getItem().getId(),
                booking.getItem(),
                booking.getBooker(),
                booking.getStatus(),
                booking.getStart(),
                booking.getEnd()
        );
    }

    public static Booking toBooking(BookingNewDto bookingDto, Item item, User booker, BookingStatus status) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        return booking;
    }
}
