package infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReleaseDateDto {

    @JsonProperty("release_id")
    private int releaseId;

    // JSON 키와 변수명이 "date"로 동일하므로 @JsonProperty 생략 가능
    private String date;

    @JsonProperty("release_name")
    private String releaseName;
}
