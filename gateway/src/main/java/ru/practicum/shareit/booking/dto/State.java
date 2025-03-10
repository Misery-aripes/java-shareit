package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum State {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static Optional<State> fromString(String state) {
        for (State s : State.values()) {
            if (s.name().equalsIgnoreCase(state)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }
}
