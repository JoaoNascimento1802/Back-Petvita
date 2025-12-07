package sesi.petvita.veterinary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.repository.UserRepository;
import sesi.petvita.veterinary.dto.WorkScheduleDTO;
import sesi.petvita.veterinary.model.WorkSchedule;
import sesi.petvita.veterinary.repository.WorkScheduleRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final UserRepository userRepository;

    private WorkScheduleDTO toDTO(WorkSchedule model) {
        return new WorkScheduleDTO(
                model.getId(),
                model.getDayOfWeek(),
                model.getWorkDate(),
                model.getStartTime(),
                model.getEndTime(),
                model.isWorking()
        );
    }

    // 1. Padrão Semanal
    @Transactional(readOnly = true)
    public List<WorkScheduleDTO> getWeeklyTemplate(Long userId) {
        return workScheduleRepository.findByProfessionalUserId(userId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<WorkScheduleDTO> updateWeeklyTemplate(Long userId, List<WorkScheduleDTO> dtos) {
        List<WorkSchedule> existing = workScheduleRepository.findByProfessionalUserId(userId);
        for (WorkScheduleDTO dto : dtos) {
            existing.stream().filter(s -> s.getDayOfWeek() == dto.dayOfWeek()).findFirst().ifPresent(s -> {
                s.setStartTime(dto.startTime());
                s.setEndTime(dto.endTime());
                s.setWorking(dto.isWorking());
            });
        }
        workScheduleRepository.saveAll(existing);
        return getWeeklyTemplate(userId);
    }

    // 2. Calendário Mensal (Busca por intervalo)
    @Transactional(readOnly = true)
    public List<WorkScheduleDTO> getMonthlySchedule(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return workScheduleRepository.findByUserIdAndDateRange(userId, start, end)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // 3. Lista de Exceções Futuras (MÉTODO QUE FALTAVA E CAUSAVA O ERRO)
    @Transactional(readOnly = true)
    public List<WorkScheduleDTO> getSpecificSchedules(Long userId) {
        return workScheduleRepository.findSpecificSchedulesByUserId(userId, LocalDate.now())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // 4. Salvar Data Específica
    @Transactional
    public WorkScheduleDTO saveSpecificSchedule(Long userId, WorkScheduleDTO dto) {
        UserModel user = userRepository.findById(userId).orElseThrow();

        WorkSchedule schedule = workScheduleRepository.findByProfessionalUserIdAndWorkDate(userId, dto.workDate())
                .orElse(new WorkSchedule());

        schedule.setProfessionalUser(user);
        schedule.setWorkDate(dto.workDate());
        schedule.setDayOfWeek(dto.workDate().getDayOfWeek());
        schedule.setStartTime(dto.startTime());
        schedule.setEndTime(dto.endTime());
        schedule.setWorking(dto.isWorking());

        return toDTO(workScheduleRepository.save(schedule));
    }

    // 5. Deletar Data Específica
    @Transactional
    public void deleteSpecificSchedule(Long id) {
        workScheduleRepository.deleteById(id);
    }
}