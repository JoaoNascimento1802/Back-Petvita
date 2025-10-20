package sesi.petvita.employee.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.employee.dto.EmployeeConsultationRequestDTO;
import sesi.petvita.employee.dto.TriageRequestDTO;
import sesi.petvita.employee.service.EmployeeService;
import sesi.petvita.pet.dto.PetRequestDTO;
import sesi.petvita.pet.dto.PetResponseDTO;
import sesi.petvita.pet.service.PetService;
import sesi.petvita.serviceschedule.dto.ServiceScheduleResponseDTO;
import sesi.petvita.serviceschedule.service.ServiceScheduleService;
import sesi.petvita.user.dto.UserRequestDTO;
import sesi.petvita.user.dto.UserResponseDTO;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.role.UserRole;
import sesi.petvita.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Endpoints para funcionários/recepcionistas")
public class EmployeeController {

    private final ServiceScheduleService serviceScheduleService;
    private final UserService userService;
    private final PetService petService;
    private final EmployeeService employeeService;

    @GetMapping("/my-schedules")
    @Operation(summary = "[EMPLOYEE] Listar meus agendamentos de serviços não-médicos")
    public ResponseEntity<List<ServiceScheduleResponseDTO>> getMySchedules(@AuthenticationPrincipal UserModel employee) {
        return ResponseEntity.ok(serviceScheduleService.findForEmployee(employee));
    }

    @GetMapping("/all")
    @Operation(summary = "[EMPLOYEE] Listar todos os funcionários da clínica")
    public ResponseEntity<List<UserResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(userService.findUsersByRole(UserRole.EMPLOYEE));
    }

    // Gerenciamento de Clientes (USER)
    @PostMapping("/users")
    @Operation(summary = "[EMPLOYEE] Cadastrar um novo cliente (tutor)")
    public ResponseEntity<UserResponseDTO> createClient(@Valid @RequestBody UserRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(dto));
    }

    // Gerenciamento de Pets
    @PostMapping("/pets")
    @Operation(summary = "[EMPLOYEE] Cadastrar um novo pet para um cliente")
    public ResponseEntity<PetResponseDTO> createPet(@Valid @RequestBody PetRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(petService.createPet(dto));
    }

    // Fluxo de Atendimento
    @PostMapping("/consultations/{id}/check-in")
    @Operation(summary = "[EMPLOYEE] Realizar check-in de uma consulta e adicionar dados de triagem")
    public ResponseEntity<Void> checkIn(@PathVariable Long id, @Valid @RequestBody TriageRequestDTO dto) {
        employeeService.performCheckIn(id, dto);
        return ResponseEntity.ok().build();
    }

    // Agendamento Assistido
    @PostMapping("/consultations")
    @Operation(summary = "[EMPLOYEE] Agendar uma nova consulta para um cliente")
    public ResponseEntity<Void> scheduleConsultation(@Valid @RequestBody EmployeeConsultationRequestDTO dto) {
        employeeService.scheduleByEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}