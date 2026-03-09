package com.example.demo.contracts.message.chunk.message;

import com.example.demo.contracts.message.chunk.SnapshotChunkMeta;
import com.example.demo.contracts.message.meta.ExchangeMessage;

import java.util.List;

public record ExchangeSnapshotChunkMessage(
        SnapshotChunkMeta meta,
        List<ExchangeMessage> messages
) {

}
