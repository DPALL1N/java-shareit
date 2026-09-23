package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody CreateItemRequest createItemRequest) {
        return itemService.create(userId, createItemRequest);
    }

    @PatchMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId,
                          @RequestBody UpdateItemRequest updateItemRequest) {
        return itemService.update(userId, itemId, updateItemRequest);
    }

    @GetMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDto getItemById(@PathVariable Long itemId) {
        return itemService.findById(itemId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.findAllByOwner(userId);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }
}
