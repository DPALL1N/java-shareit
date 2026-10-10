package ru.practicum.shareit.request;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestItemRequestClient requestClient;

    @BeforeEach
    void setUp() {
        requestClient.reset();
    }

    @Test
    void forwardsValidRequestCreationToServer() throws Exception {
        requestClient.nextResponse = ResponseEntity.status(HttpStatus.CREATED).build();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content("{\"description\":\"Нужна дрель\"}"))
                .andExpect(status().isCreated());

        assertThat(requestClient.wasCreateCalled).isTrue();
        assertThat(requestClient.lastUserId).isEqualTo(1L);
    }

    @Test
    void rejectsBlankDescriptionBeforeCallingServer() throws Exception {
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content("{\"description\":\"  \"}"))
                .andExpect(status().isBadRequest());

        assertThat(requestClient.wasCreateCalled).isFalse();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        TestItemRequestClient itemRequestClient() {
            return new TestItemRequestClient();
        }
    }

    static class TestItemRequestClient extends ItemRequestClient {
        private boolean wasCreateCalled;
        private long lastUserId;
        private ResponseEntity<Object> nextResponse;

        TestItemRequestClient() {
            super("http://localhost:9090", new RestTemplateBuilder());
        }

        void reset() {
            wasCreateCalled = false;
            lastUserId = 0L;
            nextResponse = null;
        }

        @Override
        public ResponseEntity<Object> create(long userId, Object body) {
            wasCreateCalled = true;
            lastUserId = userId;
            return nextResponse != null ? nextResponse : ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }
}
