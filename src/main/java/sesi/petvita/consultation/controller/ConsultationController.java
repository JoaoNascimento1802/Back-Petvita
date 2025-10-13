package sesi.petvita.consultation.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.consultation.dto.ConsultationRequestDTO;
import sesi.petvita.consultation.dto.ConsultationResponseDTO;
import sesi.petvita.consultation.dto.ConsultationUpdateRequestDTO;
import sesi.petvita.consultation.service.ConsultationService;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/consultas")
@RequiredArgsConstructor
@Tag(name = "Consultas", description = "Endpoints de agendamento e gestão de consultas")
public class ConsultationController {

    private final ConsultationService service;

    @PostMapping
    @Operation(summary = "Solicitar uma nova consulta (Usuário)")
    public ResponseEntity<ConsultationResponseDTO> create(@RequestBody @Valid ConsultationRequestDTO dto, @AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(service.create(dto, user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar uma consulta por ID (Autenticado)")
    public ResponseEntity<ConsultationResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/my-consultations")
    @Operation(summary = "Listar as minhas consultas (Usuário)")
    public ResponseEntity<List<ConsultationResponseDTO>> findMyConsultations(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(service.findForAuthenticatedUser(user));
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "[VET] Aceitar uma consulta pendente")
    public ResponseEntity<Void> accept(@PathVariable Long id) {
        service.acceptConsultation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "[VET] Recusar uma consulta pendente")
    public ResponseEntity<Void> reject(@PathVariable Long id) {
        service.rejectConsultation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "[USER/VET] Cancelar uma consulta agendada")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @AuthenticationPrincipal UserModel user) {
        service.cancelConsultation(id, user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/report")
    @Operation(summary = "[VET] Adicionar/Editar relatório de uma consulta finalizada")
    public ResponseEntity<Void> writeReport(@PathVariable Long id, @RequestBody String report) {
        service.writeReport(id, report);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "[USER] Editar uma consulta")
    public ResponseEntity<ConsultationResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid ConsultationUpdateRequestDTO dto,
            @AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(service.updateConsultation(id, dto, user));
    }

    // O ENDPOINT GET /all FOI REMOVIDO DAQUI. A funcionalidade agora está centralizada e segura em /admin/consultations

    @GetMapping("/by-date")
    @Operation(summary = "Buscar consultas por data")
    public ResponseEntity<List<ConsultationResponseDTO>> findByDate(@RequestParam @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ResponseEntity.ok(service.findConsultationsByDate(date));
    }

    @GetMapping("/by-speciality")
    @Operation(summary = "Buscar consultas por especialidade do médico")
    public ResponseEntity<List<ConsultationResponseDTO>> findBySpeciality(@RequestParam SpecialityEnum speciality) {
        return ResponseEntity.ok(service.findConsultationsBySpeciality(speciality));
    }

    @GetMapping("/vet/my-consultations")
    @Operation(summary = "[VET] Listar as minhas consultas (Veterinário)")
    public ResponseEntity<List<ConsultationResponseDTO>> findMyConsultationsForVet(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(service.findForAuthenticatedVeterinary(user));
    }

    // Este endpoint foi mantido pois é chamado pelo AdminController, mas a rota principal de acesso é /admin/consultations/{id}
    @PutMapping("/consultations/{id}")
    @Operation(summary = "[ADMIN] Atualizar dados de uma consulta")
    public ResponseEntity<ConsultationResponseDTO> updateConsultation(@PathVariable Long id, @RequestBody @Valid ConsultationUpdateRequestDTO dto) {
        return ResponseEntity.ok(service.updateConsultationByAdmin(id, dto));
    }

    @GetMapping("/by-veterinary-name")
    @Operation(summary = "Buscar consultas por nome do médico")
    public ResponseEntity<List<ConsultationResponseDTO>> findByVeterinaryName(@RequestParam String name) {
        return ResponseEntity.ok(service.findConsultationsByVeterinaryName(name));
    }

    @GetMapping("/by-pet-name")
    @Operation(summary = "Buscar consultas por nome do paciente (pet)")
    public ResponseEntity<List<ConsultationResponseDTO>> findByPetName(@RequestParam String name) {
        return ResponseEntity.ok(service.findConsultationsByPetName(name));
    }

    @GetMapping("/by-range")
    @Operation(summary = "Buscar consultas por intervalo de datas (para calendários)")
    public ResponseEntity<List<ConsultationResponseDTO>> findByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(service.findByDateRange(startDate, endDate));
    }
}