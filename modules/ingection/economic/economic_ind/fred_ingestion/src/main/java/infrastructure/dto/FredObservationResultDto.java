package infrastructure.dto;

public record FredObservationResultDto(
        String seriesId,
        String date,
        String value,
        String units,
        String frequency
) {}
