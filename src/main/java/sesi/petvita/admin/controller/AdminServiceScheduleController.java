// sesi/petvita/admin/controller/AdminServiceScheduleController.java
package sesi.petvita.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.serviceschedule.dto.ServiceScheduleResponseDTO;
import sesi.petvita.serviceschedule.service.ServiceScheduleService;

import java.util.List;

@RestController
@RequestMapping("/admin/service-schedules") // Nova rota de admin
@RequiredArgsConstructor
@Tag(name = "Admin Service Schedules", description = "Endpoints [ADMIN] para gerenciar agendamentos de serviços")
public class AdminServiceScheduleController {

    private final ServiceScheduleService serviceScheduleService;

    @GetMapping
    @Operation(summary = "Listar todos os agendamentos de serviço da clínica")
    public ResponseEntity<List<ServiceScheduleResponseDTO>> getAllServiceSchedules() {
        return ResponseEntity.ok(serviceScheduleService.findAllForAdmin());
    }
}