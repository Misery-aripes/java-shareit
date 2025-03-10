package ru.practicum.shareit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select b from Booking as b " +
            "where b.item.id = :itemId and b.status in (:statuses) and :startDate <= b.end and :endDate >= b.start")
    List<Booking> findAllWithIntersectionDates(Long itemId, Set<BookingStatus> statuses, LocalDateTime startDate,
                                               LocalDateTime endDate);

    List<Booking> findAllByItemIdAndBookerId(Long itemId, Long bookerId);

    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByBookerIdAndStatusAndEndIsAfterOrderByStartDesc(Long bookerId, BookingStatus status, LocalDateTime dt);

    List<Booking> findAllByBookerIdAndStatusAndEndIsBeforeOrderByStartDesc(Long bookerId, BookingStatus status, LocalDateTime dt);

    List<Booking> findAllByBookerIdAndStatusAndStartIsAfterOrderByStartDesc(Long bookerId, BookingStatus status, LocalDateTime dt);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findAllByItemIdOrderByStartAsc(Long itemId);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findAllByItemOwnerIdAndStatusAndEndIsAfterOrderByStartDesc(Long ownerId, BookingStatus status, LocalDateTime dt);

    List<Booking> findAllByItemOwnerIdAndStatusAndEndIsBeforeOrderByStartDesc(Long ownerId, BookingStatus status, LocalDateTime dt);

    List<Booking> findAllByItemOwnerIdAndStatusAndStartIsAfterOrderByStartDesc(Long ownerId, BookingStatus status, LocalDateTime dt);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingStatus status);
}
