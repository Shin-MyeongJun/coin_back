package com.example.demo.ingestion.exchange.application;


import com.example.demo.ingestion.exchange.application.port.in.GetSymbolsUseCase;
import com.example.demo.ingestion.exchange.application.port.in.ManageSubscribe;
import com.example.demo.ingestion.exchange.application.port.out.SubscribeMarketDataPort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
public abstract class ExchangeStreamManager implements ManageSubscribe {

    private static final int CHUNK_SIZE = 50;
    private static final Logger log = LoggerFactory.getLogger(ExchangeStreamManager.class);

    private final Set<String> marketCodes = new HashSet<>();
    private final GetSymbolsUseCase symbolGetter;



    // 개별 구독자들 (청크 수 만큼 운용)
    private final List<SubscribeMarketDataPort> subscribers = new ArrayList<>();


    //scheduling  connect
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();
    private final AtomicLong nextAvailableTime = new AtomicLong(0L);
    private final long interval;


    @PostConstruct
    public void init() {
        marketCodes.addAll(symbolGetter.getAll());
        connect();
    }

    /**
     * 심볼 목록이 바뀌었을 때만 재구성
     */
    @Override
    public synchronized void reconnect() {
        log.info("reconnect to ExchangeStreamManager");
        Set<String> fresh = new HashSet<>(symbolGetter.getAll());
        if (!marketCodes.equals(fresh)) {
            marketCodes.clear();
            marketCodes.addAll(fresh);
            connect();
        }
    }

    /**
     * 구독(또는 재구독) 토폴로지 구성
     */
    protected synchronized void connect() {
        // 1) 결정론적 청크 생성: 정렬 → 청크
        List<String> sorted = new ArrayList<>(marketCodes);
        Collections.sort(sorted);
        List<List<String>> chunks = makeChunks(sorted);

        // 2) 구독자 수 증감
        adjustSubscribers(chunks.size());
        log.info("connect to ExchangeStreamManager  chunks: {}", chunks.size());
        // 3) 매칭 재구성: i번째 구독자는 i번째 청크로 구독
        for (int i = 0; i < chunks.size(); i++) {
            SubscribeMarketDataPort s = subscribers.get(i);
            // 안전하게 기존 구독 해제 후 재구독(인터페이스 정의에 따라 다르면 resubscribe 제공)
            scheduleSubscribeWithDelay(s, chunks.get(i));
        }
    }

    private List<List<String>> makeChunks(List<String> input) {
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < input.size(); i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, input.size());
            // subList는 뷰이지만 여기서는 즉시 subscribe에 넘기므로 OK
            chunks.add(input.subList(i, end));
        }
        return chunks;
    }

    /**
     * 청크 수 변화에 맞춘 구독자 증감
     */
    private void adjustSubscribers(int newSize) {
        int cur = subscribers.size();

        // grow
        for (int i = cur; i < newSize; i++) {
            subscribers.add(createSubscriber());
        }

        // shrink (끝에서부터 pop, 리소스 정리)
        for (int i = subscribers.size() - 1; i >= newSize; i--) {
            try {
                subscribers.get(i).unsubscribe();
            } catch (Exception ignore) {
            }
            subscribers.remove(i);
        }
    }

    /**
     * 구독자 인스턴스 생성 (구현체 필수)
     */

    protected abstract SubscribeMarketDataPort createSubscriber();

    protected long getConnectIntervalMillis() {
        return 0L;
    }

    /**
     * 공통 딜레이/레이트 리밋 로직
     */
    private void scheduleSubscribeWithDelay(SubscribeMarketDataPort s, List<String> codes) {


        // 딜레이 제한이 없으면 바로 동기 실행 (현재 동작 유지)
        if (interval <= 0) {
            s.unsubscribe();
            s.subscribe(codes);
            return;
        }

        // 레이트 리밋 적용: 각 subscribe 호출 사이에 interval 만큼 간격 유지
        long now = System.currentTimeMillis();
        long base = Math.max(now, nextAvailableTime.get());
        long scheduledTime = base + interval;
        nextAvailableTime.set(scheduledTime);

        long delay = Math.max(0, scheduledTime - now);

        log.info("schedule subscribe: delay={}ms, codesSize={}", delay, codes.size());

        scheduler.schedule(() -> {
            try {
                s.unsubscribe();
                s.subscribe(codes);
            } catch (Exception e) {
                log.error("subscribe failed", e);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }
}
