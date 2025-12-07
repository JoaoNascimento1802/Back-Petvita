package sesi.petvita.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.serviceschedule.dto.ServiceScheduleResponseDTO;
import sesi.petvita.serviceschedule.dto.ServiceScheduleUpdateRequestDTO;
import sesi.petvita.serviceschedule.service.ServiceScheduleService;

import java.util.List;

@RestController
@RequestMapping("/admin/service-schedules") // Verifique se esta linha está exata
@RequiredArgsConstructor
@Tag(name = "Admin Service Schedules", description = "Endpoints [ADMIN] para gerenciar agendamentos de serviços")
public class AdminServiceScheduleController {

    private final ServiceScheduleService serviceScheduleService;

    @GetMapping
    @Operation(summary = "Listar todos os agendamentos de serviço da clínica")
    public ResponseEntity<List<ServiceScheduleResponseDTO>> getAllServiceSchedules() {
        return ResponseEntity.ok(serviceScheduleService.findAllForAdmin());
    }

    @PutMapping("/{id}")
    @Operation(summary = "[ADMIN] Editar um agendamento de serviço")
    public ResponseEntity<ServiceScheduleResponseDTO> updateServiceSchedule(
            @PathVariable Long id,
            @RequestBody @Valid ServiceScheduleUpdateRequestDTO dto) {
        return ResponseEntity.ok(serviceScheduleService.updateServiceScheduleByAdmin(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "[ADMIN] Excluir um agendamento de serviço")
    public ResponseEntity<Void> deleteServiceSchedule(@PathVariable Long id) {
        serviceScheduleService.deleteServiceSchedule(id);
        return ResponseEntity.noContent().build();
    }
}