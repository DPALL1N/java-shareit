package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, String description);

    List<ItemRequestDto> findByRequestor(Long userId);

    List<ItemRequestDto> findAll(Long userId, int from, int size);

    ItemRequestDto findById(Long requestId);
}
