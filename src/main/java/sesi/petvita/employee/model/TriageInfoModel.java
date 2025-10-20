package sesi.petvita.employee.model;

import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.consultation.model.ConsultationModel;
import java.time.LocalDateTime;

@Entity
@Table(name = "triage_info")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriageInfoModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false, unique = true)
    private ConsultationModel consultation;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "temperature_celsius")
    private Double temperatureCelsius;

    @Column(columnDefinition = "TEXT", name = "main_complaint")
    private String mainComplaint;

    @Column(nullable = false)
    private LocalDateTime triageTimestamp;

    @PrePersist
    protected void onCreate() {
        triageTimestamp = LocalDateTime.now();
    }
}