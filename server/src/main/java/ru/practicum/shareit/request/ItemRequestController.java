package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @Valid @RequestBody CreateItemRequestDto request) {
        return requestService.create(userId, request.getDescription());
    }

    @GetMapping
    public List<ItemRequestDto> findByRequestor(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.findByRequestor(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                        @Positive @RequestParam(defaultValue = "10") int size) {
        return requestService.findAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto findById(@PathVariable Long requestId) {
        return requestService.findById(requestId);
    }
}
