package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto create(Long bookerId, CreateBookingRequest request) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + bookerId + " не найден"));
        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с ID: " + request.getItemId() + " не найдена"));
        LocalDateTime now = LocalDateTime.now();
        if (request.getStart() == null || request.getEnd() == null
                || !request.getStart().isBefore(request.getEnd())
                || request.getStart().isBefore(now) || request.getEnd().isBefore(now)) {
            throw new BadRequestException("Укажите корректные даты бронирования в будущем");
        }
        if (item.getOwner().getId().equals(bookerId)) {
            throw new ConditionsNotMetException("Нельзя бронировать собственную вещь");
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }
        Booking booking = new Booking();
        booking.setStart(request.getStart());
        booking.setEnd(request.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        return toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = getBooking(bookingId);
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ConditionsNotMetException("Подтвердить бронирование может только владелец вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ConditionsNotMetException("Можно изменить только ожидающее бронирование");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto findById(Long userId, Long bookingId) {
        Booking booking = getBooking(bookingId);
        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ConditionsNotMetException("Просматривать бронирование могут только его автор и владелец вещи");
        }
        return toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> findByBooker(Long userId, BookingState state) {
        requireUser(userId);
        return filter(bookingRepository.findByBookerIdOrderByStartDesc(userId), state);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> findByOwner(Long userId, BookingState state) {
        requireUser(userId);
        return filter(bookingRepository.findByItemOwnerIdOrderByStartDesc(userId), state);
    }

    private List<BookingDto> filter(List<Booking> bookings, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(booking -> matches(booking, state, now))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .map(this::toDto)
                .toList();
    }

    private boolean matches(Booking booking, BookingState state, LocalDateTime now) {
        return switch (state) {
            case ALL -> true;
            case CURRENT -> !booking.getStart().isAfter(now) && !booking.getEnd().isBefore(now);
            case PAST -> booking.getEnd().isBefore(now);
            case FUTURE -> booking.getStart().isAfter(now);
            case WAITING -> booking.getStatus() == BookingStatus.WAITING;
            case REJECTED -> booking.getStatus() == BookingStatus.REJECTED;
        };
    }

    private Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID: " + bookingId + " не найдено"));
    }

    private void requireUser(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с ID: " + userId + " не найден");
        }
    }

    private BookingDto toDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setItem(ItemMapper.mapToItemDto(booking.getItem()));
        dto.setBooker(UserMapper.mapToUserDto(booking.getBooker()));
        dto.setStatus(booking.getStatus().name());
        return dto;
    }
}
