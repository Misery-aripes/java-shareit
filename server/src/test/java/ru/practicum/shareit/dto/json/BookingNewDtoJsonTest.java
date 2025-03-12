package ru.practicum.shareit.dto.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingNewDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingNewDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerializeAndDeserialize() throws Exception {
        var originalDto = new BookingNewDto(1L,
                LocalDateTime.of(2025, 6, 10, 12, 0),
                LocalDateTime.of(2025, 6, 20, 12, 0));

        String json = objectMapper.writeValueAsString(originalDto);
        var deserializedDto = objectMapper.readValue(json, BookingNewDto.class);

        assertThat(deserializedDto)
                .usingRecursiveComparison()
                .isEqualTo(originalDto);
    }
}
