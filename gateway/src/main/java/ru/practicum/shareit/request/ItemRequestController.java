package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

@RestController
@Validated
@RequestMapping("/requests")
public class ItemRequestController {
    private final ItemRequestClient requestClient;

    public ItemRequestController(ItemRequestClient requestClient) {
        this.requestClient = requestClient;
    }

    @PostMapping
    public ResponseEntity<Object> create(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
                                         @Valid @RequestBody CreateItemRequestDto request) {
        return requestClient.create(userId, request);
    }

    @GetMapping
    public ResponseEntity<Object> findByRequestor(@Positive @RequestHeader("X-Sharer-User-Id") long userId) {
        return requestClient.findByRequestor(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAll(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
                                          @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                          @Positive @RequestParam(defaultValue = "10") int size) {
        return requestClient.findAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findById(@Positive @PathVariable long requestId) {
        return requestClient.findById(requestId);
    }
}
