package sesi.petvita.pet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.pet.dto.PetRequestDTO;
import sesi.petvita.pet.dto.PetResponseDTO;
import sesi.petvita.pet.service.PetService; // Importa o novo service
import sesi.petvita.user.model.UserModel;

import java.util.List;

// ARQUIVO MODIFICADO
@RestController
@RequestMapping("/pets")
@Tag(name = "Pets", description = "Endpoints relacionados aos pets dos usuários")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService; // Injeta o service

    @GetMapping
    @Operation(summary = "Listar todos os pets")
    public ResponseEntity<List<PetResponseDTO>> getAllPets() {
        return ResponseEntity.ok(petService.findAllPets());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pet por ID")
    public ResponseEntity<PetResponseDTO> getPetById(@PathVariable Long id) {
        return ResponseEntity.ok(petService.findPetById(id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar um novo pet")
    public ResponseEntity<PetResponseDTO> createPet(@Valid @RequestBody PetRequestDTO petDto) {
        // ===== LINHA DE DEBUG 1 =====
        System.out.println(">>> [CONTROLLER] DTO Recebido: " + petDto.toString());
        // ============================
        PetResponseDTO createdPet = petService.createPet(petDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPet);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pet pelo ID")
    public ResponseEntity<PetResponseDTO> updatePet(@PathVariable Long id, @Valid @RequestBody PetRequestDTO petDto) {
        PetResponseDTO updatedPet = petService.updatePet(id, petDto);
        return ResponseEntity.ok(updatedPet);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar pet pelo ID")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        petService.deletePet(id);
        return ResponseEntity.noContent().build();
    }

    // Dentro da classe PetController
    @GetMapping("/my-pets")
    @Operation(summary = "Listar os pets do usuário autenticado")
    public ResponseEntity<List<PetResponseDTO>> getMyPets(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(petService.findPetsByUser(user));
    }
}