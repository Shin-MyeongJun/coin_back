package com.example.demo.analystics.infrastructure.cache.key_codec.base;

public interface DataKeyCodec<KEY> {
    byte[] encode(KEY key);
    KEY decode(byte[] bytes);
}
