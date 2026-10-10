package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(min = 1, max = 255)
    private String name;

    @Email
    @Size(max = 512)
    private String email;
}
