package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDto addItemRequest(@RequestHeader(HEADER_USER_ID) Long userId,
                                         @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.addItemRequest(userId, itemRequestDto);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(HEADER_USER_ID) Long userId) {
        return itemRequestService.getAllRequests(userId);
    }

    @GetMapping
    public List<ItemRequestDto> getAllUserRequests(@RequestHeader(HEADER_USER_ID) Long userId) {
        return itemRequestService.getAllUserRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(HEADER_USER_ID) Long userId,
                                         @PathVariable Long requestId) {
        return itemRequestService.getRequestById(userId, requestId);
    }

}
