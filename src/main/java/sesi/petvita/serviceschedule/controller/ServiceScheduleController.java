package sesi.petvita.serviceschedule.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.serviceschedule.dto.ServiceScheduleRequestDTO;
import sesi.petvita.serviceschedule.dto.ServiceScheduleResponseDTO;
import sesi.petvita.serviceschedule.service.ServiceScheduleService;
import sesi.petvita.user.model.UserModel;

// Imports necessários para o novo endpoint
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/service-schedules")
@RequiredArgsConstructor
public class ServiceScheduleController {

    private final ServiceScheduleService serviceScheduleService;

    @PostMapping
    public ResponseEntity<ServiceScheduleResponseDTO> createSchedule(
            @Valid @RequestBody ServiceScheduleRequestDTO requestDTO,
            @AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(serviceScheduleService.create(requestDTO, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceScheduleResponseDTO> getScheduleById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserModel user) {
        // Busca detalhes (incluindo chatRoomId) validando se pertence ao usuário
        return ResponseEntity.ok(serviceScheduleService.findByIdForUser(id, user));
    }

    @GetMapping("/my-schedules")
    public ResponseEntity<List<ServiceScheduleResponseDTO>> getMySchedules(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(serviceScheduleService.findForAuthenticatedUser(user));
    }

    @GetMapping("/employee/{employeeId}/available-slots")
    public ResponseEntity<List<LocalTime>> getAvailableSlots(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(serviceScheduleService.getAvailableSlotsForEmployee(employeeId, date));
    }

}
