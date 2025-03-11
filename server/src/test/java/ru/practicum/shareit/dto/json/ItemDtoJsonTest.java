package ru.practicum.shareit.dto.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerializeAndDeserialize() throws Exception {
        var comment = new CommentDto(1L, "Great item!", "John Doe",
                LocalDateTime.of(2025, 6, 15, 10, 0));

        var originalDto = ItemDto.builder()
                .id(100L)
                .name("Laptop")
                .description("Gaming laptop")
                .available(true)
                .requestId(10L)
                .lastBooking(LocalDateTime.of(2025, 6, 10, 12, 0))
                .nextBooking(LocalDateTime.of(2025, 6, 20, 12, 0))
                .comments(List.of(comment))
                .build();

        String json = objectMapper.writeValueAsString(originalDto);
        var deserializedDto = objectMapper.readValue(json, ItemDto.class);

        assertThat(deserializedDto)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(originalDto);
    }

    @Test
    void testIgnoreIdOnDeserialization() throws Exception {
        String jsonContent = """
                {
                  "id": 999,
                  "name": "Laptop",
                  "description": "Gaming laptop",
                  "available": true,
                  "requestId": 10
                }
                """;

        var dto = objectMapper.readValue(jsonContent, ItemDto.class);

        assertThat(dto.getId()).isNull();
        assertThat(dto.getName()).isEqualTo("Laptop");
        assertThat(dto.getDescription()).isEqualTo("Gaming laptop");
    }
}
