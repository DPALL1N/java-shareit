package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;
import ru.practicum.shareit.exception.BadRequestException;

import java.util.List;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @Valid @RequestBody CreateBookingRequest request) {
        return bookingService.create(userId, request);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long bookingId,
                              @RequestParam boolean approved) {
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @PathVariable Long bookingId) {
        return bookingService.findById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> findByBooker(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.findByBooker(userId, parseState(state));
    }

    @GetMapping("/owner")
    public List<BookingDto> findByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.findByOwner(userId, parseState(state));
    }

    private BookingState parseState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Неизвестное состояние бронирования: " + state);
        }
    }
}
