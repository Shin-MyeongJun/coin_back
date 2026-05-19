package com.example.demo.analystics.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "analytics_outbox",
        indexes = {
                @Index(name = "idx_outbox_pending", columnList = "published_at, id")
        }
)
@SequenceGenerator(
        name = "analytics_outbox_seq_gen",
        sequenceName = "analytics_outbox_seq",
        allocationSize = 50
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsOutboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analytics_outbox_seq_gen")
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 32)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 128)
    private String aggregateId;

    @Column(name = "topic", nullable = false, length = 64)
    private String topic;

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "published_at")
    private Long publishedAt;
}
