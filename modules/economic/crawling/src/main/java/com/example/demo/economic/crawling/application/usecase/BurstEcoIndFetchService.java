package com.example.demo.economic.crawling.application.usecase;

import com.example.demo.economic.crawling.application.port.out.FetchEcoValuePort;
import com.example.demo.economic.crawling.application.port.out.PublishEcoIndPort;
import com.example.demo.economic.crawling.infrastructure.config.CrawlingEcoProperties;
import com.example.demo.ingestion.economic.economic_ind.application.port.in.GetRealTimeEcoIndUseCase;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.FlushAndSaveEconomicValuePort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorValue;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicRawIndicator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BurstEcoIndFetchService implements GetRealTimeEcoIndUseCase {

    private final FetchEcoValuePort.ForInvesting investingFetcher;
    private final FetchEcoValuePort.ForYahoo yahooFetcher;
    private final FlushAndSaveEconomicValuePort<EconomicRawIndicator> indSave;
    private final FlushAndSaveEconomicValuePort<EconomicIndicatorCode> codeSave;
    private final PublishEcoIndPort publisher;
    private final CrawlingEcoProperties properties;

    @Override
    public void process(String target) {
        CrawlingEcoProperties.IndicatorConfig config = properties.findByType(target);
        if (config == null) {
            log.warn("No crawling config found for indicator type: {}", target);
            return;
        }

        long startMs = System.currentTimeMillis();
        long timeoutMs = properties.getBurst().getTimeoutMs();
        long intervalMs = properties.getBurst().getIntervalMs();

        log.info("Starting burst polling for indicator: {}", target);

        while (System.currentTimeMillis() - startMs < timeoutMs) {
            Optional<BigDecimal> value = investingFetcher.fetchCurrentValue(target);
            if (value.isEmpty()) {
                log.debug("Investing.com returned no value for {}, trying Yahoo", target);
                value = yahooFetcher.fetchCurrentValue(target);
            }

            if (value.isPresent()) {
                long now = System.currentTimeMillis();
                EconomicIndicatorCode code = EconomicIndicatorCode.of(
                        config.getCountry(),
                        config.getType(),
                        config.getFrequency(),
                        config.getUnit()
                );
                EconomicIndicatorValue indValue = new EconomicIndicatorValue(
                        value.get(),
                        LocalDate.now().toString(),
                        now,
                        now
                );
                EconomicRawIndicator ind = new EconomicRawIndicator(code, indValue);

                codeSave.saveAll(List.of(ind.code()));
                indSave.saveAll(List.of(ind));
                publisher.publish(ind);

                log.info("Burst polling succeeded for {}: value={}", target, value.get());
                return;
            }

            log.debug("No value yet for {}, retrying in {}ms", target, intervalMs);
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Burst polling interrupted for: {}", target);
                return;
            }
        }

        log.warn("Burst polling timed out for indicator: {} after {}ms", target, timeoutMs);
    }
}
