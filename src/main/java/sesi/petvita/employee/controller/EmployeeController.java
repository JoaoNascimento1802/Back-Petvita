package sesi.petvita.employee.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sesi.petvita.serviceschedule.dto.ServiceScheduleResponseDTO;
import sesi.petvita.serviceschedule.service.ServiceScheduleService;
import sesi.petvita.user.dto.UserResponseDTO;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.role.UserRole;
import sesi.petvita.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final ServiceScheduleService serviceScheduleService;
    private final UserService userService; // Adicionado para buscar funcionários

    // Endpoint para o painel do funcionário (protegido)
    @GetMapping("/my-schedules")
    public ResponseEntity<List<ServiceScheduleResponseDTO>> getMySchedules(@AuthenticationPrincipal UserModel employee) {
        return ResponseEntity.ok(serviceScheduleService.findForEmployee(employee));
    }

    // NOVO ENDPOINT PÚBLICO para listar todos os funcionários
    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(userService.findUsersByRole(UserRole.EMPLOYEE));
    }
}