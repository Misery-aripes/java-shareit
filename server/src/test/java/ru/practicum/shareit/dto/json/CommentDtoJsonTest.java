package ru.practicum.shareit.dto.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerializeAndDeserialize() throws Exception {
        var originalDto = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("John Doe")
                .created(LocalDateTime.of(2025, 6, 15, 10, 0))
                .build();

        String json = objectMapper.writeValueAsString(originalDto);
        var deserializedDto = objectMapper.readValue(json, CommentDto.class);

        assertThat(deserializedDto)
                .usingRecursiveComparison()
                .isEqualTo(originalDto);
    }
}
