package com.example.demo.infra_shard.paging;

/**
 * 시계열 cursor 페이징 방향.
 * <ul>
 *   <li>{@link #BACKWARD} - cursor 이전(=더 과거) 데이터. 무한 스크롤 위로 이동 시 사용. 기본값.</li>
 *   <li>{@link #FORWARD}  - cursor 이후(=더 미래) 데이터. 실시간 fill-in 또는 재연결 보충에 사용.</li>
 * </ul>
 */
public enum CursorDirection {
    BACKWARD,
    FORWARD;

    /**
     * 문자열 → enum 변환. null/blank/unknown 은 기본값 {@link #BACKWARD}.
     */
    public static CursorDirection parseOrDefault(String raw) {
        if (raw == null || raw.isBlank()) {
            return BACKWARD;
        }
        return switch (raw.trim().toLowerCase()) {
            case "forward" -> FORWARD;
            case "backward" -> BACKWARD;
            default -> throw new IllegalArgumentException(
                    "Unknown cursor direction: " + raw + " (expected: backward|forward)");
        };
    }
}
