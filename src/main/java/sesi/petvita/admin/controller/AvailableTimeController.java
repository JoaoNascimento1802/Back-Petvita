package sesi.petvita.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.employee.service.EmployeeService;
import sesi.petvita.veterinary.service.VeterinaryService; // Importe o VeterinaryService

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/available-times")
@RequiredArgsConstructor
public class AvailableTimeController {

    private final EmployeeService employeeService;
    private final VeterinaryService veterinaryService; // Injete o serviço

    // Endpoint para FUNCIONÁRIO (que já fizemos)
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Listar horários disponíveis de um funcionário para serviços")
    public ResponseEntity<List<LocalTime>> getEmployeeAvailableTimes(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<LocalTime> slots = employeeService.getAvailableSlots(employeeId, date);
        return ResponseEntity.ok(slots);
    }

    // --- NOVO ENDPOINT PARA VETERINÁRIO ---
    @GetMapping("/veterinary/{vetId}")
    @Operation(summary = "Listar horários disponíveis de um veterinário para consultas")
    public ResponseEntity<List<LocalTime>> getVeterinaryAvailableTimes(
            @PathVariable Long vetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // O VeterinaryService já tinha esse método getAvailableSlots no código que você mandou antes
        List<LocalTime> slots = veterinaryService.getAvailableSlots(vetId, date);
        return ResponseEntity.ok(slots);
    }
}