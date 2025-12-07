package sesi.petvita.employee.controller; // Ajuste o pacote conforme seu projeto

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.model.WorkSchedule; // Ou o DTO correspondente
import sesi.petvita.veterinary.repository.WorkScheduleRepository; // Ou o Service

import java.util.List;

@RestController
@RequestMapping("/api/schedules/employee")
@RequiredArgsConstructor
public class EmployeeScheduleController {

    private final WorkScheduleRepository workScheduleRepository;

    @GetMapping("/me")
    @Operation(summary = "Buscar horários de trabalho do funcionário/veterinário logado")
    public ResponseEntity<List<WorkSchedule>> getMySchedules(@AuthenticationPrincipal UserModel user) {

        // Busca os horários usando o ID do usuário que está no token (user.getId())
        List<WorkSchedule> schedules = workScheduleRepository.findByProfessionalUserId(user.getId());

        return ResponseEntity.ok(schedules);
    }
}