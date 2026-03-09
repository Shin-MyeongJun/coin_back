package com.example.demo.infra_shard.json;


import com.jsoniter.JsonIterator;
import com.jsoniter.JsonIteratorPool;
import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.TypeLiteral;

import java.nio.charset.StandardCharsets;


public final class JsonUtil {

    private JsonUtil() {
        // Utility class
    }

    /**
     * 객체를 JSON 문자열로 변환합니다.
     */
    public static String toJson(Object obj) {
        try {
            return JsonStream.serialize(obj);
        } catch (Exception e) {
            throw new IllegalStateException("JSON 직렬화 실패", e);
        }
    }

    /**
     * JSON 문자열을 지정된 타입의 객체로 변환합니다.
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        JsonIterator iter = JsonIteratorPool.borrowJsonIterator();
        try {
            iter.reset(bytes, 0, bytes.length);
            return iter.read(clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 역직렬화 실패", e);
        } finally {
            JsonIteratorPool.returnJsonIterator(iter);
        }
    }

    /**
     * JSON 문자열을 제네릭 타입(TypeLiteral)으로 변환합니다.
     * 예: new TypeLiteral<List<MyDto>>() {}
     */
    public static <T> T fromJson(String json, TypeLiteral<T> typeLiteral) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        JsonIterator iter = JsonIteratorPool.borrowJsonIterator();
        try {
            iter.reset(bytes, 0, bytes.length);
            return iter.read(typeLiteral);
        } catch (Exception e) {
            throw new IllegalArgumentException("제네릭 JSON 역직렬화 실패", e);
        } finally {
            JsonIteratorPool.returnJsonIterator(iter);
        }
    }
}