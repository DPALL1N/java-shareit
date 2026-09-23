package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long userId, CreateItemRequest request) {
        if (request.getName() == null || request.getName().isBlank()
                || request.getDescription() == null
                || request.getDescription().isBlank()
                || request.getAvailable() == null) {
            throw new ConditionsNotMetException(
                    "Название, описание и доступность должны быть указаны");
        }

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = ItemMapper.mapToItem(request, owner);
        item = itemRepository.save(item);
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, UpdateItemRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Вещь не найдена");
        }
        ItemMapper.updateItemFields(item, request);
        item = itemRepository.update(item);
        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public ItemDto findById(Long itemId) {
        return itemRepository.findById(itemId)
                .map(ItemMapper::mapToItemDto)
                .orElseThrow(() -> new NotFoundException("Вещь с ID: " + itemId + "не найдена"));
    }

    @Override
    public List<ItemDto> findAllByOwner(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с ID: " + userId + " не найден");
        }
        return itemRepository.findByOwnerId(userId)
                .stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text)
                .stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }
}
