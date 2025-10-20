package sesi.petvita.employee.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.consultation.repository.ConsultationRepository;
import sesi.petvita.consultation.service.ConsultationService;
import sesi.petvita.consultation.status.ConsultationStatus;
import sesi.petvita.employee.dto.EmployeeConsultationRequestDTO;
import sesi.petvita.employee.dto.TriageRequestDTO;
import sesi.petvita.employee.model.TriageInfoModel;
import sesi.petvita.employee.repository.TriageInfoRepository;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.repository.UserRepository;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final ConsultationRepository consultationRepository;
    private final TriageInfoRepository triageInfoRepository;
    private final ConsultationService consultationService;
    private final UserRepository userRepository;

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

        // Opcional: Notificar o veterinário que o paciente chegou.
    }

    @Transactional
    public void scheduleByEmployee(EmployeeConsultationRequestDTO dto) {
        UserModel client = userRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new NoSuchElementException("Cliente não encontrado com o ID: " + dto.usuarioId()));

        // A lógica de criação de consulta já existe, vamos reutilizá-la
        // Convertendo o DTO do funcionário para o DTO que o serviço espera
        sesi.petvita.consultation.dto.ConsultationRequestDTO userDto = new sesi.petvita.consultation.dto.ConsultationRequestDTO(
                dto.consultationdate(),
                dto.consultationtime(),
                dto.clinicServiceId(),
                null, // Status é definido pelo serviço
                dto.reason(),
                dto.observations(),
                dto.petId(),
                dto.usuarioId(),
                dto.veterinarioId()
        );

        consultationService.create(userDto, client);
    }
}