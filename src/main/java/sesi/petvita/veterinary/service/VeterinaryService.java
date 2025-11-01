package sesi.petvita.veterinary.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sesi.petvita.admin.dto.ReportSummaryDTO;
import sesi.petvita.config.CloudinaryService;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.consultation.repository.ConsultationRepository;
import sesi.petvita.consultation.status.ConsultationStatus;
import sesi.petvita.pet.model.MedicalRecord;
import sesi.petvita.pet.repository.MedicalRecordRepository;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.repository.UserRepository;
import sesi.petvita.user.role.UserRole;
import sesi.petvita.veterinary.dto.*;
import sesi.petvita.veterinary.mapper.PrescriptionTemplateMapper;
import sesi.petvita.veterinary.mapper.VeterinaryMapper;
import sesi.petvita.veterinary.model.*;
import sesi.petvita.veterinary.repository.*;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final CloudinaryService cloudinaryService;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalAttachmentRepository medicalAttachmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionTemplateRepository prescriptionTemplateRepository;
    private final PrescriptionTemplateMapper prescriptionTemplateMapper;
    private final String DEFAULT_IMAGE_URL = "https://i.imgur.com/2qgrCI2.png";

    @Transactional
    public VeterinaryResponseDTO createVeterinary(VeterinaryRequestDTO dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new IllegalStateException("Este e-mail já está em uso por outro usuário.");
        }

        String imageUrl = (dto.imageurl() != null && !dto.imageurl().isBlank()) ? dto.imageurl() : DEFAULT_IMAGE_URL;

        UserModel userAccount = UserModel.builder()
                .username(dto.name())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .phone(dto.phone())
                .role(UserRole.VETERINARY)
                .address("Não informado")
                .rg(dto.rg())
                .imageurl(imageUrl)
                .build();

        VeterinaryModel newVeterinary = VeterinaryModel.builder()
                .name(dto.name())
                .crmv(dto.crmv())
                .specialityenum(dto.specialityenum())
                .phone(dto.phone())
                .imageurl(imageUrl)
                .userAccount(userAccount)
                .build();

        VeterinaryModel savedVeterinary = veterinaryRepository.save(newVeterinary);
        initializeWorkScheduleFor(userAccount); // Passa a conta de usuário

        return veterinaryMapper.toDTO(savedVeterinary);
    }

    private void initializeWorkScheduleFor(UserModel userAccount) {
        for (DayOfWeek day : DayOfWeek.values()) {
            WorkSchedule schedule = WorkSchedule.builder()
                    .professionalUser(userAccount) // Associa ao UserModel
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
        VeterinaryModel vet = veterinaryRepository.findById(vetId)
                .orElseThrow(() -> new NoSuchElementException("Veterinário não encontrado: " + vetId));

        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // Busca pelo ID do usuário associado ao veterinário
        WorkSchedule schedule = workScheduleRepository.findByProfessionalUserIdAndDayOfWeek(vet.getUserAccount().getId(), dayOfWeek)
                .orElse(null);

        if (schedule == null || !schedule.isWorking() || schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return new ArrayList<>();
        }

        List<LocalTime> allPossibleSlots = new ArrayList<>();
        LocalTime currentSlot = schedule.getStartTime();

        // Lógica com intervalo de 45 minutos
        while (currentSlot.isBefore(schedule.getEndTime())) {
            allPossibleSlots.add(currentSlot);
            currentSlot = currentSlot.plusMinutes(45);
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
        veterinaryRepository.delete(vet); // Graças ao cascade, o UserModel associado também será removido
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
        allRatings.add(newRating);
        double totalRating = allRatings.stream().mapToDouble(VeterinaryRating::getRating).sum();
        vet.setRatingCount(allRatings.size());
        vet.setAverageRating(totalRating / allRatings.size());

        veterinaryRepository.save(vet);
    }

    @Transactional(readOnly = true)
    public List<VeterinaryResponseDTO> findAll() {
        return veterinaryRepository.findAll().stream()
                .map(veterinaryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VeterinaryResponseDTO findById(Long id) {
        return veterinaryRepository.findById(id)
                .map(veterinaryMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Veterinário não encontrado com o ID: " + id));
    }

    @Transactional(readOnly = true)
    public VeterinaryResponseDTO findVeterinaryByUserAccount(UserModel user) {
        return veterinaryRepository.findByUserAccount(user)
                .map(veterinaryMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Perfil de veterinário não encontrado para este usuário."));
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public ReportSummaryDTO getCustomReportForVet(UserModel user, LocalDate startDate, LocalDate endDate) {
        VeterinaryModel vet = veterinaryRepository.findByUserAccount(user)
                .orElseThrow(() -> new IllegalStateException("Perfil de veterinário não encontrado para este usuário."));

        List<ConsultationModel> filteredConsultations = consultationRepository.findWithFilters(
                startDate, endDate, vet.getId(), null
        );

        long total = filteredConsultations.size();
        Map<String, Long> byStatus = filteredConsultations.stream()
                .collect(Collectors.groupingBy(c -> c.getStatus().getDescricao(), Collectors.counting()));
        Map<String, Long> bySpeciality = filteredConsultations.stream()
                .collect(Collectors.groupingBy(c -> c.getSpecialityEnum().getDescricao(), Collectors.counting()));

        List<ConsultationModel> finalizedConsultations = filteredConsultations.stream()
                .filter(c -> c.getStatus() == ConsultationStatus.FINALIZADA && c.getClinicService() != null)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = finalizedConsultations.stream()
                .map(c -> c.getClinicService().getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> revenueByService = finalizedConsultations.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getClinicService().getName(),
                        Collectors.reducing(BigDecimal.ZERO, c -> c.getClinicService().getPrice(), BigDecimal::add)
                ));

        return new ReportSummaryDTO(total, byStatus, bySpeciality, totalRevenue, revenueByService);
    }

    @Transactional
    public String addAttachmentToMedicalRecord(Long recordId, MultipartFile file) throws IOException {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new NoSuchElementException("Prontuário médico não encontrado com o ID: " + recordId));

        Map uploadResult = cloudinaryService.upload(file);
        String url = (String) uploadResult.get("url");
        String publicId = (String) uploadResult.get("public_id");

        MedicalAttachment attachment = MedicalAttachment.builder()
                .medicalRecord(medicalRecord)
                .fileName(file.getOriginalFilename())
                .fileUrl(url)
                .publicId(publicId)
                .build();
        medicalAttachmentRepository.save(attachment);

        return url;
    }

    @Transactional
    public void createPrescription(Long consultationId, PrescriptionRequestDTO dto) {
        ConsultationModel consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new NoSuchElementException("Consulta não encontrada com o ID: " + consultationId));

        Prescription prescription = Prescription.builder()
                .consultation(consultation)
                .medication(dto.medication())
                .dosage(dto.dosage())
                .instructions(dto.instructions())
                .build();
        prescriptionRepository.save(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionTemplateResponseDTO> findMyTemplates(UserModel user) {
        return prescriptionTemplateRepository.findByVeterinaryUserId(user.getId())
                .stream()
                .map(prescriptionTemplateMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PrescriptionTemplateResponseDTO createTemplate(PrescriptionTemplateRequestDTO dto, UserModel user) {
        PrescriptionTemplate template = prescriptionTemplateMapper.toModel(dto, user);
        PrescriptionTemplate savedTemplate = prescriptionTemplateRepository.save(template);
        return prescriptionTemplateMapper.toDTO(savedTemplate);
    }

    @Transactional(readOnly = true)
    public byte[] generatePrescriptionPdf(Long prescriptionId) throws DocumentException, IOException {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new NoSuchElementException("Prescrição não encontrada com o ID: " + prescriptionId));

        ConsultationModel consultation = prescription.getConsultation();
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, baos);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.DARK_GRAY);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

        Paragraph title = new Paragraph("Prescrição Veterinária", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 2});
        infoTable.setSpacingAfter(15);

        addCellToTable(infoTable, "Paciente:", headerFont, Element.ALIGN_LEFT);
        addCellToTable(infoTable, consultation.getPet().getName(), bodyFont, Element.ALIGN_LEFT);
        addCellToTable(infoTable, "Tutor:", headerFont, Element.ALIGN_LEFT);
        addCellToTable(infoTable, consultation.getUsuario().getActualUsername(), bodyFont, Element.ALIGN_LEFT);
        document.add(infoTable);

        document.add(new Paragraph("Medicação:", headerFont));
        document.add(new Paragraph(prescription.getMedication(), bodyFont));
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph("Dosagem:", headerFont));
        document.add(new Paragraph(prescription.getDosage(), bodyFont));
        document.add(Chunk.NEWLINE);

        if (prescription.getInstructions() != null && !prescription.getInstructions().isEmpty()) {
            document.add(new Paragraph("Instruções Adicionais:", headerFont));
            document.add(new Paragraph(prescription.getInstructions(), bodyFont));
            document.add(Chunk.NEWLINE);
        }

        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        Paragraph vetInfo = new Paragraph();
        vetInfo.setAlignment(Element.ALIGN_CENTER);
        vetInfo.add(new Chunk(consultation.getVeterinario().getName(), bodyFont));
        vetInfo.add(Chunk.NEWLINE);
        vetInfo.add(new Chunk("CRMV: " + consultation.getVeterinario().getCrmv(), bodyFont));
        document.add(vetInfo);

        document.close();
        return baos.toByteArray();
    }

    private void addCellToTable(PdfPTable table, String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        cell.setPaddingBottom(5);
        table.addCell(cell);
    }
}