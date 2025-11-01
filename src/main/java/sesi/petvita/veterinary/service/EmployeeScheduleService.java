package sesi.petvita.veterinary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.repository.UserRepository;
import sesi.petvita.veterinary.dto.EmployeeWorkScheduleDTO;
import sesi.petvita.veterinary.mapper.EmployeeWorkScheduleMapper;
import sesi.petvita.veterinary.model.WorkSchedule;
import sesi.petvita.veterinary.repository.WorkScheduleRepository;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final UserRepository userRepository;
    private final EmployeeWorkScheduleMapper employeeWorkScheduleMapper;

    @Transactional(readOnly = true)
    public List<EmployeeWorkScheduleDTO> getSchedulesForEmployee(Long employeeId) {
        if (!userRepository.existsById(employeeId)) {
            throw new NoSuchElementException("Funcionário não encontrado com o ID: " + employeeId);
        }

        List<WorkSchedule> schedules = workScheduleRepository.findByProfessionalUserId(employeeId);
        return schedules.stream()
                .map(employeeWorkScheduleMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<EmployeeWorkScheduleDTO> updateSchedules(Long employeeId, List<EmployeeWorkScheduleDTO> scheduleDTOs) {
        if (!userRepository.existsById(employeeId)) {
            throw new NoSuchElementException("Funcionário não encontrado com o ID: " + employeeId);
        }

        Map<Long, EmployeeWorkScheduleDTO> dtoMap = scheduleDTOs.stream()
                .collect(Collectors.toMap(EmployeeWorkScheduleDTO::id, dto -> dto));

        List<WorkSchedule> schedulesToUpdate = workScheduleRepository.findByProfessionalUserId(employeeId);

        schedulesToUpdate.forEach(schedule -> {
            EmployeeWorkScheduleDTO dto = dtoMap.get(schedule.getId());
            if (dto != null) {
                schedule.setStartTime(dto.startTime());
                schedule.setEndTime(dto.endTime());
                schedule.setWorking(dto.isWorking());
            }
        });

        List<WorkSchedule> updatedSchedules = workScheduleRepository.saveAll(schedulesToUpdate);
        return updatedSchedules.stream()
                .map(employeeWorkScheduleMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeWorkScheduleDTO> getSchedulesForAuthenticatedEmployee(UserModel user) {
        List<WorkSchedule> schedules = workScheduleRepository.findByProfessionalUserId(user.getId());
        return schedules.stream()
                .map(employeeWorkScheduleMapper::toDTO)
                .collect(Collectors.toList());
    }
}