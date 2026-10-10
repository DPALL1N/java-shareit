package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        BookingController.class,
        ItemController.class,
        ItemRequestController.class,
        UserController.class
})
class RestApiControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private ItemRequestService itemRequestService;

    @MockBean
    private UserService userService;

    @Test
    void bookingEndpointsBindHeadersBodiesAndStateFilters() throws Exception {
        BookingDto booking = bookingDto();
        when(bookingService.create(eq(1L), any(CreateBookingRequest.class))).thenReturn(booking);
        when(bookingService.approve(2L, 3L, true)).thenReturn(booking);
        when(bookingService.findById(1L, 3L)).thenReturn(booking);
        when(bookingService.findByBooker(1L, BookingState.FUTURE)).thenReturn(List.of(booking));
        when(bookingService.findByOwner(2L, BookingState.ALL)).thenReturn(List.of(booking));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":4,\"start\":\"2099-05-01T10:00:00\","
                                + "\"end\":\"2099-05-02T10:00:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.start").value("2099-05-01T10:00:00"));
        mockMvc.perform(patch("/bookings/3")
                        .header("X-Sharer-User-Id", 2)
                        .param("approved", "true"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/bookings/3").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        mockMvc.perform(get("/bookings").header("X-Sharer-User-Id", 1).param("state", "future"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk());
        mockMvc.perform(get("/bookings").header("X-Sharer-User-Id", 1).param("state", "invalid"))
                .andExpect(status().isBadRequest());

        verify(bookingService).create(eq(1L), any(CreateBookingRequest.class));
        verify(bookingService).approve(2L, 3L, true);
        verify(bookingService).findById(1L, 3L);
        verify(bookingService).findByBooker(1L, BookingState.FUTURE);
        verify(bookingService).findByOwner(2L, BookingState.ALL);
    }

    @Test
    void itemEndpointsBindHeadersBodiesAndSearchParameters() throws Exception {
        ItemDto item = itemDto();
        CommentDto comment = new CommentDto();
        comment.setId(8L);
        comment.setText("Works well");
        comment.setAuthorName("Booker");
        comment.setCreated(LocalDateTime.parse("2025-01-02T12:30:00"));
        when(itemService.create(eq(1L), any(CreateItemRequest.class))).thenReturn(item);
        when(itemService.update(eq(1L), eq(4L), any(UpdateItemRequest.class))).thenReturn(item);
        when(itemService.findById(4L)).thenReturn(item);
        when(itemService.findAllByOwner(1L)).thenReturn(List.of(item));
        when(itemService.search("дрель")).thenReturn(List.of(item));
        when(itemService.createComment(eq(2L), eq(4L), any(NewCommentRequest.class))).thenReturn(comment);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель\",\"description\":\"Мощная\",\"available\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value("Дрель"));
        mockMvc.perform(patch("/items/4")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель обновлена\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/items/4")).andExpect(status().isOk());
        mockMvc.perform(get("/items").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerId").value(1));
        mockMvc.perform(get("/items/search").param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель"));
        mockMvc.perform(post("/items/4/comment")
                        .header("X-Sharer-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Работает хорошо\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorName").value("Booker"));
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \",\"description\":\"\",\"available\":true}"))
                .andExpect(status().isBadRequest());

        verify(itemService).create(eq(1L), any(CreateItemRequest.class));
        verify(itemService).update(eq(1L), eq(4L), any(UpdateItemRequest.class));
        verify(itemService).findById(4L);
        verify(itemService).findAllByOwner(1L);
        verify(itemService).search("дрель");
        verify(itemService).createComment(eq(2L), eq(4L), any(NewCommentRequest.class));
    }

    @Test
    void itemRequestEndpointsBindPaginationAndValidateParameters() throws Exception {
        ItemRequestDto request = new ItemRequestDto();
        request.setId(6L);
        request.setDescription("Нужна дрель");
        request.setCreated(LocalDateTime.parse("2025-02-03T09:00:00"));
        when(itemRequestService.create(1L, "Нужна дрель")).thenReturn(request);
        when(itemRequestService.findByRequestor(1L)).thenReturn(List.of(request));
        when(itemRequestService.findAll(1L, 0, 10)).thenReturn(List.of(request));
        when(itemRequestService.findById(6L)).thenReturn(request);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Нужна дрель\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(6));
        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", 1)
                        .param("from", "0").param("size", "10"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/requests/6")).andExpect(status().isOk());
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\" \"}"))
                .andExpect(status().isBadRequest());

        verify(itemRequestService).create(1L, "Нужна дрель");
        verify(itemRequestService).findByRequestor(1L);
        verify(itemRequestService).findAll(1L, 0, 10);
        verify(itemRequestService).findById(6L);
    }

    @Test
    void userEndpointsBindRequestsAndReturnExpectedStatuses() throws Exception {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("Алекс");
        user.setEmail("alex@yandex.ru");
        when(userService.getUsers()).thenReturn(List.of(user));
        when(userService.getUserById(1L)).thenReturn(user);
        when(userService.createUser(any(NewUserRequest.class))).thenReturn(user);
        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(user);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("alex@yandex.ru"));
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Алекс"));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Алекс\",\"email\":\"alex@yandex.ru\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Алекс обновлен\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/users/1")).andExpect(status().isNoContent());
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest());

        verify(userService).getUsers();
        verify(userService).getUserById(1L);
        verify(userService).createUser(any(NewUserRequest.class));
        verify(userService).updateUser(eq(1L), any(UpdateUserRequest.class));
        verify(userService).deleteUser(1L);
    }

    private BookingDto bookingDto() {
        BookingDto booking = new BookingDto();
        booking.setId(3L);
        booking.setStart(LocalDateTime.parse("2099-05-01T10:00:00"));
        booking.setEnd(LocalDateTime.parse("2099-05-02T10:00:00"));
        booking.setItem(itemDto());
        booking.setBooker(new UserDto());
        booking.setStatus("APPROVED");
        return booking;
    }

    private ItemDto itemDto() {
        ItemDto item = new ItemDto();
        item.setId(4L);
        item.setName("Дрель");
        item.setDescription("Мощная");
        item.setAvailable(true);
        item.setOwnerId(1L);
        return item;
    }
}
