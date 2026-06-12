package com.example.demo.infre_exchange.client.impl;


import com.binance.connector.client.utils.WebSocketConnection;
import com.example.demo.infra_shard.connector.exchange.interfaces.ExchangeWebSocket;
import com.example.demo.infre_exchange.config.BinanceProperties;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Slf4j
public class BinanceFutureWebSocketImpl implements ExchangeWebSocket<String> {

    private  final BinanceProperties properties;
    private WebSocketConnection webSocket;
    private final OkHttpClient client = new OkHttpClient();


    public BinanceFutureWebSocketImpl(BinanceProperties properties){
        this.properties = properties;
    }

    @Override
    public void getTicker(List<String> marketCodes, Consumer<String> onMessage) {
        String steamName = properties.future().usdt().websocket().streamName().bookTicker();
        String url = buildMultiStreamUrl(marketCodes , steamName);
        Request request = new Request.Builder().url(url).build();
        start(request,onMessage);
    }

    @Override
    public void getOrderbook(List<String> marketCodes, Consumer<String> onMessage) {
        String steamName = properties.future().usdt().websocket().streamName().depth();
        String url = buildMultiStreamUrl(marketCodes , steamName);
        Request request = new Request.Builder().url(url).build();
        start(request,onMessage);
    }

    @Override
    public void getMyOrder(Consumer<String> onMessage) {

    }

    @Override
    public void getMyAsset(Consumer<String> onMessage) {

    }

    @Override
    public void disconnect() {
        if (webSocket != null){
            webSocket.close();
            webSocket = null;
        }
    }

    private void start(Request request,Consumer<String> onMessage){
        if (webSocket != null){
            webSocket.close();
            webSocket = null;
        }
        webSocket = new WebSocketConnection(
                response -> {
                    logWebSocket("WebSocket Connected");
                },
                onMessage::accept,
                (code, reason) -> logWebSocket("WebSocket Closing: " + code + " / " + reason),
                (code, reason) -> logWebSocket("WebSocket Closed: " + code + " / " + reason),
                (t, r) -> {
                    log.warn("WebSocket Error", t);
                },
                request,
                client
        );
        webSocket.connect();

    }



    private String buildMultiStreamUrl(List<String> symbols ,String streamName) {
        String streams = symbols.stream()
                .map(s -> s.toLowerCase() + streamName )
                .collect(Collectors.joining("/"));
        return "wss://fstream.binance.com/stream?streams=" + streams;
    }

    private void logWebSocket(String s) {
        log.info("[ShardManager] {}", s);
    }




}
