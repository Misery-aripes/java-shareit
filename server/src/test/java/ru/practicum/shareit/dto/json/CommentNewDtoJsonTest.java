package ru.practicum.shareit.dto.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.CommentNewDto;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JsonTest
class CommentNewDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerializeAndDeserialize() throws Exception {
        var originalDto = new CommentNewDto("This is a great item!");

        String json = objectMapper.writeValueAsString(originalDto);
        var deserializedDto = objectMapper.readValue(json, CommentNewDto.class);

        assertThat(deserializedDto)
                .usingRecursiveComparison()
                .isEqualTo(originalDto);
    }
}
