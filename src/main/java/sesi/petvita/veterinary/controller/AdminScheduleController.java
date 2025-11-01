package sesi.petvita.veterinary.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.veterinary.dto.EmployeeWorkScheduleDTO;
import sesi.petvita.veterinary.dto.VeterinaryWorkScheduleDTO;
import sesi.petvita.veterinary.service.EmployeeScheduleService;
import sesi.petvita.veterinary.service.VeterinaryScheduleService;

import java.util.List;

@RestController
@RequestMapping("/admin/schedules") // <-- MUDANÇA PRINCIPAL: URL base agora é /admin/
@RequiredArgsConstructor
@Tag(name = "Work Schedules (Admin)", description = "Endpoints para o admin gerenciar horários de trabalho")
public class AdminScheduleController { // Renomeado para clareza

    private final VeterinaryScheduleService veterinaryScheduleService;
    private final EmployeeScheduleService employeeScheduleService;

    // Endpoints para Veterinários
    @GetMapping("/veterinary/{vetId}") // URL final: /admin/schedules/veterinary/1
    public ResponseEntity<List<VeterinaryWorkScheduleDTO>> getSchedulesForVeterinary(@PathVariable Long vetId) {
        return ResponseEntity.ok(veterinaryScheduleService.getSchedulesForVeterinary(vetId));
    }

    @PutMapping("/veterinary/{vetId}") // URL final: /admin/schedules/veterinary/1
    public ResponseEntity<List<VeterinaryWorkScheduleDTO>> updateSchedulesForVeterinary(
            @PathVariable Long vetId,
            @RequestBody List<VeterinaryWorkScheduleDTO> schedules) {
        return ResponseEntity.ok(veterinaryScheduleService.updateSchedules(vetId, schedules));
    }

    // Endpoints para Funcionários
    @GetMapping("/employee/{employeeId}") // URL final: /admin/schedules/employee/2
    public ResponseEntity<List<EmployeeWorkScheduleDTO>> getSchedulesForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeScheduleService.getSchedulesForEmployee(employeeId));
    }

    @PutMapping("/employee/{employeeId}") // URL final: /admin/schedules/employee/2
    public ResponseEntity<List<EmployeeWorkScheduleDTO>> updateSchedulesForEmployee(
            @PathVariable Long employeeId,
            @RequestBody List<EmployeeWorkScheduleDTO> schedules) {
        return ResponseEntity.ok(employeeScheduleService.updateSchedules(employeeId, schedules));
    }
}