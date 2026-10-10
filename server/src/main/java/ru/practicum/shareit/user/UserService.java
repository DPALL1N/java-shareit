package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

interface UserService {
    List<UserDto> getUsers();

    UserDto createUser(NewUserRequest request);

    UserDto updateUser(Long userId, UpdateUserRequest request);

    UserDto getUserById(Long userId);

    void deleteUser(Long userId);
}