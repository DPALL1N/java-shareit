package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTests {
    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void serializesBookingDatesAsIsoLocalDateTimes() throws Exception {
        BookingDto booking = new BookingDto();
        booking.setId(3L);
        booking.setStart(LocalDateTime.parse("2025-04-10T09:30:00"));
        booking.setEnd(LocalDateTime.parse("2025-04-12T18:45:00"));
        booking.setStatus("APPROVED");

        ItemDto item = new ItemDto();
        item.setId(7L);
        item.setName("Дрель");
        booking.setItem(item);

        UserDto booker = new UserDto();
        booker.setId(5L);
        booker.setName("Алекс");
        booking.setBooker(booker);

        assertThat(json.write(booking))
                .extractingJsonPathNumberValue("$.id").isEqualTo(3);
        assertThat(json.write(booking))
                .extractingJsonPathStringValue("$.start").isEqualTo("2025-04-10T09:30:00");
        assertThat(json.write(booking))
                .extractingJsonPathStringValue("$.end").isEqualTo("2025-04-12T18:45:00");
        assertThat(json.write(booking))
                .extractingJsonPathStringValue("$.item.name").isEqualTo("Дрель");
        assertThat(json.write(booking))
                .extractingJsonPathStringValue("$.booker.name").isEqualTo("Алекс");
    }
}
