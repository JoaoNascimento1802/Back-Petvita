package sesi.petvita.veterinary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.consultation.repository.ConsultationRepository;
import sesi.petvita.consultation.status.ConsultationStatus;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.repository.UserRepository;
import sesi.petvita.user.role.UserRole;
import sesi.petvita.veterinary.dto.*;
import sesi.petvita.veterinary.mapper.VeterinaryMapper;
import sesi.petvita.veterinary.model.VeterinaryModel;
import sesi.petvita.veterinary.model.VeterinaryRating;
import sesi.petvita.veterinary.model.WorkSchedule;
import sesi.petvita.veterinary.repository.VeterinaryRatingRepository;
import sesi.petvita.veterinary.repository.VeterinaryRepository;
import sesi.petvita.veterinary.repository.WorkScheduleRepository;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VeterinaryService {

    private final VeterinaryRepository veterinaryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VeterinaryMapper veterinaryMapper;
    private final VeterinaryRatingRepository ratingRepository;
    private final ConsultationRepository consultationRepository;
    private final WorkScheduleRepository workScheduleRepository;

    @Transactional
    public VeterinaryResponseDTO createVeterinary(VeterinaryRequestDTO dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new IllegalStateException("Este e-mail já está em uso por outro usuário.");
        }

        UserModel userAccount = UserModel.builder()
                .username(dto.name())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .phone(dto.phone())
                .role(UserRole.VETERINARY)
                .address("Não informado")
                .rg(dto.rg())
                .imageurl(dto.imageurl())
                .build();

        VeterinaryModel newVeterinary = VeterinaryModel.builder()
                .name(dto.name())
                .crmv(dto.crmv())
                .specialityenum(dto.specialityenum())
                .phone(dto.phone())
                .imageurl(dto.imageurl())
                .userAccount(userAccount)
                .build();

        VeterinaryModel savedVeterinary = veterinaryRepository.save(newVeterinary);
        initializeWorkScheduleFor(savedVeterinary);

        return veterinaryMapper.toDTO(savedVeterinary);
    }

    private void initializeWorkScheduleFor(VeterinaryModel vet) {
        for (DayOfWeek day : DayOfWeek.values()) {
            WorkSchedule schedule = WorkSchedule.builder()
                    .veterinary(vet)
                    .dayOfWeek(day)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(18, 0))
                    .isWorking(day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY)
                    .build();
            workScheduleRepository.save(schedule);
        }
    }

    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlots(Long vetId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        WorkSchedule schedule = workScheduleRepository.findByVeterinaryIdAndDayOfWeek(vetId, dayOfWeek)
                .orElse(null);

        if (schedule == null || !schedule.isWorking() || schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return new ArrayList<>();
        }

        List<LocalTime> allPossibleSlots = new ArrayList<>();
        LocalTime currentSlot = schedule.getStartTime();
        while (currentSlot.isBefore(schedule.getEndTime())) {
            allPossibleSlots.add(currentSlot);
            currentSlot = currentSlot.plusHours(1);
        }

        List<LocalTime> bookedSlots = consultationRepository.findBookedTimesByVeterinarianAndDate(vetId, date);

        return allPossibleSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());
    }

    @Transactional
    public VeterinaryResponseDTO updateVeterinary(Long id, VeterinaryRequestDTO dto) {
        VeterinaryModel vet = veterinaryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Veterinário não encontrado com o ID: " + id));

        UserModel userAccount = vet.getUserAccount();
        if (userAccount == null) {
            throw new IllegalStateException("Perfil de veterinário sem conta de usuário associada.");
        }

        userAccount.setUsername(dto.name());
        userAccount.setEmail(dto.email());
        userAccount.setPhone(dto.phone());
        userAccount.setImageurl(dto.imageurl());
        userAccount.setRg(dto.rg());

        if (dto.password() != null && !dto.password().isEmpty()) {
            userAccount.setPassword(passwordEncoder.encode(dto.password()));
        }

        vet.setName(dto.name());
        vet.setCrmv(dto.crmv());
        vet.setSpecialityenum(dto.specialityenum());
        vet.setPhone(dto.phone());
        vet.setImageurl(dto.imageurl());

        VeterinaryModel updatedVet = veterinaryRepository.save(vet);

        return veterinaryMapper.toDTO(updatedVet);
    }

    @Transactional
    public void deleteVeterinary(Long id) {
        if (!veterinaryRepository.existsById(id)) {
            throw new NoSuchElementException("Veterinário não encontrado com o ID: " + id);
        }

        List<ConsultationStatus> activeStatuses = List.of(ConsultationStatus.PENDENTE, ConsultationStatus.AGENDADA);
        if (consultationRepository.existsByVeterinarioIdAndStatusIn(id, activeStatuses)) {
            throw new IllegalStateException("Não é possível excluir este veterinário, pois ele possui consultas pendentes ou agendadas.");
        }

        VeterinaryModel vet = veterinaryRepository.findById(id).get();

        // A exclusão do UserModel é feita em cascata (orphanRemoval=true)
        veterinaryRepository.delete(vet);
    }

    @Transactional
    public void addRating(Long veterinaryId, Long userId, VeterinaryRatingRequestDTO dto) {
        VeterinaryModel vet = veterinaryRepository.findById(veterinaryId)
                .orElseThrow(() -> new NoSuchElementException("Veterinário não encontrado."));
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado."));

        VeterinaryRating newRating = VeterinaryRating.builder()
                .veterinary(vet)
                .user(user)
                .rating(dto.rating())
                .comment(dto.comment())
                .build();
        ratingRepository.save(newRating);

        List<VeterinaryRating> allRatings = vet.getRatings();
        allRatings.add(newRating); // Adiciona a nova avaliação para o cálculo
        double totalRating = allRatings.stream().mapToDouble(VeterinaryRating::getRating).sum();
        vet.setRatingCount(allRatings.size());
        vet.setAverageRating(totalRating / allRatings.size());

        veterinaryRepository.save(vet);
    }

    public List<VeterinaryResponseDTO> findAll() {
        return veterinaryRepository.findAll().stream()
                .map(veterinaryMapper::toDTO)
                .collect(Collectors.toList());
    }

    public VeterinaryResponseDTO findById(Long id) {
        return veterinaryRepository.findById(id)
                .map(veterinaryMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Veterinário não encontrado com o ID: " + id));
    }

    public VeterinaryResponseDTO findVeterinaryByUserAccount(UserModel user) {
        return veterinaryRepository.findByUserAccount(user)
                .map(veterinaryMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Perfil de veterinário não encontrado para este usuário."));
    }

    public List<VeterinaryResponseDTO> searchVeterinarians(String name, SpecialityEnum speciality) {
        List<VeterinaryModel> result;
        if (name != null && !name.isEmpty() && speciality != null) {
            result = veterinaryRepository.findByNameContainingIgnoreCaseAndSpecialityenum(name, speciality);
        } else if (name != null && !name.isEmpty()) {
            result = veterinaryRepository.findByNameContainingIgnoreCase(name);
        } else if (speciality != null) {
            result = veterinaryRepository.findBySpecialityenum(speciality);
        } else {
            result = veterinaryRepository.findAll();
        }
        return result.stream().map(veterinaryMapper::toDTO).collect(Collectors.toList());
    }

    public VeterinarianMonthlyReportDTO getMonthlyReport(UserModel user) {
        VeterinaryModel vet = veterinaryRepository.findByUserAccount(user)
                .orElseThrow(() -> new IllegalStateException("Perfil de veterinário não encontrado para este usuário."));

        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<ConsultationModel> monthlyConsultations = consultationRepository.findByVeterinarioAndConsultationdateBetween(vet, startDate, endDate);

        long total = monthlyConsultations.size();
        long finalized = monthlyConsultations.stream().filter(c -> c.getStatus() == ConsultationStatus.FINALIZADA).count();
        long pending = monthlyConsultations.stream().filter(c -> c.getStatus() == ConsultationStatus.PENDENTE).count();
        Set<String> patients = monthlyConsultations.stream().map(c -> c.getPet().getName()).collect(Collectors.toSet());

        return new VeterinarianMonthlyReportDTO(year, month, total, finalized, pending, patients);
    }
}