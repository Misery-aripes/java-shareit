package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User user = getUserOrThrow(userId);
        Item item = ItemMapper.toItem(itemDto, user);
        Item savedItem = itemStorage.createItem(userId, item);

        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        User user = getUserOrThrow(userId);
        Item item = ItemMapper.toItem(itemDto, user);

        Item updatedItem = itemStorage.updateItem(userId, itemId, item);

        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItem(Long userId, Long itemId) {
        getUserOrThrow(userId);
        Item foundItem = itemStorage.getItem(userId, itemId);

        return ItemMapper.toItemDto(foundItem);
    }

    @Override
    public List<ItemDto> getAllItems(Long userId) {
        getUserOrThrow(userId);

        List<Item> allItems = itemStorage.getAllItems(userId);

        return allItems.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(Long userId, String text) {
        getUserOrThrow(userId);

        List<Item> foundItems = itemStorage.searchItems(userId, text);

        return foundItems.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        itemStorage.deleteItem(userId, itemId);
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.getUser(userId);
    }
}
