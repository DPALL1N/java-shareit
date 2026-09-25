package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepositoryImpl;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepositoryImpl;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShareItTests {
    private UserRepositoryImpl userRepository;
    private UserServiceImpl userService;
    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepositoryImpl();
        userService = new UserServiceImpl(userRepository);
        itemService = new ItemServiceImpl(new ItemRepositoryImpl(), userRepository);

        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@yandex.ru");
        userRepository.save(owner);
    }

    @Test
    void shouldCreateUpdateGetAndDeleteUser() {
        NewUserRequest create = new NewUserRequest();
        create.setName("Ivan");
        create.setEmail("ivan@yandex.ru");

        assertEquals("Ivan", userService.createUser(create).getName());

        UpdateUserRequest update = new UpdateUserRequest();
        update.setName("Petr");
        update.setEmail("petr@yandex.ru");
        assertEquals("Petr", userService.updateUser(2L, update).getName());

        assertEquals(2, userService.getUsers().size());
        assertEquals("Petr", userService.getUserById(2L).getName());

        userService.deleteUser(2L);
        assertThrows(NotFoundException.class, () -> userService.getUserById(2L));
    }

    @Test
    void shouldRejectDuplicateUserEmail() {
        NewUserRequest request = new NewUserRequest();
        request.setEmail("duplicate@yandex.ru");
        userService.createUser(request);

        NewUserRequest duplicate = new NewUserRequest();
        duplicate.setEmail("duplicate@yandex.ru");

        assertThrows(DuplicatedDataException.class,
                () -> userService.createUser(duplicate));
    }

    @Test
    void shouldCreateUpdateAndSearchItem() {
        CreateItemRequest create = new CreateItemRequest();
        create.setName("Дрель");
        create.setDescription("Мощная");
        create.setAvailable(true);

        ItemDto item = itemService.create(1L, create);
        assertEquals("Дрель", item.getName());
        assertEquals(true, item.getAvailable());
        assertEquals(1, itemService.search("ДРЕЛЬ").size());

        UpdateItemRequest update = new UpdateItemRequest();
        update.setName("Молоток");
        update.setAvailable(false);
        assertEquals("Молоток", itemService.update(1L, 1L, update).getName());
        assertEquals(0, itemService.search("молоток").size());
    }

    @Test
    void shouldRejectInvalidItemOperations() {
        assertThrows(NotFoundException.class, () -> itemService.create(99L, validItem()));
        assertThrows(NotFoundException.class, () -> itemService.findById(99L));
    }

    @Test
    void shouldMapItemAndUpdateFields() {
        User owner = userRepository.findById(1L).orElseThrow();
        Item item = ItemMapper.mapToItem(validItem(), owner);
        item.setId(1L);

        ItemDto dto = ItemMapper.mapToItemDto(item);
        assertEquals("Дрель", dto.getName());
        assertEquals(1L, dto.getOwnerId());

        UpdateItemRequest update = new UpdateItemRequest();
        update.setAvailable(false);
        ItemMapper.updateItemFields(item, update);
        assertEquals(false, item.getAvailable());
    }

    private CreateItemRequest validItem() {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("Дрель");
        request.setDescription("Мощная");
        request.setAvailable(true);
        return request;
    }
}
