package com.example.demo.contracts.message.chunk.message;

import com.example.demo.contracts.message.chunk.SnapshotChunkMeta;
import com.example.demo.contracts.message.meta.MarketCodeMessage;

import java.util.List;

public record MarketCodeSnapshotChunkMessage(
        SnapshotChunkMeta meta,
        List<MarketCodeMessage> messages
) {
}
