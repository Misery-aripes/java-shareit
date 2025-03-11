package ru.practicum.shareit.dto.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.util.ArrayList;

@JsonTest
public class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    public void testSerializeAndDeserialize() throws Exception {
        User user = new User(1L, "John Doe", "john.doe@example.com");
        Item item = new Item(2L, "Laptop", "Gaming Laptop", true, user, null, new ArrayList<>());
        BookingDto bookingDto = BookingDto.builder()
                .id(3L)
                .itemId(2L)
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED)
                .start(LocalDateTime.of(2025, 3, 10, 12, 0))
                .end(LocalDateTime.of(2025, 3, 15, 12, 0))
                .build();

        JsonContent<BookingDto> result = json.write(bookingDto);
        assertThat(result).hasJsonPathNumberValue("$.id", 3)
                .hasJsonPathNumberValue("$.itemId", 2)
                .hasJsonPathStringValue("$.status", "APPROVED")
                .hasJsonPathStringValue("$.start", "2025-03-10T12:00:00")
                .hasJsonPathStringValue("$.end", "2025-03-15T12:00:00");

        String jsonContent = result.getJson();
        BookingDto deserialized = json.parse(jsonContent).getObject();
        assertThat(deserialized).isEqualToComparingFieldByField(bookingDto);
    }
}
