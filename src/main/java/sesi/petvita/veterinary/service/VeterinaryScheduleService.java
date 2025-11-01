package sesi.petvita.veterinary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.dto.VeterinaryWorkScheduleDTO;
import sesi.petvita.veterinary.mapper.VeterinaryWorkScheduleMapper;
import sesi.petvita.veterinary.model.WorkSchedule;
import sesi.petvita.veterinary.repository.VeterinaryRepository;
import sesi.petvita.veterinary.repository.WorkScheduleRepository;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VeterinaryScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final VeterinaryRepository veterinaryRepository;
    private final VeterinaryWorkScheduleMapper veterinaryWorkScheduleMapper;

    @Transactional(readOnly = true)
    public List<VeterinaryWorkScheduleDTO> getSchedulesForVeterinary(Long vetId) {
        Long userId = veterinaryRepository.findById(vetId)
                .orElseThrow(() -> new NoSuchElementException("Veterinário não encontrado com o ID: " + vetId))
                .getUserAccount().getId();

        List<WorkSchedule> schedules = workScheduleRepository.findByProfessionalUserId(userId);
        return schedules.stream()
                .map(veterinaryWorkScheduleMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<VeterinaryWorkScheduleDTO> updateSchedules(Long vetId, List<VeterinaryWorkScheduleDTO> scheduleDTOs) {
        Long userId = veterinaryRepository.findById(vetId)
                .orElseThrow(() -> new NoSuchElementException("Veterinário não encontrado com o ID: " + vetId))
                .getUserAccount().getId();

        Map<Long, VeterinaryWorkScheduleDTO> dtoMap = scheduleDTOs.stream()
                .collect(Collectors.toMap(VeterinaryWorkScheduleDTO::id, dto -> dto));

        List<WorkSchedule> schedulesToUpdate = workScheduleRepository.findByProfessionalUserId(userId);

        schedulesToUpdate.forEach(schedule -> {
            VeterinaryWorkScheduleDTO dto = dtoMap.get(schedule.getId());
            if (dto != null) {
                schedule.setStartTime(dto.startTime());
                schedule.setEndTime(dto.endTime());
                schedule.setWorking(dto.isWorking());
            }
        });

        List<WorkSchedule> updatedSchedules = workScheduleRepository.saveAll(schedulesToUpdate);
        return updatedSchedules.stream()
                .map(veterinaryWorkScheduleMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VeterinaryWorkScheduleDTO> getSchedulesForAuthenticatedVeterinary(UserModel user) {
        // Encontra o perfil de veterinário associado à conta de usuário logada
        Long userId = user.getId();
        List<WorkSchedule> schedules = workScheduleRepository.findByProfessionalUserId(userId);
        return schedules.stream()
                .map(veterinaryWorkScheduleMapper::toDTO)
                .collect(Collectors.toList());
    }
}