package infrastructure.clinet;

import com.fasterxml.jackson.databind.JsonNode;
import infrastructure.config.FredProperties;
import infrastructure.dto.FredObservationResultDto;
import infrastructure.dto.FredReleaseResponseDto;
import infrastructure.dto.ReleaseDateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FredClient{

    private final WebClient.Builder webClientBuilder;
    private final FredProperties properties;

    /**
     * fred/releases/dates
     * include_release_dates_with_no_data=true → 미래 발표 일정 포함
     */
    public List<ReleaseDateDto> fetchReleaseDates(String realTimeStart, String realTimeEnd) {
        String url = properties.getBaseUrl() + "/releases/dates"
                + "?api_key=" + properties.getApiKey()
                + "&file_type=json"
                + "&realtime_start=" + realTimeStart
                + "&include_release_dates_with_no_data=true"
                + "&realtime_end=" + realTimeEnd
                + "&sort_order=asc"
                + "&limit=1000";

        // JsonNode 대신 생성한 Wrapper DTO 클래스를 직접 지정합니다.
        FredReleaseResponseDto response = webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(FredReleaseResponseDto.class) // ✨ 여기가 핵심입니다!
                .block();

        // 응답이 없거나 데이터가 비어있으면 안전하게 빈 리스트 반환
        if (response == null || response.getReleaseDates() == null) {
            return Collections.emptyList();
        }

        // 파싱된 DTO 리스트를 그대로 반환
        return response.getReleaseDates();
    }

    /**
     * fred/series/observations
     * sort_order=desc & limit=1 → 가장 최신 관측값 1건만
     */
    public FredObservationResultDto fetchLatestObservation(String seriesId) {
        String url = properties.getBaseUrl() + "/series/observations"
                + "?series_id=" + seriesId
                + "&api_key=" + properties.getApiKey()
                + "&file_type=json"
                + "&sort_order=desc"
                + "&limit=1";

        JsonNode root = webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (root == null || !root.has("observations")
                || root.get("observations").isEmpty()) {
            return null;
        }

        JsonNode obs = root.get("observations").get(0);
        String value = obs.get("value").asText();

        // FRED에서 units와 frequency를 가져오려면 fred/series 호출 필요
        // 여기서는 간결하게 null 처리, 추후 fred/series 메타데이터 캐싱으로 개선
        return new FredObservationResultDto(
                seriesId,
                obs.get("date").asText(),
                value,
                null,  // units - 별도 fred/series 호출로 채움
                null   // frequency
        );
    }
}
