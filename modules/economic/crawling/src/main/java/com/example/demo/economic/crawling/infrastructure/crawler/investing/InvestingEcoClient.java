package com.example.demo.economic.crawling.infrastructure.crawler.investing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

@Slf4j
public class InvestingEcoClient {

    private static final String CALENDAR_API =
            "https://economic-calendar.investing.com/economic-calendar/Action/GetCalendarFilteredData";
    private static final String REFERER = "https://www.investing.com/economic-calendar/";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public InvestingEcoClient() {
        this.webClient = WebClient.builder()
                .defaultHeader("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .defaultHeader("X-Requested-With", "XMLHttpRequest")
                .defaultHeader("Referer", REFERER)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public Optional<BigDecimal> fetchActualValue(String eventName) {
        String today = LocalDate.now().toString();
        String body = "country%5B%5D=5&dateFrom=" + today + "&dateTo=" + today
                + "&timeZone=55&timeframe=0&currentTab=custom&limit_from=0";
        try {
            String responseBody = webClient.post()
                    .uri(CALENDAR_API)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            if (responseBody == null) return Optional.empty();

            JsonNode root = objectMapper.readTree(responseBody);
            String html = root.path("data").asText();
            if (html.isEmpty()) return Optional.empty();

            Document doc = Jsoup.parse(html);
            Elements rows = doc.select("tr.js-event-item");
            for (Element row : rows) {
                String name = row.select("td.event a").text();
                if (name.toLowerCase().contains(eventName.toLowerCase())) {
                    String actual = row.select("td.act").text().trim();
                    if (!actual.isEmpty() && !actual.equals("/") && !actual.equals("-")) {
                        String cleaned = actual.replaceAll("[^0-9.\\-]", "");
                        if (!cleaned.isEmpty()) {
                            return Optional.of(new BigDecimal(cleaned));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Investing.com fetch failed for '{}': {}", eventName, e.getMessage());
        }
        return Optional.empty();
    }
}
