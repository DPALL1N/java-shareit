package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryDb implements ItemRepository {
    private final ItemJpaRepository repository;

    @Override
    public Item save(Item item) {
        return repository.save(item);
    }

    @Override
    public Item update(Item item) {
        return repository.save(item);
    }

    @Override
    public Optional<Item> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Item> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        return repository.findByOwnerId(ownerId);
    }

    @Override
    public List<Item> search(String text) {
        return repository.search(text);
    }
}
