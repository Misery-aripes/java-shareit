package ru.practicum.shareit.dto.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerializeAndDeserialize() throws Exception {
        var originalDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        String json = objectMapper.writeValueAsString(originalDto);
        var deserializedDto = objectMapper.readValue(json, UserDto.class);

        assertThat(deserializedDto)
                .usingRecursiveComparison()
                .isEqualTo(originalDto);
    }
}
