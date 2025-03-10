package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto addItemRequest(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getAllUserRequests(Long userId);

    List<ItemRequestDto> getAllRequests(Long userId);

    ItemRequestDto getRequestById(Long userId, Long requestId);
}
