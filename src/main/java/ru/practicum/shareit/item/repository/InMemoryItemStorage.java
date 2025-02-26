package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();
    private final UserStorage userStorage;
    private Long id = 1L;

    @Override
    public Item createItem(Long userId, Item item) {
        User owner = userStorage.getUser(userId);

        if (item.getName() == null || item.getName().trim().isEmpty() ||
                item.getDescription() == null || item.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Поля name и description не могут быть пустыми");
        }

        item.setId(id++);
        item.setOwner(owner);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item getItem(Long userId, Long itemId) {
        return getItemOrThrow(itemId);
    }

    @Override
    public Item updateItem(Long userId, Long itemId, Item item) {
        Item updatedItem = getItemOrThrow(itemId);

        if (item.getName() != null) {
            updatedItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            updatedItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            updatedItem.setAvailable(item.getAvailable());
        }

        items.put(itemId, updatedItem);
        return updatedItem;
    }

    @Override
    public List<Item> getAllItems(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .toList();
    }

    @Override
    public List<Item> searchItems(Long userId, String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String textToLowerCase = text.toLowerCase();
        return items.values().stream()
                .filter(item ->
                        (item.getDescription().toLowerCase().contains(textToLowerCase)
                                || item.getName().toLowerCase().contains(textToLowerCase))
                                && item.getOwner().getId().equals(userId)
                                && Boolean.TRUE.equals(item.getAvailable())
                )
                .toList();
    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        items.remove(itemId);
    }

    private Item getItemOrThrow(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("Предмет с таким id не найден");
        }
        return item;
    }
}
