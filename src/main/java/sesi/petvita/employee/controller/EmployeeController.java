package sesi.petvita.employee.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sesi.petvita.serviceschedule.dto.ServiceScheduleResponseDTO;
import sesi.petvita.serviceschedule.service.ServiceScheduleService;
import sesi.petvita.user.model.UserModel;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final ServiceScheduleService serviceScheduleService;

    @GetMapping("/my-schedules")
    public ResponseEntity<List<ServiceScheduleResponseDTO>> getMySchedules(@AuthenticationPrincipal UserModel employee) {
        return ResponseEntity.ok(serviceScheduleService.findForEmployee(employee));
    }
}
