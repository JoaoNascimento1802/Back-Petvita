package sesi.petvita.veterinary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.dto.VeterinarianMonthlyReportDTO;
import sesi.petvita.veterinary.dto.VeterinaryRatingRequestDTO;
import sesi.petvita.veterinary.dto.VeterinaryRequestDTO;
import sesi.petvita.veterinary.dto.VeterinaryResponseDTO;
import sesi.petvita.veterinary.service.VeterinaryService;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/veterinary")
@Tag(name = "Veterinários", description = "Endpoints para visualização e gerenciamento de veterinários")
@RequiredArgsConstructor
public class VeterinaryController {

    private final VeterinaryService veterinaryService;

    @PostMapping
    @Operation(summary = "[ADMIN] Cadastrar um novo veterinário")
    public ResponseEntity<VeterinaryResponseDTO> createVeterinary(@Valid @RequestBody VeterinaryRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(veterinaryService.createVeterinary(requestDTO));
    }

    @GetMapping
    @Operation(summary = "[TODOS] Listar todos os veterinários")
    public ResponseEntity<List<VeterinaryResponseDTO>> getAllVeterinaries() {
        return ResponseEntity.ok(veterinaryService.findAll());
    }

    @GetMapping("/search")
    @Operation(summary = "[TODOS] Buscar veterinários por nome e/ou especialidade")
    public ResponseEntity<List<VeterinaryResponseDTO>> searchVeterinarians(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) SpecialityEnum speciality) {
        return ResponseEntity.ok(veterinaryService.searchVeterinarians(name, speciality));
    }

    @GetMapping("/me")
    @Operation(summary = "[VET] Buscar dados do perfil profissional do veterinário logado")
    public ResponseEntity<VeterinaryResponseDTO> getMyProfessionalProfile(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(veterinaryService.findVeterinaryByUserAccount(user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "[TODOS] Buscar veterinário por ID")
    public ResponseEntity<VeterinaryResponseDTO> getVeterinaryById(@PathVariable Long id) {
        return ResponseEntity.ok(veterinaryService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "[ADMIN] Atualizar veterinário pelo ID")
    public ResponseEntity<VeterinaryResponseDTO> updateVeterinary(@PathVariable Long id, @Valid @RequestBody VeterinaryRequestDTO requestDTO) {
        return ResponseEntity.ok(veterinaryService.updateVeterinary(id, requestDTO));
    }

    @GetMapping("/{vetId}/available-slots")
    @Operation(summary = "[TODOS] Listar horários disponíveis para um veterinário em uma data específica")
    public ResponseEntity<List<LocalTime>> getAvailableSlots(
            @PathVariable Long vetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(veterinaryService.getAvailableSlots(vetId, date));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "[ADMIN] Deletar veterinário pelo ID")
    public ResponseEntity<Void> deleteVeterinary(@PathVariable Long id) {
        veterinaryService.deleteVeterinary(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rate")
    @Operation(summary = "[USER] Adicionar uma avaliação a um veterinário")
    public ResponseEntity<Void> addRating(
            @PathVariable Long id,
            @RequestBody @Valid VeterinaryRatingRequestDTO dto,
            @AuthenticationPrincipal UserModel user) {
        veterinaryService.addRating(id, user.getId(), dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me/monthly-report")
    @Operation(summary = "[VET] Obter relatório de consultas do mês para o veterinário logado")
    public ResponseEntity<VeterinarianMonthlyReportDTO> getMyMonthlyReport(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(veterinaryService.getMonthlyReport(user));
    }
}