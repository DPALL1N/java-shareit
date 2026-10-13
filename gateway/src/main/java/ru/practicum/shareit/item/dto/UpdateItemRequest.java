package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateItemRequest {
    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    private Boolean available;
}
