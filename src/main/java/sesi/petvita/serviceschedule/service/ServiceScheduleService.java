// sesi/petvita/serviceschedule/service/ServiceScheduleService.java
package sesi.petvita.serviceschedule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.clinic.model.ClinicService;
import sesi.petvita.clinic.repository.ClinicServiceRepository;
import sesi.petvita.employee.dto.EmployeeDashboardSummaryDTO;
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
import sesi.petvita.veterinary.model.WorkSchedule;
import sesi.petvita.veterinary.repository.WorkScheduleRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
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
    private final WorkScheduleRepository workScheduleRepository;

    // --- CORREÇÃO: PADRONIZANDO STATUS PARA FEMININO (igual ConsultationStatus) ---
    private static final String STATUS_PENDENTE = "PENDENTE";
    private static final String STATUS_AGENDADA = "AGENDADA";
    private static final String STATUS_RECUSADA = "RECUSADA";
    private static final String STATUS_FINALIZADA = "FINALIZADA";
    // -------------------------------------------------------------------------

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
        // O status PENDENTE é definido por padrão na entidade
        ServiceScheduleModel savedSchedule = scheduleRepository.save(newSchedule);
        String notificationMessage = "Novo pedido de " + service.getName() + " para o pet " + pet.getName() + ".";
        notificationService.createNotification(employee, notificationMessage, null);

        return mapper.toDTO(savedSchedule);
    }

    @Transactional(readOnly = true)
    public List<ServiceScheduleResponseDTO> findForAuthenticatedUser(UserModel client) {
        return scheduleRepository.findByClientIdOrderByScheduleDateDesc(client.getId())
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceScheduleResponseDTO> findForEmployee(UserModel employee) {
        if (employee.getRole() != UserRole.EMPLOYEE && employee.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Acesso negado.");
        }
        List<ServiceScheduleModel> schedules = scheduleRepository.findAllByEmployeeIdWithDetails(employee.getId());
        return schedules.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceScheduleResponseDTO> findAllForAdmin() {
        return scheduleRepository.findAllWithDetails()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeDashboardSummaryDTO getDashboardSummary(UserModel employee) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        long servicesToday = scheduleRepository.countByEmployeeIdAndScheduleDate(employee.getId(), today);
        long servicesFinalizedThisMonth = scheduleRepository.countByEmployeeIdAndStatusAndScheduleDateBetween(employee.getId(), STATUS_FINALIZADA, startOfMonth, endOfMonth);
        return new EmployeeDashboardSummaryDTO(servicesToday, servicesFinalizedThisMonth);
    }

    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlotsForEmployee(Long employeeId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        WorkSchedule schedule = workScheduleRepository.findByProfessionalUserIdAndDayOfWeek(employeeId, dayOfWeek)
                .orElse(null);
        if (schedule == null || !schedule.isWorking() || schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return new ArrayList<>();
        }

        List<LocalTime> allPossibleSlots = new ArrayList<>();
        LocalTime currentSlot = schedule.getStartTime();
        long slotInterval = 45;

        while (currentSlot.isBefore(schedule.getEndTime())) {
            allPossibleSlots.add(currentSlot);
            currentSlot = currentSlot.plusMinutes(slotInterval);
        }

        List<LocalTime> bookedSlots = scheduleRepository.findBookedTimesByEmployeeAndDate(employeeId, date, List.of(STATUS_AGENDADA, STATUS_PENDENTE));
        return allPossibleSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptSchedule(Long scheduleId, UserModel employee) {
        ServiceScheduleModel schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("Agendamento de serviço não encontrado."));
        if (!schedule.getEmployee().getId().equals(employee.getId())) {
            throw new AccessDeniedException("Você não tem permissão para gerenciar este agendamento.");
        }
        if (!schedule.getStatus().equals(STATUS_PENDENTE)) {
            throw new IllegalStateException("Apenas agendamentos pendentes podem ser aceitos.");
        }

        // CORREÇÃO: Padronizado para feminino
        schedule.setStatus(STATUS_AGENDADA);
        scheduleRepository.save(schedule);
        notificationService.createNotification(
                schedule.getClient(),
                "Seu agendamento de " + schedule.getClinicService().getName() + " para o pet " + schedule.getPet().getName() + " foi confirmado!",
                null
        );
    }

    @Transactional
    public void rejectSchedule(Long scheduleId, UserModel employee) {
        ServiceScheduleModel schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("Agendamento de serviço não encontrado."));
        if (!schedule.getEmployee().getId().equals(employee.getId())) {
            throw new AccessDeniedException("Você não tem permissão para gerenciar este agendamento.");
        }
        if (!schedule.getStatus().equals(STATUS_PENDENTE)) {
            throw new IllegalStateException("Apenas agendamentos pendentes podem ser recusados.");
        }

        // CORREÇÃO: Padronizado para feminino
        schedule.setStatus(STATUS_RECUSADA);
        scheduleRepository.save(schedule);
        notificationService.createNotification(
                schedule.getClient(),
                "Seu agendamento de " + schedule.getClinicService().getName() + " para o pet " + schedule.getPet().getName() + " foi recusado.",
                null
        );
    }

    @Transactional(readOnly = true)
    public ServiceScheduleResponseDTO findScheduleByIdForEmployee(Long scheduleId, UserModel employee) {
        ServiceScheduleModel schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("Agendamento de serviço não encontrado."));
        if (!schedule.getEmployee().getId().equals(employee.getId())) {
            throw new AccessDeniedException("Você não tem permissão para visualizar este agendamento.");
        }

        return mapper.toDTO(schedule);
    }

    @Transactional
    public void addReport(Long scheduleId, String report, UserModel employee) {
        ServiceScheduleModel schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("Agendamento de serviço não encontrado."));
        if (!schedule.getEmployee().getId().equals(employee.getId())) {
            throw new AccessDeniedException("Você não tem permissão para editar este agendamento.");
        }

        schedule.setEmployeeReport(report);
        scheduleRepository.save(schedule);
    }

    @Transactional
    public void finalizeSchedule(Long scheduleId, UserModel employee) {
        ServiceScheduleModel schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("Agendamento de serviço não encontrado."));
        if (!schedule.getEmployee().getId().equals(employee.getId())) {
            throw new AccessDeniedException("Você não tem permissão para finalizar este agendamento.");
        }
        // CORREÇÃO: Padronizado para feminino
        if (!schedule.getStatus().equals(STATUS_AGENDADA)) {
            throw new IllegalStateException("Apenas serviços agendados podem ser finalizados.");
        }

        // CORREÇÃO: Padronizado para feminino
        schedule.setStatus(STATUS_FINALIZADA);
        scheduleRepository.save(schedule);
        notificationService.createNotification(
                schedule.getClient(),
                "O serviço de " + schedule.getClinicService().getName() + " para o pet " + schedule.getPet().getName() + " foi finalizado!",
                null
        );
    }
}