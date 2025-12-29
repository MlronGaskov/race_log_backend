package miron.gaskov.racelog.result;

import jakarta.persistence.*;
import lombok.*;
import miron.gaskov.racelog.discipline.Discipline;
import miron.gaskov.racelog.user.User;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "results", indexes = {
        @Index(name = "idx_results_athlete", columnList = "athlete_id"),
        @Index(name = "idx_results_discipline", columnList = "discipline_id"),
        @Index(name = "idx_results_date", columnList = "date")
})
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "athlete_id")
    private User athlete;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discipline_id")
    private Discipline discipline;

    @Column(name = "result_value", nullable = false, length = 32)
    private String resultValue;

    @Column(name = "result_numeric")
    private Double resultNumeric;

    @Column(name = "competition_name", nullable = false, length = 128)
    private String competitionName;

    @Column
    private Integer place;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 512)
    private String info;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
