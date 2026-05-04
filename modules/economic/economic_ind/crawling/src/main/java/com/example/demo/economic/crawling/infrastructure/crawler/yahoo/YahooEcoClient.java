package com.example.demo.economic.crawling.infrastructure.crawler.yahoo;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Slf4j
public class YahooEcoClient {

    private static final String CALENDAR_URL = "https://finance.yahoo.com/calendar/economic";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    public Optional<BigDecimal> fetchActualValue(String eventName) {
        String today = LocalDate.now().toString();
        try {
            Document doc = Jsoup.connect(CALENDAR_URL + "?day=" + today)
                    .userAgent(USER_AGENT)
                    .timeout(15_000)
                    .get();

            Elements rows = doc.select("table tbody tr");
            for (Element row : rows) {
                Elements cells = row.select("td");
                if (cells.size() < 4) continue;

                String event = cells.get(0).text();
                String actual = cells.get(3).text().trim();

                if (event.toLowerCase().contains(eventName.toLowerCase())) {
                    if (!actual.isEmpty() && !actual.equals("-") && !actual.equals("N/A")) {
                        String cleaned = actual.replaceAll("[^0-9.\\-]", "");
                        if (!cleaned.isEmpty()) {
                            return Optional.of(new BigDecimal(cleaned));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Yahoo Finance fetch failed for '{}': {}", eventName, e.getMessage());
        }
        return Optional.empty();
    }
}
