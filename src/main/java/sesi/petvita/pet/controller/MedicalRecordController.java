package sesi.petvita.pet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.pet.dto.MedicalRecordResponseDTO;
import sesi.petvita.pet.service.MedicalRecordService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Prontuário Médico", description = "Endpoints para visualização do prontuário médico")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @GetMapping("/pets/{petId}/medical-records")
    @Operation(summary = "[TUTOR, VET] Listar o histórico de prontuários de um pet")
    public ResponseEntity<List<MedicalRecordResponseDTO>> getMedicalRecordsByPet(@PathVariable Long petId) {
        return ResponseEntity.ok(medicalRecordService.findRecordsByPet(petId));
    }

    // --- NOVO ENDPOINT ---
    @GetMapping("/medical-records/{id}")
    @Operation(summary = "[TODOS] Buscar um prontuário específico pelo ID (com anexos e prescrições)")
    public ResponseEntity<MedicalRecordResponseDTO> getMedicalRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(medicalRecordService.findById(id));
    }
}