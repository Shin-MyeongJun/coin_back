package com.example.demo.meta_data.infrastructure.persistence.entity.embeddable;

import com.example.demo.meta_data.domain.ExchangeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class ExchangeKey {

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchange_type", nullable = false, length = 32)
    private ExchangeType exchangeType;

    @Column(name = "quote", nullable = false, length = 32)
    private String quote;

    @Column(name = "country", length = 64)
    private String country;
}
