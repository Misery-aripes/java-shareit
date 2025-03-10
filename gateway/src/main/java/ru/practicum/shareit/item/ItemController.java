package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader(HEADER_USER_ID) Long userId,
                                          @Valid @RequestBody ItemDto itemDto) {
        return itemClient.addItem(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader(HEADER_USER_ID) Long userId,
                                         @PathVariable Long itemId) {
        return itemClient.getItem(userId, itemId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> patchItem(@RequestHeader(HEADER_USER_ID) Long userId,
                                           @PathVariable Long itemId,
                                           @RequestBody ItemDto itemDto) {
        return itemClient.patchItem(userId, itemId, itemDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItems(@RequestHeader(HEADER_USER_ID) Long userId) {
        return itemClient.getAllItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@NotBlank @RequestParam String text) {
        return itemClient.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(HEADER_USER_ID) Long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody CommentDto commentDto) {
        return itemClient.addComment(userId, itemId, commentDto);
    }
}
