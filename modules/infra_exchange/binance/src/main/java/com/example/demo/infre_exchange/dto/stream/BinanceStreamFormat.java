package com.example.demo.infre_exchange.dto.stream;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public record BinanceStreamFormat<T>(String stream, T data) {}