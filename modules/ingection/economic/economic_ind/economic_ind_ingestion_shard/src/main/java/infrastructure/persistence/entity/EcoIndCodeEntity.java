package infrastructure.persistence.entity;

import domain.enums.IndicatorUnit;
import domain.enums.ReleaseFrequency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "economic_indicator_code"
)
@SequenceGenerator(
        name = "economic_indicator_code_seq_gen",
        sequenceName = "economic_indicator_code_seq",
        allocationSize = 100   // ← 하이버네이트가 1000개씩 ID 블록을 미리 가져와 round-trip을 줄임
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EcoIndCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "economic_indicator_code_seq_gen")
    private Long id;

    @Column(unique = true)
    private String indicatorCode; //고유 이름 (country-type-frequency)

    @Column
    private String country;

    @Column
    private String type; //cpi,gdp ,실업률,...

    @Column
    @Enumerated(EnumType.STRING)
    private ReleaseFrequency frequency;//주기

    @Column
    @Enumerated(EnumType.STRING)
    private IndicatorUnit unit;//단위
}
