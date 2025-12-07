package sesi.petvita.veterinary.controller;

import com.itextpdf.text.DocumentException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sesi.petvita.admin.dto.ReportSummaryDTO;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.dto.*;
import sesi.petvita.veterinary.model.VeterinaryModel;
import sesi.petvita.veterinary.service.VeterinaryService;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

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


    @GetMapping("/{id}/rate/me")
    @Operation(summary = "[USER] Obter minha avaliação anterior para este veterinário")
    public ResponseEntity<VeterinaryRatingRequestDTO> getMyRating(
            @PathVariable Long id,
            @AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(veterinaryService.getUserRatingForVet(id, user.getId()));
    }

    @GetMapping("/me/custom-report")
    @Operation(summary = "[VET] Obter relatório customizado por período para o veterinário logado")
    public ResponseEntity<ReportSummaryDTO> getMyCustomReport(
            @AuthenticationPrincipal UserModel user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(veterinaryService.getCustomReportForVet(user, startDate, endDate));
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

    @PatchMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "[VET] Atualizar foto de perfil do veterinário logado")
    public ResponseEntity<Map<String, String>> updateMyPhoto(
            @AuthenticationPrincipal UserModel user,
            @RequestParam("file") MultipartFile file) throws IOException {

        String newImageUrl = veterinaryService.updateProfilePicture(user, file);
        return ResponseEntity.ok(Map.of("url", newImageUrl));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "[ADMIN] Deletar veterinário pelo ID")
    public ResponseEntity<Void> deleteVeterinary(@PathVariable Long id) {
        veterinaryService.deleteVeterinary(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me")
    @Operation(summary = "[VET] Atualizar meus próprios dados de perfil")
    public ResponseEntity<VeterinaryResponseDTO> updateMyProfile(
            @AuthenticationPrincipal UserModel user,
            @Valid @RequestBody VeterinaryRequestDTO requestDTO) {

        // 1. Busca o DTO do veterinário vinculado ao usuário logado
        VeterinaryResponseDTO vetDTO = veterinaryService.findVeterinaryByUserAccount(user);

        // 2. Chama o update usando vetDTO.id() (sem o "get")
        return ResponseEntity.ok(veterinaryService.updateVeterinary(vetDTO.id(), requestDTO));
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

    // NOVOS ENDPOINTS - FASE 3 E CORREÇÕES

    @PostMapping("/medical-records/{recordId}/attachments")
    @Operation(summary = "[VET] Anexar um arquivo a um prontuário médico")
    public ResponseEntity<Map<String, String>> uploadAttachment(
            @PathVariable Long recordId,
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = veterinaryService.addAttachmentToMedicalRecord(recordId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", url));
    }

    // --- NOVO ENDPOINT DE LISTAGEM (A CORREÇÃO PRINCIPAL) ---
    @GetMapping("/medical-records/{recordId}/attachments")
    @Operation(summary = "[VET/USER] Listar anexos de um prontuário")
    public ResponseEntity<List<MedicalAttachmentResponseDTO>> getAttachments(
            @PathVariable Long recordId) {
        return ResponseEntity.ok(veterinaryService.getAttachmentsByRecordId(recordId));
    }
    // --------------------------------------------------------

    @PostMapping("/consultations/{consultationId}/prescriptions")
    @Operation(summary = "[VET] Criar uma nova prescrição para uma consulta")
    public ResponseEntity<Void> createPrescription(
            @PathVariable Long consultationId,
            @RequestBody PrescriptionRequestDTO dto) { // Removido @Valid para permitir campos livres
        veterinaryService.createPrescription(consultationId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/prescription-templates")
    @Operation(summary = "[VET] Listar meus templates de prescrição")
    public ResponseEntity<List<PrescriptionTemplateResponseDTO>> getMyTemplates(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(veterinaryService.findMyTemplates(user));
    }

    @PostMapping("/prescription-templates")
    @Operation(summary = "[VET] Salvar uma nova prescrição como template")
    public ResponseEntity<PrescriptionTemplateResponseDTO> createTemplate(
            @Valid @RequestBody PrescriptionTemplateRequestDTO dto,
            @AuthenticationPrincipal UserModel user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(veterinaryService.createTemplate(dto, user));
    }

    @GetMapping("/prescriptions/{prescriptionId}/pdf")
    @Operation(summary = "[VET] Gerar PDF de uma prescrição")
    public ResponseEntity<byte[]> getPrescriptionPdf(@PathVariable Long prescriptionId) {
        try {
            byte[] pdfBytes = veterinaryService.generatePrescriptionPdf(prescriptionId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "prescricao_petvita_" + prescriptionId + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (DocumentException | IOException e) {
            System.err.println("Erro ao gerar PDF da prescrição: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/ratings/check/{vetId}")
    @Operation(summary = "[USER] Verificar minha avaliação para um veterinário específico")
    public ResponseEntity<VeterinaryRatingRequestDTO> checkMyRating(
            @PathVariable Long vetId,
            @AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(veterinaryService.getUserRatingForVet(vetId, user.getId()));
    }
}