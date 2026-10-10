package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;

@RestController
@Validated
@RequestMapping("/users")
public class UserController {
    private final UserClient userClient;

    public UserController(UserClient userClient) {
        this.userClient = userClient;
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return userClient.getAll();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> get(@Positive @PathVariable long userId) {
        return userClient.get(userId);
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody NewUserRequest request) {
        return userClient.create(request);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@Positive @PathVariable long userId,
                                         @Valid @RequestBody UpdateUserRequest request) {
        return userClient.update(userId, request);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(@Positive @PathVariable long userId) {
        return userClient.delete(userId);
    }
}
