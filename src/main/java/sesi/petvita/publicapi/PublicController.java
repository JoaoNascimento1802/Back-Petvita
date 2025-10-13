package sesi.petvita.publicapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sesi.petvita.clinic.model.ClinicService;
import sesi.petvita.clinic.service.ClinicServiceService;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final ClinicServiceService clinicServiceService;

    /**
     * Endpoint público para listar todos os serviços da clínica.
     * Usado na página de agendamento para que os clientes possam ver as opções disponíveis.
     */
    @GetMapping("/services")
    public ResponseEntity<List<ClinicService>> getAllServices() {
        return ResponseEntity.ok(clinicServiceService.findAll());
    }
}
