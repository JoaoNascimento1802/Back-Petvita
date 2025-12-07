package sesi.petvita.employee.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.consultation.dto.ConsultationRequestDTO;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.consultation.repository.ConsultationRepository;
import sesi.petvita.consultation.service.ConsultationService;
import sesi.petvita.consultation.status.ConsultationStatus;
import sesi.petvita.employee.dto.EmployeeConsultationRequestDTO;
import sesi.petvita.employee.dto.TriageRequestDTO;
import sesi.petvita.employee.model.EmployeeRating;
import sesi.petvita.employee.model.TriageInfoModel;
import sesi.petvita.employee.repository.EmployeeRatingRepository;
import sesi.petvita.employee.repository.TriageInfoRepository;
import sesi.petvita.serviceschedule.repository.ServiceScheduleRepository; // Import novo
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.repository.UserRepository;
import sesi.petvita.veterinary.dto.VeterinaryRatingRequestDTO;
import sesi.petvita.veterinary.model.WorkSchedule;
import sesi.petvita.veterinary.repository.WorkScheduleRepository; // Import novo

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final ConsultationRepository consultationRepository;
    private final TriageInfoRepository triageInfoRepository;
    private final ConsultationService consultationService;
    private final UserRepository userRepository;
    private final EmployeeRatingRepository ratingRepository;
    private final WorkScheduleRepository workScheduleRepository; // Injetado
    private final ServiceScheduleRepository serviceScheduleRepository; // Injetado

    // --- NOVO MÉTODO PARA CALCULAR HORÁRIOS DISPONÍVEIS ---
    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlots(Long employeeId, LocalDate date) {
        // 1. Busca a escala de trabalho do funcionário para aquele dia
        WorkSchedule schedule = workScheduleRepository.findByProfessionalUserIdAndWorkDate(employeeId, date)
                .orElse(null);

        // Se não tiver escala específica, busca a padrão pelo dia da semana
        if (schedule == null) {
            schedule = workScheduleRepository.findByProfessionalUserIdAndDayOfWeek(employeeId, date.getDayOfWeek())
                    .orElse(null);
        }

        // Se não tiver escala ou não estiver trabalhando nesse dia
        if (schedule == null || !schedule.isWorking()) {
            return new ArrayList<>();
        }

        // 2. Gera todos os slots possíveis (Ex: a cada 1 hora)
        List<LocalTime> allSlots = new ArrayList<>();
        LocalTime current = schedule.getStartTime();
        // Assume duração padrão de 1 hora para serviços (pode ajustar para 30min ou parametrizar)
        while (current.isBefore(schedule.getEndTime())) {
            allSlots.add(current);
            current = current.plusHours(1); // Incremento de 1h
        }

        // 3. Busca os horários JÁ OCUPADOS
        // Filtra apenas status ativos (não cancelados/recusados)
        List<String> activeStatuses = List.of("PENDENTE", "ACEITO", "EM_ANDAMENTO", "CONCLUIDO");
        List<LocalTime> bookedSlots = serviceScheduleRepository.findBookedTimesByEmployeeAndDate(employeeId, date, activeStatuses);

        // 4. Filtra os disponíveis
        List<LocalTime> available = allSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());

        // 5. Se for hoje, remove horários que já passaram
        if (date.equals(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            available = available.stream().filter(t -> t.isAfter(now)).collect(Collectors.toList());
        }

        return available;
    }
    // -----------------------------------------------------

    @Transactional
    public void performCheckIn(Long consultationId, TriageRequestDTO dto) {
        ConsultationModel consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new NoSuchElementException("Consulta não encontrada com o ID: " + consultationId));

        if (consultation.getStatus() != ConsultationStatus.AGENDADA) {
            throw new IllegalStateException("Apenas consultas agendadas podem passar por check-in.");
        }

        TriageInfoModel triageInfo = TriageInfoModel.builder()
                .consultation(consultation)
                .weightKg(dto.weightKg())
                .temperatureCelsius(dto.temperatureCelsius())
                .mainComplaint(dto.mainComplaint())
                .build();
        triageInfoRepository.save(triageInfo);

        consultation.setStatus(ConsultationStatus.CHECKED_IN);
        consultationRepository.save(consultation);
    }

    @Transactional
    public void scheduleByEmployee(EmployeeConsultationRequestDTO dto) {
        UserModel client = userRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new NoSuchElementException("Cliente não encontrado com o ID: " + dto.usuarioId()));

        ConsultationRequestDTO userDto = new ConsultationRequestDTO(
                dto.consultationdate(),
                dto.consultationtime(),
                dto.clinicServiceId(),
                dto.reason(),
                dto.observations(),
                dto.petId(),
                dto.usuarioId(),
                dto.veterinarioId()
        );

        consultationService.create(userDto, client);
    }

    @Transactional
    public void addRating(Long employeeId, Long userId, VeterinaryRatingRequestDTO dto) {
        UserModel employee = userRepository.findById(employeeId).orElseThrow();
        UserModel user = userRepository.findById(userId).orElseThrow();

        EmployeeRating rating = ratingRepository.findByEmployeeIdAndUserId(employeeId, userId)
                .orElse(new EmployeeRating());

        rating.setEmployee(employee);
        rating.setUser(user);
        rating.setRating(dto.rating());
        rating.setComment(dto.comment());

        ratingRepository.save(rating);
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(Long employeeId) {
        List<EmployeeRating> ratings = ratingRepository.findByEmployeeId(employeeId);
        if (ratings.isEmpty()) return 0.0;
        return ratings.stream().mapToDouble(EmployeeRating::getRating).average().orElse(0.0);
    }

    @Transactional(readOnly = true)
    public VeterinaryRatingRequestDTO getRatingByUser(Long employeeId, Long userId) {
        return ratingRepository.findByEmployeeIdAndUserId(employeeId, userId)
                .map(r -> new VeterinaryRatingRequestDTO(r.getRating(), r.getComment()))
                .orElse(null);
    }
}