package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        itemDto.setRequestId(item.getRequest() != null ? item.getRequest().getId() : null);
        itemDto.setComments(CommentMapper.toCommentDto(item.getComments()));
        return itemDto;
    }

    public static Item toItem(ItemDto itemDto, User user, ItemRequest request) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(user);
        item.setRequest(request);
        return item;
    }

    public static List<ItemResponseDto> toItemResponseDto(List<Item> items) {
        if (items == null) {
            return List.of();
        }
        List<ItemResponseDto> itemResponseDtos = new ArrayList<>();
        for (Item item : items) {
            if (item != null) {
                itemResponseDtos.add(new ItemResponseDto(item.getId(), item.getName(), item.getOwner().getId()));
            }
        }
        return itemResponseDtos;
    }
}
