package com.example.demo.contracts.message.chunk;

public record SnapshotChunkMeta(
        SnapshotType snapshotType,
        String snapshotId,
        long snapshotVersion,
        int chunkIndex,
        int chunkCount
) {}
