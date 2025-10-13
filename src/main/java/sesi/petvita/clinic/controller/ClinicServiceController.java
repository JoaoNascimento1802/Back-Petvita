package sesi.petvita.clinic.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.clinic.dto.ClinicServiceRequestDTO;
import sesi.petvita.clinic.model.ClinicService;
import sesi.petvita.clinic.service.ClinicServiceService;

import java.util.List;

@RestController
@RequestMapping("/admin/clinic-services")
@RequiredArgsConstructor
@Tag(name = "Clinic Services", description = "Endpoints [ADMIN] para gerenciar os serviços da clínica")
public class ClinicServiceController {

    private final ClinicServiceService clinicServiceService;

    @GetMapping
    public ResponseEntity<List<ClinicService>> getAllServices() {
        return ResponseEntity.ok(clinicServiceService.findAll());
    }

    @PostMapping
    public ResponseEntity<ClinicService> createService(@Valid @RequestBody ClinicServiceRequestDTO serviceDto) {
        return ResponseEntity.ok(clinicServiceService.save(serviceDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClinicService> updateService(@PathVariable Long id, @Valid @RequestBody ClinicServiceRequestDTO serviceDto) {
        return ResponseEntity.ok(clinicServiceService.update(id, serviceDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        clinicServiceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}