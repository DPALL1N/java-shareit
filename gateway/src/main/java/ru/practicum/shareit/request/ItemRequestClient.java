package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/requests"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> create(long userId, Object body) {
        return post("", userId, body);
    }

    public ResponseEntity<Object> findByRequestor(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> findAll(long userId, int from, int size) {
        return get("/all?from={from}&size={size}", userId,
                Map.of("from", from, "size", size));
    }

    public ResponseEntity<Object> findById(long requestId) {
        return get("/" + requestId);
    }
}
