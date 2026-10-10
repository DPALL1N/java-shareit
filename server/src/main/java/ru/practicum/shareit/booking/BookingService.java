package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;

import java.util.List;

public interface BookingService {
    BookingDto create(Long bookerId, CreateBookingRequest request);

    BookingDto approve(Long ownerId, Long bookingId, boolean approved);

    BookingDto findById(Long userId, Long bookingId);

    List<BookingDto> findByBooker(Long userId, BookingState state);

    List<BookingDto> findByOwner(Long userId, BookingState state);
}
