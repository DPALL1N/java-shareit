package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
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
        if (!request.getStart().isBefore(request.getEnd())) {
            throw new BadRequestException("Дата начала бронирования должна быть раньше даты окончания");
        }
        if (item.getOwner().getId().equals(bookerId)) {
            throw new ConditionsNotMetException("Нельзя бронировать собственную вещь");
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }
        Booking booking = BookingMapper.toBooking(request, item, booker);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
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
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto findById(Long userId, Long bookingId) {
        Booking booking = getBooking(bookingId);
        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ConditionsNotMetException("Просматривать бронирование могут только его автор и владелец вещи");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> findByBooker(Long userId, BookingState state) {
        requireUser(userId);
        return bookingRepository.findByBookerAndState(userId, state.name(), LocalDateTime.now()).stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> findByOwner(Long userId, BookingState state) {
        requireUser(userId);
        return bookingRepository.findByOwnerAndState(userId, state.name(), LocalDateTime.now()).stream()
                .map(BookingMapper::toBookingDto)
                .toList();
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
}
