package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestItemClient itemClient;

    @BeforeEach
    void setUp() {
        itemClient.reset();
    }

    @Test
    void returnsEmptySearchResultsWithoutCallingServerForBlankText() throws Exception {
        mockMvc.perform(get("/items/search").param("text", "  "))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        assertThat(itemClient.wasSearchCalled).isFalse();
    }

    @Test
    void forwardsNonBlankSearchToServer() throws Exception {
        mockMvc.perform(get("/items/search").param("text", "дрель"))
                .andExpect(status().isOk());

        assertThat(itemClient.wasSearchCalled).isTrue();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        TestItemClient itemClient() {
            return new TestItemClient();
        }
    }

    static class TestItemClient extends ItemClient {
        private boolean wasSearchCalled;

        TestItemClient() {
            super("http://localhost:9090", new RestTemplateBuilder());
        }

        void reset() {
            wasSearchCalled = false;
        }

        @Override
        public ResponseEntity<Object> search(String text) {
            wasSearchCalled = true;
            return ResponseEntity.ok().body(List.of());
        }
    }
}
