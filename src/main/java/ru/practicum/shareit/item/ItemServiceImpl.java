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
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this(itemRepository, userRepository, null, null);
    }

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           BookingRepository bookingRepository, CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public ItemDto create(Long userId, CreateItemRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));

        Item item = ItemMapper.mapToItem(request, owner);
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
        return itemRepository.findByOwnerId(userId).stream()
                .map(this::withBookingsAndComments)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text)
                .stream()
                .map(this::withComments)
                .collect(Collectors.toList());
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
        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return toCommentDto(commentRepository.save(comment));
    }

    private ItemDto withComments(Item item) {
        ItemDto dto = ItemMapper.mapToItemDto(item);
        if (commentRepository != null) {
            dto.setComments(commentRepository.findByItemIdOrderByCreatedDesc(item.getId()).stream()
                    .map(this::toCommentDto)
                    .toList());
        }
        return dto;
    }

    private ItemDto withBookingsAndComments(Item item) {
        ItemDto dto = withComments(item);
        if (bookingRepository != null) {
            LocalDateTime now = LocalDateTime.now();
            bookingRepository.findByItemIdAndStatusAndEndBeforeOrderByEndDesc(
                            item.getId(), BookingStatus.APPROVED, now).stream().findFirst()
                    .ifPresent(booking -> dto.setLastBooking(toShortDto(booking)));
            bookingRepository.findByItemIdAndStatusAndStartAfterOrderByStartAsc(
                            item.getId(), BookingStatus.APPROVED, now).stream().findFirst()
                    .ifPresent(booking -> dto.setNextBooking(toShortDto(booking)));
        }
        return dto;
    }

    private BookingShortDto toShortDto(Booking booking) {
        BookingShortDto dto = new BookingShortDto();
        dto.setId(booking.getId());
        dto.setBookerId(booking.getBooker().getId());
        return dto;
    }

    private CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setCreated(comment.getCreated());
        return dto;
    }
}
