package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingNewDto {

    private Long itemId;

    @NotNull
    @Future(message = "Дата начала бронирования не может быть в прошлом")
    private LocalDateTime start;

    @NotNull
    @Future(message = "Дата окончания бронирования не может быть в прошлом")
    private LocalDateTime end;
}
