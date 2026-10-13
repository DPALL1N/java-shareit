package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestBookingClient bookingClient;

    @BeforeEach
    void setUp() {
        bookingClient.reset();
    }

    @Test
    void rejectsMissingBookingDatesBeforeCallingServer() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content("{\"itemId\":1}"))
                .andExpect(status().isBadRequest());

        assertThat(bookingClient.wasBookItemCalled).isFalse();
    }

    @Test
    void rejectsInvalidDateRangeBeforeCallingServer() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content("{\"itemId\":1,\"start\":\"2099-01-02T10:00:00\","
                                + "\"end\":\"2099-01-01T10:00:00\"}"))
                .andExpect(status().isBadRequest());

        assertThat(bookingClient.wasBookItemCalled).isFalse();
    }

    @Test
    void forwardsValidBookingToServer() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content("{\"itemId\":1,\"start\":\"2099-01-01T10:00:00\","
                                + "\"end\":\"2099-01-02T10:00:00\"}"))
                .andExpect(status().isCreated());

        assertThat(bookingClient.wasBookItemCalled).isTrue();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        TestBookingClient bookingClient() {
            return new TestBookingClient();
        }
    }

    static class TestBookingClient extends BookingClient {
        private boolean wasBookItemCalled;

        TestBookingClient() {
            super("http://localhost:9090", new RestTemplateBuilder());
        }

        void reset() {
            wasBookItemCalled = false;
        }

        @Override
        public ResponseEntity<Object> bookItem(long userId, BookItemRequestDto requestDto) {
            wasBookItemCalled = true;
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }
}
