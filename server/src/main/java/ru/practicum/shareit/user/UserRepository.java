package ru.practicum.shareit.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();

    User save(User user);

    Optional<User> findById(Long id);

    default boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    Optional<User> findByEmail(String email);

    User update(User user);

    void deleteById(Long userId);
}