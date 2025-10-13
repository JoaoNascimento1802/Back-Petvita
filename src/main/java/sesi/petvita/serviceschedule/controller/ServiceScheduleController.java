package sesi.petvita.serviceschedule.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sesi.petvita.serviceschedule.dto.ServiceScheduleRequestDTO;
import sesi.petvita.serviceschedule.dto.ServiceScheduleResponseDTO;
import sesi.petvita.serviceschedule.service.ServiceScheduleService;
import sesi.petvita.user.model.UserModel;

@RestController
@RequestMapping("/api/service-schedules")
@RequiredArgsConstructor
public class ServiceScheduleController {

    private final ServiceScheduleService serviceScheduleService;

    @PostMapping
    public ResponseEntity<ServiceScheduleResponseDTO> createSchedule(
            @Valid @RequestBody ServiceScheduleRequestDTO requestDTO,
            @AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(serviceScheduleService.create(requestDTO, user));
    }
}
