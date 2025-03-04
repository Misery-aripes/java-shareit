package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentNewDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
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

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User user = findUser(userId);
        Item item = ItemMapper.toItem(itemDto, user, null);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item item = findItem(itemId);
        validateUserIsOwner(userId, item);

        updateItemFields(item, itemDto);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getItem(Long userId, Long itemId) {
        Item item = findItem(itemId);
        ItemDto itemDto = ItemMapper.toItemDto(item);
        itemDto.setComments(getCommentsForItem(itemId));
        return itemDto;
    }

    @Override
    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        Item item = findItem(itemId);
        validateUserIsOwner(userId, item);
        itemRepository.delete(item);
    }

    @Override
    public List<ItemDto> getAllItems(Long userId) {
        List<Item> items = itemRepository.findByOwnerId(userId);
        Map<Long, List<CommentDto>> comments = getCommentsForItems(items);

        return items.stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toItemDto(item);
                    dto.setComments(comments.getOrDefault(item.getId(), List.of()));
                    setLastAndNextBooking(dto);
                    return dto;
                })
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.findByRequest(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentNewDto commentDto) {
        User user = findUser(userId);
        Item item = findItem(itemId);

        if (!canUserComment(userId, itemId)) {
            throw new RuntimeException("Нельзя оставить комментарий, если не бронировал предмет," +
                    " или бронирование не подтверждено");
        }

        Comment comment = CommentMapper.toComment(commentDto.getText(), item, user);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не найден"));
    }

    private Item findItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с таким id не найден"));
    }

    private void validateUserIsOwner(Long userId, Item item) {
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Редактировать предмет может только его владелец");
        }
    }

    private void updateItemFields(Item item, ItemDto itemDto) {
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
    }

    private boolean canUserComment(Long userId, Long itemId) {
        return bookingRepository.findAllByItemIdAndBookerId(itemId, userId).stream()
                .anyMatch(booking -> booking.getStatus() == BookingStatus.APPROVED && booking.getEnd().isBefore(LocalDateTime.now()));
    }

    private List<CommentDto> getCommentsForItem(Long itemId) {
        return commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    private Map<Long, List<CommentDto>> getCommentsForItems(List<Item> items) {
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        return commentRepository.findAllByItemIdIn(itemIds).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.groupingBy(CommentDto::getId));
    }

    private void setLastAndNextBooking(ItemDto itemDto) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findAllByItemIdOrderByStartAsc(itemDto.getId());

        LocalDateTime lastBooking = bookings.stream()
                .filter(booking -> booking.getStatus() != BookingStatus.REJECTED && booking.getEnd().isBefore(now))
                .map(Booking::getStart)
                .reduce((first, second) -> second)
                .orElse(null);

        LocalDateTime nextBooking = bookings.stream()
                .filter(booking -> booking.getStatus() != BookingStatus.REJECTED && booking.getStart().isAfter(now))
                .map(Booking::getEnd)
                .findFirst()
                .orElse(null);

        itemDto.setLastBooking(lastBooking);
        itemDto.setNextBooking(nextBooking);
    }
}
