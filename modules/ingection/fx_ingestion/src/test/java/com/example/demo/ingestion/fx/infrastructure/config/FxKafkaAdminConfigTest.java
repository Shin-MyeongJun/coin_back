package com.example.demo.ingestion.fx.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FxKafkaAdminConfigTest {

    @Test
    @DisplayName("FX Admin 토픽명은 publisher/consumer 토픽명과 일치한다")
    void fxTopicNameMatchesPipelineTopic() {
        FxKafkaAdminConfig sut = new FxKafkaAdminConfig();

        assertThat(sut.fxTopic().name()).isEqualTo("ingestion-fx.fx");
    }
}
