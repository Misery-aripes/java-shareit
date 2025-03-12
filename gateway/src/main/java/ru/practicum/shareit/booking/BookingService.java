package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.exception.InvalidStateException;

@Service
@RequiredArgsConstructor
public class BookingService {

    public State parseState(String stateParam) {
        return State.fromString(stateParam)
                .orElseThrow(() -> new InvalidStateException("Указано неизвестное состояние бронирования:" + stateParam));
    }
}
