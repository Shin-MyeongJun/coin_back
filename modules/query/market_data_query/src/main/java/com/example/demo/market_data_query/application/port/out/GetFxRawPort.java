package com.example.demo.market_data_query.application.port.out;

import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.market_data_query.application.dto.FxView;

import java.util.List;

public interface GetFxRawPort {

    List<FxView> findRaw(String baseCurrency, String quoteCurrency, Long fromTs, Long toTs);

    /**
     * Cursor 기반 조회.
     * <ul>
     *   <li>{@code cursor}=null 일 경우 최신부터(BACKWARD) 또는 가장 과거부터(FORWARD).</li>
     *   <li>결과는 timestamp 오름차순으로 반환한다 (direction 무관, items 정렬은 호출 측 expectation).</li>
     *   <li>UseCase 레이어가 hasMore 판정을 위해 {@code limit + 1} 을 전달하므로 본 메서드는 받은 limit 만큼 페치.</li>
     * </ul>
     */
    List<FxView> findCursor(String baseCurrency, String quoteCurrency,
                            Long cursor, int limit, CursorDirection direction);
}
