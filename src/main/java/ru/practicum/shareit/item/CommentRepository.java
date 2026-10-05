package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = "author")
    List<Comment> findByItemIdOrderByCreatedDesc(Long itemId);

    @EntityGraph(attributePaths = "author")
    List<Comment> findByItemIdInOrderByCreatedDesc(List<Long> itemIds);
}
