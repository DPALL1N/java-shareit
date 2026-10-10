package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewCommentRequest {
    @NotBlank
    @Size(max = 1000)
    private String text;
}
