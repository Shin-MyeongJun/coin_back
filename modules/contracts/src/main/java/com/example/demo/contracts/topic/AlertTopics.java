package com.example.demo.contracts.topic;

/**
 * Alert 도메인 Kafka 토픽 상수.
 * <p>
 * 토픽 네이밍 규칙: {@code {소스모듈}.{데이터타입}}.
 * <p>
 * NewTopic Spring Bean 정의는 alert 모듈 생성 시점에 해당 모듈의
 * {@code infrastructure.messaging.config}에서 별도 등록한다.
 */
public final class AlertTopics {

    public static final String ALERT_FIRING = "alert.firing";

    private AlertTopics() {}
}
