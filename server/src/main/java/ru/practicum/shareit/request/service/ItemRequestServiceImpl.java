package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public ItemRequestDto addItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        User user = getUser(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        itemRequest.setCreated(LocalDateTime.now());
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDto> getAllUserRequests(Long userId) {
        User user = getUser(userId);
        List<ItemRequestDto> itemRequestDtos = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(user.getId())
                .stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
        List<Long> itemRequestIds = itemRequestDtos.stream()
                .map(ItemRequestDto::getId)
                .toList();
        Map<Long, List<Item>> itemsMap = itemRepository.findAllByRequestIdIn(itemRequestIds)
                .stream()
                .collect(Collectors.groupingBy(o -> o.getRequest().getId()));

        for (ItemRequestDto itemRequestDto : itemRequestDtos) {
            itemRequestDto.setItems(ItemMapper.toItemResponseDto(itemsMap.getOrDefault(itemRequestDto.getId(), List.of())));
        }
        return itemRequestDtos;
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        User user = getUser(userId);
        return itemRequestRepository.findAll().stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        getUser(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с таким id не найден"));

        List<Item> items = itemRepository.findAllByRequestId(requestId);

        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        itemRequestDto.setItems(ItemMapper.toItemResponseDto(items));
        return itemRequestDto;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не найден"));
    }
}
