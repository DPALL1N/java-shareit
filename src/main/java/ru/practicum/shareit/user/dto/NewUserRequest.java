package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewUserRequest {
    private String name;
    @NotBlank(message = "Email должен быть указан")
    @Email(message = "Некорректный email")
    private String email;
}
