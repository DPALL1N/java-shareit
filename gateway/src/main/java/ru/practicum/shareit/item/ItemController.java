package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

@RestController
@Validated
@RequestMapping("/items")
public class ItemController {
    private final ItemClient itemClient;

    public ItemController(ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @PostMapping
    public ResponseEntity<Object> create(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
                                         @Valid @RequestBody CreateItemRequest request) {
        return itemClient.create(userId, request);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
                                         @Positive @PathVariable long itemId,
                                         @Valid @RequestBody UpdateItemRequest request) {
        return itemClient.update(userId, itemId, request);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(@Positive @PathVariable long itemId) {
        return itemClient.get(itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll(@Positive @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemClient.getAll(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text) {
        return itemClient.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> comment(@Positive @RequestHeader("X-Sharer-User-Id") long userId,
                                          @Positive @PathVariable long itemId,
                                          @Valid @RequestBody NewCommentRequest request) {
        return itemClient.comment(userId, itemId, request);
    }
}
