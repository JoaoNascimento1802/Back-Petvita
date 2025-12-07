package sesi.petvita.employee.model;

import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.user.model.UserModel;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employee_rating")
public class EmployeeRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private UserModel employee; // O funcionário sendo avaliado

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user; // Quem avaliou

    private Double rating;
    private String comment;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}