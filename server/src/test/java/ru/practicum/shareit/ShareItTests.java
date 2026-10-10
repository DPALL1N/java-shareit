package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingRequest;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemJpaRepository;
import ru.practicum.shareit.item.ItemRepositoryImpl;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserJpaRepository;
import ru.practicum.shareit.user.UserRepositoryImpl;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ShareItTests {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserJpaRepository jpaUserRepository;

    @Autowired
    private ItemService jpaItemService;

    @Autowired
    private ItemJpaRepository jpaItemRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRequestService itemRequestService;

    private UserRepositoryImpl userRepository;
    private UserServiceImpl userService;
    private ItemRepositoryImpl itemRepository;
    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepositoryImpl();
        userService = new UserServiceImpl(userRepository);
        itemRepository = new ItemRepositoryImpl();
        itemService = new ItemServiceImpl(itemRepository, userRepository);

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
    void inMemoryItemRepositoryFiltersByOwnerAvailabilityAndBothSearchFields() {
        User owner = userRepository.findById(1L).orElseThrow();

        Item nameMatch = testItem("Дрель аккумуляторная", "Инструмент", true, owner);
        Item descriptionMatch = testItem("Пила", "Дрель пригодится при ремонте", true, owner);
        Item unavailableMatch = testItem("Дрель запасная", "Инструмент", false, owner);
        Item unownedMatch = testItem("Дрель", null, true, null);
        Item nullName = testItem(null, "Запасная дрель", true, owner);
        Item nullDescription = testItem("Молоток", null, true, owner);
        itemRepository.save(nameMatch);
        itemRepository.save(descriptionMatch);
        itemRepository.save(unavailableMatch);
        itemRepository.save(unownedMatch);
        itemRepository.save(nullName);
        itemRepository.save(nullDescription);

        assertEquals(6, itemRepository.findAll().size());
        assertEquals(5, itemRepository.findByOwnerId(owner.getId()).size());
        List<Item> searchResults = itemRepository.search("ДРЕЛЬ");
        assertTrue(searchResults.contains(nameMatch));
        assertTrue(searchResults.contains(descriptionMatch));
        assertTrue(searchResults.contains(unownedMatch));
        assertTrue(searchResults.contains(nullName));
        assertEquals(4, searchResults.size());
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

        UpdateItemRequest blankUpdate = new UpdateItemRequest();
        blankUpdate.setName(" ");
        blankUpdate.setDescription(" ");
        ItemMapper.updateItemFields(item, blankUpdate);
        assertEquals("Дрель", item.getName());
        assertEquals("Мощная", item.getDescription());
    }

    @Test
    void schemaCreatesRequiredTables() {
        assertEquals(0, jdbcTemplate.queryForObject("select count(*) from users", Integer.class));
        assertEquals(0, jdbcTemplate.queryForObject("select count(*) from items", Integer.class));
        assertEquals(0, jdbcTemplate.queryForObject("select count(*) from bookings", Integer.class));
        assertEquals(0, jdbcTemplate.queryForObject("select count(*) from comments", Integer.class));
    }

    @Test
    @Transactional
    void bookingAndCompletedRentalCommentPersistAndAppearOnItems() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-" + System.nanoTime() + "@yandex.ru");
        owner = jpaUserRepository.save(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker-" + System.nanoTime() + "@yandex.ru");
        booker = jpaUserRepository.save(booker);
        long ownerId = owner.getId();
        long bookerId = booker.getId();

        CreateItemRequest itemRequest = new CreateItemRequest();
        itemRequest.setName("Дрель");
        itemRequest.setDescription("Мощная дрель");
        itemRequest.setAvailable(true);
        ItemDto item = jpaItemService.create(owner.getId(), itemRequest);
        Item persistedItem = jpaItemRepository.findById(item.getId()).orElseThrow();

        LocalDateTime now = LocalDateTime.now();
        CreateBookingRequest request = new CreateBookingRequest();
        request.setItemId(item.getId());
        request.setStart(now.plusDays(1));
        request.setEnd(now.plusDays(2));

        CreateBookingRequest invalidDates = new CreateBookingRequest();
        invalidDates.setItemId(item.getId());
        invalidDates.setStart(now.plusDays(2));
        invalidDates.setEnd(now.plusDays(1));
        assertThrows(BadRequestException.class, () -> bookingService.create(bookerId, invalidDates));
        assertThrows(ConditionsNotMetException.class, () -> bookingService.create(ownerId, request));
        persistedItem.setAvailable(false);
        jpaItemRepository.save(persistedItem);
        assertThrows(BadRequestException.class, () -> bookingService.create(bookerId, request));
        persistedItem.setAvailable(true);
        jpaItemRepository.save(persistedItem);

        BookingDto booking = bookingService.create(bookerId, request);
        assertEquals("WAITING", booking.getStatus());
        assertThrows(ConditionsNotMetException.class,
                () -> bookingService.approve(bookerId, booking.getId(), true));
        assertThrows(ConditionsNotMetException.class,
                () -> bookingService.findById(ownerId + 100, booking.getId()));
        assertEquals("APPROVED", bookingService.approve(owner.getId(), booking.getId(), true).getStatus());
        assertEquals(1, bookingService.findByBooker(booker.getId(), BookingState.FUTURE).size());
        assertEquals(1, bookingService.findByOwner(owner.getId(), BookingState.FUTURE).size());

        Booking completed = bookingRepository.findById(booking.getId()).orElseThrow();
        completed.setEnd(now.minusDays(1));
        bookingRepository.save(completed);
        Booking olderCompleted = approvedBooking(persistedItem, booker, now.minusDays(4), now.minusDays(3));
        bookingRepository.save(olderCompleted);
        Booking upcoming = approvedBooking(persistedItem, booker, now.plusDays(3), now.plusDays(4));
        bookingRepository.save(upcoming);
        assertEquals(2, bookingService.findByBooker(booker.getId(), BookingState.PAST).size());
        assertEquals(2, bookingService.findByOwner(owner.getId(), BookingState.PAST).size());

        NewCommentRequest commentRequest = new NewCommentRequest();
        commentRequest.setText("Работает замечательно");
        CommentDto comment = jpaItemService.createComment(booker.getId(), item.getId(), commentRequest);
        assertEquals("Booker", comment.getAuthorName());

        ItemDto ownerItem = jpaItemService.findAllByOwner(owner.getId()).getFirst();
        assertNotNull(ownerItem.getLastBooking());
        assertEquals(booking.getId(), ownerItem.getLastBooking().getId());
        assertEquals(booking.getId(), ownerItem.getNextBooking().getId());
        assertEquals(1, ownerItem.getComments().size());
        assertEquals(1, jpaItemService.findById(item.getId()).getComments().size());
        assertEquals(1, jpaItemService.search("Дрель").getFirst().getComments().size());
    }

    @Test
    @Transactional
    void itemRequestsCanBeCreatedListedAndAnsweredWithItems() {
        User requestor = saveUser("requestor");
        User owner = saveUser("responder");
        User otherUser = saveUser("other");

        ItemRequestDto request = itemRequestService.create(requestor.getId(), "Нужна дрель");
        ItemRequestDto newerRequest = itemRequestService.create(requestor.getId(), "Нужна пила");

        CreateItemRequest createItem = validItem();
        createItem.setRequestId(request.getId());
        ItemDto item = jpaItemService.create(owner.getId(), createItem);

        assertEquals(request.getId(), item.getRequestId());
        assertEquals(2, itemRequestService.findByRequestor(requestor.getId()).size());
        assertEquals(newerRequest.getId(),
                itemRequestService.findByRequestor(requestor.getId()).getFirst().getId());
        assertEquals(1, itemRequestService.findById(request.getId()).getItems().size());
        assertEquals(item.getId(), itemRequestService.findById(request.getId()).getItems().getFirst().getId());
        assertEquals(2, itemRequestService.findAll(otherUser.getId(), 0, 10).size());
        assertEquals(item.getId(), itemRequestService.findAll(otherUser.getId(), 0, 10)
                .get(1).getItems().getFirst().getId());
        assertEquals(1, itemRequestService.findAll(owner.getId(), 0, 1).size());
        assertEquals(0, itemRequestService.findAll(requestor.getId(), 0, 10).size());
    }

    private User saveUser(String prefix) {
        User user = new User();
        user.setName(prefix);
        user.setEmail(prefix + "-" + System.nanoTime() + "@yandex.ru");
        return jpaUserRepository.save(user);
    }

    private CreateItemRequest validItem() {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("Дрель");
        request.setDescription("Мощная");
        request.setAvailable(true);
        return request;
    }

    private Item testItem(String name, String description, boolean available, User owner) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        return item;
    }

    private Booking approvedBooking(Item item, User booker, LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(BookingStatus.APPROVED);
        return booking;
    }
}
