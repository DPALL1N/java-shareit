package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this(itemRepository, userRepository, null, null, null);
    }

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           BookingRepository bookingRepository, CommentRepository commentRepository,
                           ItemRequestRepository requestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public ItemDto create(Long userId, CreateItemRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));

        Item item = ItemMapper.mapToItem(request, owner);
        if (request.getRequestId() != null) {
            if (requestRepository == null) {
                throw new NotFoundException("Запрос с ID: " + request.getRequestId() + " не найден");
            }
            requestRepository.findById(request.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с ID: "
                            + request.getRequestId() + " не найден"));
        }
        item = itemRepository.save(item);
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, UpdateItemRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID: " + itemId + " не найдена"));
        if (!item.getOwner().getId().equals(userId)) {
            throw new ConditionsNotMetException("Пользователь не является владельцем вещи");
        }
        ItemMapper.updateItemFields(item, request);
        item = itemRepository.update(item);
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public ItemDto findById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID: " + itemId + " не найдена"));
        return withComments(item);
    }

    @Override
    public List<ItemDto> findAllByOwner(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с ID: " + userId + " не найден");
        }
        List<Item> items = itemRepository.findByOwnerId(userId);
        return enrichItems(items, true);
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return enrichItems(itemRepository.search(text), false);
    }

    @Override
    public CommentDto createComment(Long userId, Long itemId, NewCommentRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID: " + itemId + " не найдена"));
        if (bookingRepository == null || commentRepository == null
                || !bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now())) {
            throw new BadRequestException("Оставить отзыв может только пользователь после завершения бронирования");
        }
        Comment comment = CommentMapper.toComment(request, item, author);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private ItemDto withComments(Item item) {
        ItemDto dto = ItemMapper.mapToItemDto(item);
        if (commentRepository != null) {
            dto.setComments(commentRepository.findByItemIdOrderByCreatedDesc(item.getId()).stream()
                    .map(CommentMapper::toCommentDto)
                    .toList());
        }
        return dto;
    }

    private List<ItemDto> enrichItems(List<Item> items, boolean includeBookings) {
        if (items.isEmpty()) {
            return List.of();
        }
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        Map<Long, List<Booking>> bookingsByItem = includeBookings && bookingRepository != null
                ? bookingRepository.findByItemIdInAndStatusOrderByStartDesc(itemIds, BookingStatus.APPROVED)
                .stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()))
                : Collections.emptyMap();
        Map<Long, List<Comment>> commentsByItem = commentRepository == null
                ? Collections.emptyMap()
                : commentRepository.findByItemIdInOrderByCreatedDesc(itemIds).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
        LocalDateTime now = LocalDateTime.now();
        return items.stream()
                .map(item -> toItemDto(item,
                        bookingsByItem.getOrDefault(item.getId(), List.of()),
                        commentsByItem.getOrDefault(item.getId(), List.of()), now))
                .toList();
    }

    private ItemDto toItemDto(Item item, List<Booking> bookings, List<Comment> comments, LocalDateTime now) {
        ItemDto dto = ItemMapper.mapToItemDto(item);
        dto.setComments(comments.stream().map(CommentMapper::toCommentDto).toList());
        bookings.stream()
                .filter(booking -> booking.getEnd().isBefore(now))
                .max((first, second) -> first.getStart().compareTo(second.getStart()))
                .map(this::toBookingShortDto)
                .ifPresent(dto::setLastBooking);
        bookings.stream()
                .filter(booking -> booking.getStart().isAfter(now))
                .min((first, second) -> first.getStart().compareTo(second.getStart()))
                .map(this::toBookingShortDto)
                .ifPresent(dto::setNextBooking);
        return dto;
    }

    private BookingShortDto toBookingShortDto(Booking booking) {
        BookingShortDto dto = new BookingShortDto();
        dto.setId(booking.getId());
        dto.setBookerId(booking.getBooker().getId());
        return dto;
    }
}
