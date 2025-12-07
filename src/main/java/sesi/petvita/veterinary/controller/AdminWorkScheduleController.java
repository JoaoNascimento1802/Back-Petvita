package sesi.petvita.veterinary.controller; // Note o pacote

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.veterinary.dto.WorkScheduleDTO;
import sesi.petvita.veterinary.service.WorkScheduleService; // Novo Service que criaremos abaixo

import java.util.List;

@RestController
@RequestMapping("/admin/schedules")
@RequiredArgsConstructor
@Tag(name = "Gestão de Horários", description = "Gerencia escalas semanais e dias específicos")
public class AdminWorkScheduleController {

    private final WorkScheduleService workScheduleService;

    // 1. Obter Padrão Semanal (7 dias)
    @GetMapping("/template/{userId}")
    public ResponseEntity<List<WorkScheduleDTO>> getWeeklyTemplate(@PathVariable Long userId) {
        return ResponseEntity.ok(workScheduleService.getWeeklyTemplate(userId));
    }

    // 2. Salvar Padrão Semanal
    @PutMapping("/template/{userId}")
    public ResponseEntity<List<WorkScheduleDTO>> updateWeeklyTemplate(
            @PathVariable Long userId,
            @RequestBody List<WorkScheduleDTO> dtos) {
        return ResponseEntity.ok(workScheduleService.updateWeeklyTemplate(userId, dtos));
    }

    // 3. Obter Dias Específicos (Exceções Futuras)
    @GetMapping("/specific/{userId}")
    public ResponseEntity<List<WorkScheduleDTO>> getSpecificSchedules(@PathVariable Long userId) {
        return ResponseEntity.ok(workScheduleService.getSpecificSchedules(userId));
    }

    // 4. Criar/Atualizar Dia Específico
    @PostMapping("/specific/{userId}")
    public ResponseEntity<WorkScheduleDTO> saveSpecificSchedule(
            @PathVariable Long userId,
            @RequestBody WorkScheduleDTO dto) {
        return ResponseEntity.ok(workScheduleService.saveSpecificSchedule(userId, dto));
    }
    
    // 5. Deletar Dia Específico (Volta ao padrão semanal)
    @DeleteMapping("/specific/{scheduleId}")
    public ResponseEntity<Void> deleteSpecificSchedule(@PathVariable Long scheduleId) {
        workScheduleService.deleteSpecificSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    // 6. Obter Horários do Mês (Datas Específicas)
    @GetMapping("/monthly/{userId}")
    public ResponseEntity<List<WorkScheduleDTO>> getMonthlySchedules(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(workScheduleService.getMonthlySchedule(userId, year, month));
    }
}
