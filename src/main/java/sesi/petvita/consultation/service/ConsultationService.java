package sesi.petvita.consultation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.clinic.model.ClinicService;
import sesi.petvita.clinic.repository.ClinicServiceRepository;
import sesi.petvita.consultation.dto.ConsultationRequestDTO;
import sesi.petvita.consultation.dto.ConsultationResponseDTO;
import sesi.petvita.consultation.dto.ConsultationUpdateRequestDTO;
import sesi.petvita.consultation.mapper.ConsultationMapper;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.consultation.repository.ConsultationRepository;
import sesi.petvita.consultation.status.ConsultationStatus;
import sesi.petvita.notification.service.EmailService;
import sesi.petvita.notification.service.NotificationService;
import sesi.petvita.pet.model.PetModel;
import sesi.petvita.pet.repository.PetRepository;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.role.UserRole;
import sesi.petvita.veterinary.model.VeterinaryModel;
import sesi.petvita.veterinary.model.WorkSchedule;
import sesi.petvita.veterinary.repository.VeterinaryRepository;
import sesi.petvita.veterinary.repository.WorkScheduleRepository;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final PetRepository petRepository;
    private final VeterinaryRepository veterinaryRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final ClinicServiceRepository clinicServiceRepository;
    private final ConsultationMapper consultationMapper;
    private final NotificationService notificationService;
    private final EmailService emailService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private Map<String, Object> createEmailModel(String titulo, String nomeDestinatario, String corpoMensagem, ConsultationModel consultation) {
        Map<String, Object> model = new HashMap<>();
        model.put("titulo", titulo);
        model.put("nomeUsuario", nomeDestinatario);
        model.put("corpoMensagem", corpoMensagem);

        if (consultation != null) {
            model.put("mostrarDetalhesConsulta", true);
            model.put("nomePet", consultation.getPet().getName());
            model.put("nomeVeterinario", consultation.getVeterinario().getName());
            model.put("dataConsulta", consultation.getConsultationdate().format(dateFormatter));
            model.put("horarioConsulta", consultation.getConsultationtime().format(timeFormatter));
        } else {
            model.put("mostrarDetalhesConsulta", false);
        }
        return model;
    }

    @Transactional
    public ConsultationResponseDTO create(ConsultationRequestDTO dto, UserModel user) {
        ClinicService service = clinicServiceRepository.findById(dto.clinicServiceId())
                .orElseThrow(() -> new NoSuchElementException("Serviço não encontrado com o ID: " + dto.clinicServiceId()));
        VeterinaryModel vet = veterinaryRepository.findById(dto.veterinarioId())
                .orElseThrow(() -> new NoSuchElementException("Veterinário não encontrado com o ID: " + dto.veterinarioId()));
        if (vet.getUserAccount() == null) {
            throw new IllegalStateException("Este veterinário não possui uma conta de usuário ativa para receber notificações.");
        }

        DayOfWeek dayOfWeek = dto.consultationdate().getDayOfWeek();

        // --- CORREÇÃO APLICADA AQUI ---
        // A busca agora é feita pelo ID da conta de usuário (professional_user_id) do veterinário
        WorkSchedule schedule = workScheduleRepository.findByProfessionalUserIdAndDayOfWeek(vet.getUserAccount().getId(), dayOfWeek)
                .orElseThrow(() -> new IllegalStateException("Configuração de agenda não encontrada para este dia."));

        if (!schedule.isWorking() || dto.consultationtime().isBefore(schedule.getStartTime()) || dto.consultationtime().isAfter(schedule.getEndTime().minusMinutes(30))) {
            throw new IllegalStateException("O veterinário não atende no dia ou horário selecionado. Horário de atendimento: " + schedule.getStartTime() + " - " + schedule.getEndTime());
        }

        if (consultationRepository.existsByVeterinarioIdAndConsultationdateAndConsultationtime(dto.veterinarioId(), dto.consultationdate(), dto.consultationtime())) {
            throw new IllegalStateException("Conflito de horário. O veterinário já possui uma consulta neste horário.");
        }

        PetModel pet = petRepository.findById(dto.petId())
                .orElseThrow(() -> new NoSuchElementException("Pet não encontrado com o ID: " + dto.petId()));

        ConsultationModel newConsultation = consultationMapper.toModel(dto, pet, user, vet, service);
        ConsultationModel savedConsultation = consultationRepository.save(newConsultation);
        String corpoEmailVet = "Você recebeu uma nova solicitação de consulta de " + user.getActualUsername() + ". Por favor, acesse o painel para aceitar ou recusar.";
        Map<String, Object> emailModel = createEmailModel("Nova Solicitação de Consulta", vet.getName(), corpoEmailVet, savedConsultation);
        emailService.sendHtmlEmailFromTemplate(vet.getUserAccount().getEmail(), "Nova Solicitação de Consulta - Pet Vita", emailModel);
        return consultationMapper.toDTO(savedConsultation);
    }

    @Transactional
    public void acceptConsultation(Long consultationId) {
        ConsultationModel consultation = findByIdOrThrow(consultationId);
        if (consultation.getStatus() != ConsultationStatus.PENDENTE) {
            throw new IllegalStateException("Apenas consultas com status 'PENDENTE' podem ser aceitas.");
        }
        consultation.setStatus(ConsultationStatus.AGENDADA);
        consultationRepository.save(consultation);
        notificationService.createNotification(consultation.getUsuario(), "Sua consulta para " + consultation.getPet().getName() + " foi agendada!", consultation.getId());
        String corpoEmailCliente = "Sua solicitação de consulta foi aceita pelo(a) Dr(a). " + consultation.getVeterinario().getName() + ". Estamos ansiosos para ver você e seu pet!";
        Map<String, Object> emailModel = createEmailModel("Consulta Confirmada!", consultation.getUsuario().getActualUsername(), corpoEmailCliente, consultation);
        emailService.sendHtmlEmailFromTemplate(consultation.getUsuario().getEmail(), "Sua Consulta foi Confirmada - Pet Vita", emailModel);
    }

    @Transactional
    public void rejectConsultation(Long consultationId) {
        ConsultationModel consultation = findByIdOrThrow(consultationId);
        if (consultation.getStatus() != ConsultationStatus.PENDENTE) {
            throw new IllegalStateException("Apenas consultas com status 'PENDENTE' podem ser recusadas.");
        }
        consultation.setStatus(ConsultationStatus.RECUSADA);
        consultationRepository.save(consultation);
        notificationService.createNotification(consultation.getUsuario(), "Sua solicitação de consulta para " + consultation.getPet().getName() + " foi recusada.", consultation.getId());
        String corpo = "Infelizmente, sua solicitação de consulta para o pet " + consultation.getPet().getName() + " não pôde ser aceita no momento. Por favor, tente agendar um novo horário.";
        Map<String, Object> emailModel = createEmailModel("Solicitação de Consulta Recusada", consultation.getUsuario().getActualUsername(), corpo, null);
        emailService.sendHtmlEmailFromTemplate(consultation.getUsuario().getEmail(), "Solicitação de Consulta Recusada - Pet Vita", emailModel);
    }

    @Transactional
    public void cancelConsultation(Long consultationId, UserModel userCanceling) {
        ConsultationModel consultation = findByIdOrThrow(consultationId);
        if (consultation.getStatus() != ConsultationStatus.AGENDADA) {
            throw new IllegalStateException("Apenas consultas 'AGENDADAS' podem ser canceladas.");
        }

        UserModel targetUserToNotify;
        String recipientEmail, recipientName, emailTitle, emailBody;
        if (userCanceling.getRole() == UserRole.USER) {
            if (!consultation.getUsuario().getId().equals(userCanceling.getId())) {
                throw new IllegalStateException("Você só pode cancelar suas próprias consultas.");
            }
            targetUserToNotify = consultation.getVeterinario().getUserAccount();
            recipientEmail = targetUserToNotify.getEmail();
            recipientName = targetUserToNotify.getUsername();
            emailTitle = "Consulta Cancelada pelo Cliente";
            emailBody = "Uma consulta agendada com você foi cancelada pelo cliente (" + userCanceling.getActualUsername() + "). Por favor, verifique sua agenda.";
        } else if (userCanceling.getRole() == UserRole.VETERINARY) {
            if (!consultation.getVeterinario().getUserAccount().getId().equals(userCanceling.getId())) {
                throw new IllegalStateException("Você só pode cancelar suas próprias consultas.");
            }
            targetUserToNotify = consultation.getUsuario();
            recipientEmail = targetUserToNotify.getEmail();
            recipientName = targetUserToNotify.getUsername();
            emailTitle = "Consulta Cancelada pelo Veterinário";
            emailBody = "Sua consulta foi cancelada pelo veterinário. Por favor, entre em contato ou agende um novo horário.";
        } else {
            throw new IllegalStateException("Ação de cancelamento não permitida para este usuário.");
        }

        consultation.setStatus(ConsultationStatus.CANCELADA);
        consultationRepository.save(consultation);
        notificationService.createNotification(targetUserToNotify, "A consulta para " + consultation.getPet().getName() + " foi cancelada.", consultation.getId());

        Map<String, Object> emailModel = createEmailModel(emailTitle, recipientName, emailBody, consultation);
        emailService.sendHtmlEmailFromTemplate(recipientEmail, emailTitle + " - Pet Vita", emailModel);
    }

    @Transactional
    public ConsultationResponseDTO updateConsultation(Long consultationId, ConsultationUpdateRequestDTO dto, UserModel user) {
        ConsultationModel consultation = findByIdOrThrow(consultationId);
        if (!consultation.getUsuario().getId().equals(user.getId())) {
            throw new IllegalStateException("Você só pode editar suas próprias consultas.");
        }

        if (dto.consultationdate() != null) consultation.setConsultationdate(dto.consultationdate());
        if (dto.consultationtime() != null) consultation.setConsultationtime(dto.consultationtime());
        if (dto.reason() != null) consultation.setReason(dto.reason());
        if (dto.observations() != null) consultation.setObservations(dto.observations());

        ConsultationModel updatedConsultation = consultationRepository.save(consultation);
        String corpo = "Os detalhes de uma consulta agendada com você foram alterados pelo cliente. Verifique as novas informações:";
        Map<String, Object> emailModel = createEmailModel("Consulta Alterada pelo Cliente", updatedConsultation.getVeterinario().getName(), corpo, updatedConsultation);
        emailService.sendHtmlEmailFromTemplate(updatedConsultation.getVeterinario().getUserAccount().getEmail(), "Alteração de Consulta - Pet Vita", emailModel);
        return consultationMapper.toDTO(updatedConsultation);
    }

    @Transactional
    public void finalizeConsultation(Long consultationId) {
        ConsultationModel consultation = findByIdOrThrow(consultationId);
        if (consultation.getStatus() != ConsultationStatus.AGENDADA) {
            throw new IllegalStateException("Apenas consultas 'AGENDADAS' podem ser finalizadas.");
        }
        consultation.setStatus(ConsultationStatus.FINALIZADA);
        consultationRepository.save(consultation);
        notificationService.createNotification(consultation.getUsuario(), "Sua consulta para " + consultation.getPet().getName() + " foi finalizada.", consultation.getId());
    }

    @Transactional
    public void writeReport(Long consultationId, String report) {
        ConsultationModel consultation = findByIdOrThrow(consultationId);
        if (consultation.getStatus() != ConsultationStatus.FINALIZADA && consultation.getStatus() != ConsultationStatus.AGENDADA) {
            throw new IllegalStateException("O relatório só pode ser preenchido para consultas 'AGENDADAS' ou 'FINALIZADAS'.");
        }
        consultation.setDoctorReport(report);
        consultationRepository.save(consultation);
        notificationService.createNotification(consultation.getUsuario(), "O relatório da sua consulta para " + consultation.getPet().getName() + " está disponível.", consultation.getId());
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponseDTO> findAllForAdmin() {
        return consultationRepository.findAll().stream().map(consultationMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponseDTO> findForAuthenticatedUser(UserModel user) {
        return consultationRepository.findByUsuarioIdWithDetails(user.getId())
                .stream()
                .map(consultationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConsultationResponseDTO findById(Long id) {
        return consultationRepository.findByIdWithDetails(id)
                .map(consultationMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Consulta não encontrada com o ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponseDTO> findForAuthenticatedVeterinary(UserModel user) {
        VeterinaryModel vet = veterinaryRepository.findByUserAccount(user).orElseThrow(() -> new NoSuchElementException("Perfil de veterinário não encontrado."));
        return consultationRepository.findByVeterinarioId(vet.getId()).stream().map(consultationMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public ConsultationResponseDTO updateConsultationByAdmin(Long consultationId, ConsultationUpdateRequestDTO dto) {
        ConsultationModel consultation = findByIdOrThrow(consultationId);
        if (dto.consultationdate() != null) consultation.setConsultationdate(dto.consultationdate());
        if (dto.consultationtime() != null) consultation.setConsultationtime(dto.consultationtime());
        if (dto.reason() != null && !dto.reason().isEmpty()) consultation.setReason(dto.reason());
        if (dto.observations() != null && !dto.observations().isEmpty()) consultation.setObservations(dto.observations());
        return consultationMapper.toDTO(consultationRepository.save(consultation));
    }

    public List<ConsultationResponseDTO> findConsultationsByDate(LocalDate date) {
        return consultationRepository.findByConsultationdate(date).stream().map(consultationMapper::toDTO).collect(Collectors.toList());
    }

    public List<ConsultationResponseDTO> findConsultationsBySpeciality(SpecialityEnum speciality) {
        return consultationRepository.findBySpecialityEnum(speciality).stream().map(consultationMapper::toDTO).collect(Collectors.toList());
    }

    public List<ConsultationResponseDTO> findConsultationsByVeterinaryName(String veterinaryName) {
        return consultationRepository.findByVeterinario_NameContainingIgnoreCase(veterinaryName).stream().map(consultationMapper::toDTO).collect(Collectors.toList());
    }

    public List<ConsultationResponseDTO> findConsultationsByPetName(String petName) {
        return consultationRepository.findByPet_NameContainingIgnoreCase(petName).stream().map(consultationMapper::toDTO).collect(Collectors.toList());
    }

    public List<ConsultationResponseDTO> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return consultationRepository.findWithFilters(startDate, endDate, null, null)
                .stream()
                .map(consultationMapper::toDTO)
                .collect(Collectors.toList());
    }

    private ConsultationModel findByIdOrThrow(Long id) {
        return consultationRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Consulta não encontrada com o ID: " + id));
    }
}