package com.example.demo.infra_shard.paging;

import java.util.List;

/**
 * Query-side cursor 페이징 결과. read-side adapter/usecase 간 운반용 record.
 * <p>
 * <b>정렬 보장:</b> {@link #items} 는 timestamp 오름차순이다 (direction 무관).
 * <p>
 * <b>nextCursor 의미:</b>
 * <ul>
 *   <li>{@code direction=BACKWARD}: 다음 페이지(=더 과거)를 가져올 때 사용할 cursor. 보통
 *       {@code items[0].ts - 1}. 더 과거 데이터가 없으면 null.</li>
 *   <li>{@code direction=FORWARD}: 다음 페이지(=더 미래)를 가져올 때 사용할 cursor. 보통
 *       {@code items[last].ts + 1}. 더 미래 데이터가 없으면 null.</li>
 * </ul>
 * <p>
 * api layer 의 {@code CursorPage<T>} 와 분리한 이유: query 모듈은 api 의 envelope record 에
 * 의존하지 않으며, 같은 슬라이스를 다른 채널(예: 합성 service)에서 재가공할 수 있어야 한다.
 */
public record CursorSlice<T>(
        List<T> items,
        Long nextCursor,
        boolean hasMore
) {
    public static <T> CursorSlice<T> of(List<T> items, Long nextCursor) {
        return new CursorSlice<>(items, nextCursor, nextCursor != null);
    }

    public static <T> CursorSlice<T> last(List<T> items) {
        return new CursorSlice<>(items, null, false);
    }

    public static <T> CursorSlice<T> empty() {
        return new CursorSlice<>(List.of(), null, false);
    }
}
