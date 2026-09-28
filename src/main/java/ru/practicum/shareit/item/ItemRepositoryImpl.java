package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private HashMap<Long, Item> items = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public Item save(Item item) {
        item.setId(nextId++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAll() {
        return items.values().stream().toList();
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        return items.values().stream().filter(item -> item.getOwner() != null && item.getOwner()
                .getId().equals(ownerId)).toList();
    }

    @Override
    public List<Item> search(String text) {
        String searchText = text.toLowerCase();

        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> containsIgnoreCase(item.getName(), searchText)
                        || containsIgnoreCase(item.getDescription(), searchText))
                .toList();
    }


    private boolean containsIgnoreCase(String value, String searchText) {
        return value != null && value.toLowerCase().contains(searchText);
    }
}
