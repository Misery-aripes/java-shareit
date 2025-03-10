package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentNewDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User user = findUser(userId);
        Item item;
        ItemRequest itemRequest;
        if (itemDto.getRequestId() == null) {
            item = ItemMapper.toItem(itemDto, user, null);
        } else {
            itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с таким id не найден"));
            item = ItemMapper.toItem(itemDto, user, itemRequest);
        }

        itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        User user = findUser(userId);
        Item updatedItem = findItem(itemId);
        if (!updatedItem.getOwner().equals(user)) {
            throw new NotFoundException("Редактировать предмет может только его владелец");
        }

        if (itemDto.getName() != null) {
            updatedItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            updatedItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            updatedItem.setAvailable(itemDto.getAvailable());
        }

        itemRepository.save(updatedItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItem(Long userId, Long itemId) {
        Item item = findItem(itemId);
        ItemDto itemDto = ItemMapper.toItemDto(item);
        itemDto.setComments(commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .toList());
        return itemDto;
    }

    @Override
    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        Item item = findItem(itemId);
        itemRepository.delete(item);
    }

    @Override
    public List<ItemDto> getAllItems(Long userId) {
        List<Item> items = itemRepository.findByOwnerId(userId);
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        Map<Long, List<Comment>> comments = commentRepository.findAllByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(o -> o.getItem().getId()));

        items.forEach(item -> item.setComments(comments.getOrDefault(item.getId(), List.of())));

        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toItemDto)
                .toList();

        for (ItemDto itemDto : itemDtos) {
            setLastAndNextBooking(itemDto);
        }

        return itemDtos;
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<Item> items = itemRepository.findByRequest(text);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentNewDto commentDto) {
        User user = findUser(userId);
        Item item = findItem(itemId);

        List<Booking> bookings = bookingRepository.findAllByItemIdAndBookerId(itemId, userId);

        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.APPROVED && booking.getEnd().isBefore(LocalDateTime.now())) {
                Comment comment = CommentMapper.toComment(commentDto.getText(), item, user);
                return CommentMapper.toCommentDto(commentRepository.save(comment));
            }
        }

        throw new RuntimeException("Предмет занят в указанные даты");
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не найден"));
    }

    private Item findItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с таким id не найден"));
    }

    private void setLastAndNextBooking(ItemDto itemDto) {
        LocalDateTime lastBooking = null;
        LocalDateTime nextBooking = null;

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findAllByItemIdOrderByStartAsc(itemDto.getId());
        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.REJECTED) {
                return;
            }
            if (booking.getEnd().isBefore(now)) {
                lastBooking = booking.getStart();
            }

            if (booking.getStart().isAfter(now)) {
                nextBooking = booking.getEnd();
                break;
            }
        }

        itemDto.setLastBooking(lastBooking);
        itemDto.setNextBooking(nextBooking);
    }
}
