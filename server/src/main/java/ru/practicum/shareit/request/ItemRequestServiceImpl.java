package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemJpaRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemJpaRepository itemRepository;

    @Override
    public ItemRequestDto create(Long userId, String description) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID: " + userId + " не найден"));
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return ItemRequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> findByRequestor(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID: " + userId + " не найден");
        }
        return toDtos(requestRepository.findByRequestorIdOrderByCreatedDescIdDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> findAll(Long userId, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID: " + userId + " не найден");
        }
        List<ItemRequest> requests = requestRepository.findByRequestorIdNotOrderByCreatedDescIdDesc(userId);
        if (from >= requests.size()) {
            return List.of();
        }
        int to = (int) Math.min((long) from + size, requests.size());
        return toDtos(requests.subList(from, to));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto findById(Long requestId) {
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с ID: " + requestId + " не найден"));
        List<Item> items = itemRepository.findByRequestIdInOrderByIdAsc(List.of(request.getId()));
        return ItemRequestMapper.toDto(request, items);
    }

    private List<ItemRequestDto> toDtos(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();
        Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdInOrderByIdAsc(requestIds).stream()
                .collect(Collectors.groupingBy(Item::getRequestId));
        return requests.stream()
                .map(request -> ItemRequestMapper.toDto(request,
                        itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList())))
                .toList();
    }
}
