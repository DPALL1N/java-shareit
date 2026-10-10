package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;


@Controller
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {
	private static final Logger log = LoggerFactory.getLogger(BookingController.class);
	private final BookingClient bookingClient;

	public BookingController(BookingClient bookingClient) {
		this.bookingClient = bookingClient;
	}

	@GetMapping
	public ResponseEntity<Object> getBookings(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
			@RequestParam(name = "state", defaultValue = "all") String stateParam,
			@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
			@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Unknown state: " + stateParam));
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getBookings(userId, state, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getOwnerBookings(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
			@RequestParam(name = "state", defaultValue = "all") String stateParam) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Unknown state: " + stateParam));
		return bookingClient.getOwnerBookings(userId, state);
	}

	@PostMapping
	public ResponseEntity<Object> bookItem(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
			@RequestBody @Valid BookItemRequestDto requestDto) {
		log.info("Creating booking {}, userId={}", requestDto, userId);
		return bookingClient.bookItem(userId, requestDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approveBooking(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
			@Positive @PathVariable Long bookingId, @RequestParam boolean approved) {
		return bookingClient.approveBooking(userId, bookingId, approved);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
			@Positive @PathVariable Long bookingId) {
		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.getBooking(userId, bookingId);
	}
}
