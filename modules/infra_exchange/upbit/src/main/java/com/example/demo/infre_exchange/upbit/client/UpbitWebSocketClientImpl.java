
package com.example.demo.infre_exchange.upbit.client;


import com.example.demo.infra_shard.connector.exchange.interfaces.ExchangeWebSocket;
import com.example.demo.infre_exchange.upbit.config.UpbitProperties;
import com.example.demo.infre_exchange.upbit.util.UpbitAuthTokenProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okio.ByteString;

import java.util.*;
import java.util.function.Consumer;


public class UpbitWebSocketClientImpl implements ExchangeWebSocket<ByteString> {

    private final UpbitProperties props;
    private final UpbitAuthTokenProvider authTokenProvider;
    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .build();;
    private WebSocket webSocket;
    private final  ObjectMapper objectMapper;





    public UpbitWebSocketClientImpl(UpbitProperties props, UpbitAuthTokenProvider authTokenProvider, ObjectMapper objectMapper) {
        this.props = props;
        this.authTokenProvider = authTokenProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public void getTicker(List<String> marketCodes, Consumer<ByteString> onMessage) {
        String payload = buildPayload("ticker", marketCodes, false, false);
        connectWebSocket(props.getWebsockets().getPublicUrl(), payload, onMessage, null);
    }

    @Override
    public void getOrderbook(List<String> marketCodes, Consumer<ByteString> onMessage) {
        String payload = buildPayload("orderbook", marketCodes, false, false);
        connectWebSocket(props.getWebsockets().getPublicUrl(), payload, onMessage, null);
    }

    @Override
    public void getMyOrder(Consumer<ByteString> onMessage) {
        String payload = buildPayload("myOrder", null, false, false);
        String jwt = authTokenProvider.createToken(null);
        connectWebSocket(props.getWebsockets().getPrivateUrl(), payload, onMessage, jwt);
    }

    @Override
    public void getMyAsset(Consumer<ByteString> onMessage) {
        String payload = buildPayload("myAsset", null, false, false);
        String jwt = authTokenProvider.createToken(null);
        connectWebSocket(props.getWebsockets().getPrivateUrl(), payload, onMessage, jwt);
    }

    @Override
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000,"");
            webSocket = null;
        }
    }

    private void connectWebSocket(String url, String payload, Consumer<ByteString> onMessage, String jwt) {
        Request.Builder builder = new Request.Builder().url(url);
        if (jwt != null) {
            builder.addHeader("Authorization", "Bearer " + jwt);
        }

        Request request = builder.build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                webSocket.send(payload);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                onMessage.accept(bytes);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                System.err.println("WebSocket failed: " + t.getMessage());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                System.out.println(code);
                System.out.println(reason);
            }

        });

    }



    private String buildPayload(String type, List<String> codes, boolean isOnlySnapshot, boolean isOnlyRealtime) {
        List<Map<String, Object>> payload = new ArrayList<>();

        Map<String, Object> ticket = new HashMap<>();
        ticket.put("ticket", UUID.randomUUID().toString());
        payload.add(ticket);

        Map<String, Object> typeField = new LinkedHashMap<>();
        typeField.put("type", type);
        if (codes != null && !codes.isEmpty()) {
            typeField.put("codes", codes);
        }
        if (isOnlySnapshot) typeField.put("isOnlySnapshot", true);
        if (isOnlyRealtime) typeField.put("isOnlyRealtime", true);
        payload.add(typeField);

        Map<String, Object> format = new HashMap<>();
        format.put("format", "SIMPLE");
        payload.add(format);

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to build payload", e);
        }
    }

}