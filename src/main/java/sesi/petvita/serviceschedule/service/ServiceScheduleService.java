package sesi.petvita.serviceschedule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException; // IMPORT CORRIGIDO
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.clinic.model.ClinicService;
import sesi.petvita.clinic.repository.ClinicServiceRepository;
import sesi.petvita.notification.service.NotificationService;
import sesi.petvita.pet.model.PetModel;
import sesi.petvita.pet.repository.PetRepository;
import sesi.petvita.serviceschedule.dto.ServiceScheduleRequestDTO;
import sesi.petvita.serviceschedule.dto.ServiceScheduleResponseDTO;
import sesi.petvita.serviceschedule.mapper.ServiceScheduleMapper;
import sesi.petvita.serviceschedule.model.ServiceScheduleModel;
import sesi.petvita.serviceschedule.repository.ServiceScheduleRepository;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.repository.UserRepository;
import sesi.petvita.user.role.UserRole;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceScheduleService {

    private final ServiceScheduleRepository scheduleRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final ClinicServiceRepository clinicServiceRepository;
    private final ServiceScheduleMapper mapper;
    private final NotificationService notificationService;

    @Transactional
    public ServiceScheduleResponseDTO create(ServiceScheduleRequestDTO dto, UserModel client) {
        PetModel pet = petRepository.findById(dto.petId())
                .orElseThrow(() -> new NoSuchElementException("Pet não encontrado com o ID: " + dto.petId()));

        ClinicService service = clinicServiceRepository.findById(dto.clinicServiceId())
                .orElseThrow(() -> new NoSuchElementException("Serviço não encontrado com o ID: " + dto.clinicServiceId()));

        UserModel employee = userRepository.findById(dto.employeeId())
                .orElseThrow(() -> new NoSuchElementException("Funcionário não encontrado com o ID: " + dto.employeeId()));

        if (employee.getRole() != UserRole.EMPLOYEE) {
            throw new IllegalStateException("O profissional selecionado não é um funcionário válido para este serviço.");
        }

        ServiceScheduleModel newSchedule = mapper.toModel(dto, pet, client, employee, service);
        ServiceScheduleModel savedSchedule = scheduleRepository.save(newSchedule);

        String notificationMessage = "Novo agendamento de " + service.getName() + " para o pet " + pet.getName() + ".";
        notificationService.createNotification(employee, notificationMessage, null);

        return mapper.toDTO(savedSchedule);
    }

    public List<ServiceScheduleResponseDTO> findForEmployee(UserModel employee) {
        if (employee.getRole() != UserRole.EMPLOYEE) {
            // EXCEÇÃO CORRIGIDA: Agora usamos a exceção de segurança do Spring.
            throw new AccessDeniedException("Acesso negado. Apenas funcionários podem ver seus agendamentos.");
        }

        List<ServiceScheduleModel> schedules = scheduleRepository.findByEmployeeIdOrderByScheduleDateDescScheduleTimeDesc(employee.getId());

        return schedules.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
}

