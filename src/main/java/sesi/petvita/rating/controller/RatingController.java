package sesi.petvita.rating.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.employee.service.EmployeeService;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.dto.VeterinaryRatingRequestDTO;
import sesi.petvita.veterinary.service.VeterinaryService;

@RestController
@RequestMapping("/api/ratings") // Rota Centralizada
@RequiredArgsConstructor
@Tag(name = "Avaliações", description = "Endpoints unificados para avaliações")
public class RatingController {

    private final VeterinaryService veterinaryService;
    private final EmployeeService employeeService;

    // --- AVALIAÇÃO DE VETERINÁRIO ---

    @GetMapping("/veterinary/{vetId}")
    @Operation(summary = "Verificar minha avaliação para um veterinário")
    public ResponseEntity<VeterinaryRatingRequestDTO> getMyVetRating(
            @PathVariable Long vetId,
            @AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(veterinaryService.getUserRatingForVet(vetId, user.getId()));
    }

    @PostMapping("/veterinary/{vetId}")
    @Operation(summary = "Avaliar um veterinário")
    public ResponseEntity<Void> rateVet(
            @PathVariable Long vetId,
            @RequestBody VeterinaryRatingRequestDTO dto,
            @AuthenticationPrincipal UserModel user) {
        veterinaryService.addRating(vetId, user.getId(), dto);
        return ResponseEntity.ok().build();
    }

    // --- AVALIAÇÃO DE FUNCIONÁRIO ---

    @GetMapping("/employee/{empId}")
    @Operation(summary = "Verificar minha avaliação para um funcionário")
    public ResponseEntity<VeterinaryRatingRequestDTO> getMyEmployeeRating(
            @PathVariable Long empId,
            @AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(employeeService.getRatingByUser(empId, user.getId()));
    }

    @PostMapping("/employee/{empId}")
    @Operation(summary = "Avaliar um funcionário")
    public ResponseEntity<Void> rateEmployee(
            @PathVariable Long empId,
            @RequestBody VeterinaryRatingRequestDTO dto,
            @AuthenticationPrincipal UserModel user) {
        employeeService.addRating(empId, user.getId(), dto);
        return ResponseEntity.ok().build();
    }
}
