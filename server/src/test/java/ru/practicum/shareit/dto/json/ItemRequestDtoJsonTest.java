package ru.practicum.shareit.dto.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerializeAndDeserialize() throws Exception {
        var itemResponse = new ItemResponseDto(10L, "Item name", 5L);

        var originalDto = new ItemRequestDto();
        originalDto.setId(1L);
        originalDto.setDescription("Need a laptop");
        originalDto.setCreated(LocalDateTime.of(2025, 6, 15, 10, 0));
        originalDto.setItems(List.of(itemResponse));

        String json = objectMapper.writeValueAsString(originalDto);
        var deserializedDto = objectMapper.readValue(json, ItemRequestDto.class);

        assertThat(deserializedDto)
                .usingRecursiveComparison()
                .isEqualTo(originalDto);
    }
}
