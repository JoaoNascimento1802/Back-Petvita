package sesi.petvita.employee.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.employee.dto.EmployeeDashboardSummaryDTO;
import sesi.petvita.employee.service.EmployeeService;
import sesi.petvita.serviceschedule.dto.ServiceScheduleResponseDTO;
import sesi.petvita.serviceschedule.service.ServiceScheduleService;
import sesi.petvita.user.dto.UserResponseDTO;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.role.UserRole;
import sesi.petvita.user.service.UserService;
import sesi.petvita.veterinary.dto.VeterinaryRatingRequestDTO;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Endpoints para funcionários/profissionais de estética")
public class EmployeeController {

    private final ServiceScheduleService serviceScheduleService;
    private final UserService userService;
    private final EmployeeService employeeService;

    @GetMapping("/my-schedules")
    public ResponseEntity<List<ServiceScheduleResponseDTO>> getMySchedules(@AuthenticationPrincipal UserModel employee) {
        return ResponseEntity.ok(serviceScheduleService.findForEmployee(employee));
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(userService.findUsersByRole(UserRole.EMPLOYEE));
    }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<EmployeeDashboardSummaryDTO> getDashboardSummary(@AuthenticationPrincipal UserModel employee) {
        return ResponseEntity.ok(serviceScheduleService.getDashboardSummary(employee));
    }

    // --- NOVO ENDPOINT PARA ACEITAR ---
    @PostMapping("/schedules/{id}/accept")
    @Operation(summary = "[EMPLOYEE] Aceitar um pedido de agendamento de serviço")
    public ResponseEntity<Void> acceptSchedule(@PathVariable Long id, @AuthenticationPrincipal UserModel employee) {
        serviceScheduleService.acceptSchedule(id, employee);
        return ResponseEntity.ok().build();
    }

    // --- NOVO ENDPOINT PARA RECUSAR ---
    @PostMapping("/schedules/{id}/reject")
    @Operation(summary = "[EMPLOYEE] Recusar um pedido de agendamento de serviço")
    public ResponseEntity<Void> rejectSchedule(@PathVariable Long id, @AuthenticationPrincipal UserModel employee) {
        serviceScheduleService.rejectSchedule(id, employee);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/schedules/{id}")
    @Operation(summary = "[EMPLOYEE] Obter detalhes de um agendamento de serviço específico")
    public ResponseEntity<ServiceScheduleResponseDTO> getScheduleDetails(@PathVariable Long id, @AuthenticationPrincipal UserModel employee) {
        return ResponseEntity.ok(serviceScheduleService.findScheduleByIdForEmployee(id, employee));
    }

    @PutMapping("/schedules/{id}/report")
    @Operation(summary = "[EMPLOYEE] Adicionar ou atualizar o relatório de um serviço")
    public ResponseEntity<Void> addReport(@PathVariable Long id, @RequestBody String report, @AuthenticationPrincipal UserModel employee) {
        serviceScheduleService.addReport(id, report, employee);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/schedules/{id}/finalize")
    @Operation(summary = "[EMPLOYEE] Finalizar um agendamento de serviço")
    public ResponseEntity<Void> finalizeSchedule(@PathVariable Long id, @AuthenticationPrincipal UserModel employee) {
        serviceScheduleService.finalizeSchedule(id, employee);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/rate")
    public ResponseEntity<Void> rateEmployee(
            @PathVariable Long id,
            @RequestBody VeterinaryRatingRequestDTO dto,
            @AuthenticationPrincipal UserModel user) {
        employeeService.addRating(id, user.getId(), dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/rate/me")
    public ResponseEntity<VeterinaryRatingRequestDTO> getMyRating(
            @PathVariable Long id,
            @AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(employeeService.getRatingByUser(id, user.getId()));
    }

    @GetMapping("/me/rating-summary")
    public ResponseEntity<Double> getMyAverage(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(employeeService.getAverageRating(user.getId()));
    }


}
