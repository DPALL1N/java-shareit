package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<RequestItemDto> items = List.of();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestItemDto {
        private Long id;
        private String name;
        private Long ownerId;
    }
}
