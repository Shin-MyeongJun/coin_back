package domain;

public record FredObservationResult(
        String seriesId,
        String date,
        String value,
        String units,
        String frequency
) {}
