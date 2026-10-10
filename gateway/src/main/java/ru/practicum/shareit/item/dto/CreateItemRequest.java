package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateItemRequest {
    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @NotNull
    private Boolean available;

    private Long requestId;
}
