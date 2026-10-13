package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public final class ItemRequestMapper {
    private ItemRequestMapper() {
    }

    public static ItemRequestDto toDto(ItemRequest request) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        return dto;
    }

    public static ItemRequestDto toDto(ItemRequest request, List<Item> items) {
        ItemRequestDto dto = toDto(request);
        dto.setItems(items.stream()
                .map(item -> new ItemRequestDto.RequestItemDto(
                        item.getId(), item.getName(), item.getOwner().getId()))
                .toList());
        return dto;
    }
}
