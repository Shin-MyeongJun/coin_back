package infrastructure.persistence.entity;

import domain.enums.ScheduleState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "economic_schedule",
        indexes = {
                @Index(name = "idx_rs_date_status", columnList = "release_date, status"),
                @Index(name = "idx_rs_release_code", columnList = "release_code")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EconomicScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ind_code_id", nullable = false)
    private Long indCodeId;

    @Column(name = "release_date", nullable = false, length = 10)
    private Long releaseDate;  // "2026-04-10"

    @Column(name = "release_code", unique = true)
    private String releaseCode;

    @Column(nullable = false, length = 20)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ScheduleState status =  ScheduleState.PENDING;  // PENDING, FETCHING, DONE, FAILED

    @Column(name = "fetched_at")
    private Long fetchedAt;

    public void markDone() {
        this.status = ScheduleState.DONE;
        this.fetchedAt = System.currentTimeMillis();
    }

    public void markFailed() {
        this.status = ScheduleState.FAIL;
    }

    public void markFetching() {
        this.status =  ScheduleState.FETCHING;
    }
}