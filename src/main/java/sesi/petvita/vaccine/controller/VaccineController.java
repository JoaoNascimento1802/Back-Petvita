package sesi.petvita.vaccine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.vaccine.dto.VaccineRequestDTO;
import sesi.petvita.vaccine.dto.VaccineResponseDTO;
import sesi.petvita.vaccine.service.VaccineService;

import java.util.List;

@RestController
@RequestMapping("/api/vaccines")
@RequiredArgsConstructor
@Tag(name = "Vacinas", description = "Gestão da carteirinha de vacinação")
public class VaccineController {

    private final VaccineService vaccineService;

    @PostMapping
    @Operation(summary = "[VET] Registrar uma nova vacina")
    public ResponseEntity<VaccineResponseDTO> addVaccine(
            @RequestBody @Valid VaccineRequestDTO dto,
            @AuthenticationPrincipal UserModel user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vaccineService.addVaccine(dto, user));
    }

    // --- NOVO ENDPOINT DE EDIÇÃO ---
    @PutMapping("/{id}")
    @Operation(summary = "[VET] Editar uma vacina existente")
    public ResponseEntity<VaccineResponseDTO> updateVaccine(
            @PathVariable Long id,
            @RequestBody @Valid VaccineRequestDTO dto,
            @AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(vaccineService.updateVaccine(id, dto, user));
    }
    // -------------------------------

    @GetMapping("/pet/{petId}")
    @Operation(summary = "[TODOS] Listar vacinas de um pet")
    public ResponseEntity<List<VaccineResponseDTO>> getVaccinesByPet(@PathVariable Long petId) {
        return ResponseEntity.ok(vaccineService.getVaccinesByPet(petId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "[VET/ADMIN] Remover registro de vacina")
    public ResponseEntity<Void> deleteVaccine(@PathVariable Long id, @AuthenticationPrincipal UserModel user) {
        vaccineService.deleteVaccine(id, user);
        return ResponseEntity.noContent().build();
    }
}