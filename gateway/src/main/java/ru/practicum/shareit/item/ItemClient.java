package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/items"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> create(long userId, Object body) {
        return post("", userId, body);
    }

    public ResponseEntity<Object> update(long userId, long itemId, Object body) {
        return patch("/" + itemId, userId, body);
    }

    public ResponseEntity<Object> get(long itemId) {
        return get("/" + itemId);
    }

    public ResponseEntity<Object> getAll(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> search(String text) {
        return get("/search?text={text}", null, Map.of("text", text));
    }

    public ResponseEntity<Object> comment(long userId, long itemId, Object body) {
        return post("/" + itemId + "/comment", userId, body);
    }
}
