package com.example.demo.economic_query.infrastructure.persistence.mapper;

import com.example.demo.economic_query.application.dto.CorrelationResultView;
import com.example.demo.economic_query.application.dto.EconomicCalendarView;
import com.example.demo.economic_query.application.dto.IndicatorMetaView;
import com.example.demo.economic_query.application.dto.IndicatorSeriesView;
import com.example.demo.economic_query.infrastructure.persistence.entity.CorrelationResultQueryEntity;
import com.example.demo.economic_query.infrastructure.persistence.entity.EcoIndCodeQueryEntity;
import com.example.demo.economic_query.infrastructure.persistence.entity.EcoIndQueryEntity;
import com.example.demo.economic_query.infrastructure.persistence.entity.EconomicScheduleQueryEntity;
import org.springframework.stereotype.Component;

@Component
public class IndicatorViewMapper {

    public IndicatorSeriesView toSeriesView(EcoIndQueryEntity e) {
        return new IndicatorSeriesView(
                e.getIndCodeId(),
                e.getValue(),
                e.getObservationDate(),
                e.getReleaseDate(),
                e.getTimestamp()
        );
    }

    public IndicatorMetaView toMetaView(EcoIndCodeQueryEntity e) {
        return new IndicatorMetaView(
                e.getId(),
                e.getIndicatorCode(),
                e.getCountry(),
                e.getType(),
                e.getFrequency(),
                e.getUnit()
        );
    }

    public EconomicCalendarView toCalendarView(EconomicScheduleQueryEntity e) {
        return new EconomicCalendarView(
                e.getId(),
                e.getIndCodeId(),
                e.getReleaseCode(),
                e.getReleaseDate(),
                e.getStatus(),
                e.getFetchedAt()
        );
    }

    public CorrelationResultView toCorrelationView(CorrelationResultQueryEntity e) {
        return new CorrelationResultView(
                e.getId(),
                e.getAssetSymbol(),
                e.getIndicatorCode(),
                e.getCorrelation(),
                e.getPeriodDays(),
                e.getCalculatedAt()
        );
    }
}
