package com.example.demo.ingestion.economic.crawling.infrastructure.crawler.investing;

import com.example.demo.ingestion.economic.crawling.infrastructure.config.CrawlingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvestingClient {

    private final CrawlingProperties properties;

    public Optional<BigDecimal> scrapePrice(String url) {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                Document doc = Jsoup.connect(url)
                        .userAgent(properties.getInvesting().getUserAgent())
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .referrer("https://www.investing.com")
                        .timeout(8000)
                        .followRedirects(true)
                        .get();

                Element priceEl = doc.selectFirst("[data-test='instrument-price-last']");
                if (priceEl == null) {
                    priceEl = doc.selectFirst("#last_last");
                }
                if (priceEl != null) {
                    String text = priceEl.text().replaceAll("[^0-9.]", "");
                    if (!text.isEmpty()) {
                        return Optional.of(new BigDecimal(text));
                    }
                }
                log.warn("Investing.com: price element not found at {}", url);
                return Optional.empty();
            } catch (Exception e) {
                if (attempt < 2) {
                    try {
                        Thread.sleep(1000L * (attempt + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return Optional.empty();
                    }
                } else {
                    log.warn("Investing.com scrape failed after {} attempts for {}: {}", attempt + 1, url, e.getMessage());
                }
            }
        }
        return Optional.empty();
    }
}
