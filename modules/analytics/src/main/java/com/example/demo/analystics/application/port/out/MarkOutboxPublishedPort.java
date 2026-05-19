package com.example.demo.analystics.application.port.out;

public interface MarkOutboxPublishedPort {

    void markPublished(Long id, long publishedAt);

    void incrementRetry(Long id);
}
