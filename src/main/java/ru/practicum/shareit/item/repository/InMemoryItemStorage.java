package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();
    private Long id = 1L;

    @Override
    public Item createItem(Long userId, Item item) {
        item.setId(id++);
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

        updatedItem.setName(item.getName());
        updatedItem.setDescription(item.getDescription());
        updatedItem.setAvailable(item.getAvailable());

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
