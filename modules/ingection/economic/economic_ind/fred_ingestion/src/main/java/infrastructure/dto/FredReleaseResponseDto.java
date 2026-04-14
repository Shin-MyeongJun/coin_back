package infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FredReleaseResponseDto {

    // JSON의 "release_dates" 키를 이 리스트에 자동으로 매핑합니다.
    @JsonProperty("release_dates")
    private List<ReleaseDateDto> releaseDates;
}
