package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemJpaRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

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
        return toDto(requestRepository.save(request), true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> findByRequestor(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID: " + userId + " не найден");
        }
        return requestRepository.findByRequestorIdOrderByCreatedDescIdDesc(userId).stream()
                .map(request -> toDto(request, true))
                .toList();
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
        return requests.subList(from, to).stream()
                .map(request -> toDto(request, true))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto findById(Long requestId) {
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с ID: " + requestId + " не найден"));
        return toDto(request, true);
    }

    private ItemRequestDto toDto(ItemRequest request, boolean includeItems) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        if (includeItems) {
            List<ItemDto> items = itemRepository.findByRequestIdOrderByIdAsc(request.getId()).stream()
                    .map(ItemMapper::mapToItemDto)
                    .toList();
            dto.setItems(items.stream()
                    .map(item -> new ItemRequestDto.RequestItemDto(
                            item.getId(), item.getName(), item.getOwnerId()))
                    .toList());
        }
        return dto;
    }
}
