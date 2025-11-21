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
import sesi.petvita.pet.model.MedicalAttachment;
import sesi.petvita.pet.model.MedicalRecord;
import sesi.petvita.pet.repository.MedicalAttachmentRepository;
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
    private final PrescriptionTemplateRepository prescriptionTemplateRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionTemplateMapper prescriptionTemplateMapper;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalAttachmentRepository medicalAttachmentRepository;
    private final String DEFAULT_IMAGE_URL = "https://i.imgur.com/2qgrCI2.png";

    // --- MÉTODO 1: UPLOAD INTELIGENTE (VIA CONSULTA) ---
    // Cria o prontuário se não existir
    @Transactional
    public String addAttachmentToConsultation(Long consultationId, MultipartFile file) throws IOException {
        ConsultationModel consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new NoSuchElementException("Consulta não encontrada: " + consultationId));

        MedicalRecord record = consultation.getMedicalRecord();

        // SE NÃO EXISTIR PRONTUÁRIO, CRIA AGORA
        if (record == null) {
            record = MedicalRecord.builder()
                    .consultation(consultation)
                    .veterinary(consultation.getVeterinario())
                    .pet(consultation.getPet()) // Essencial para evitar erro SQL 1364
                    .diagnosis("")
                    .treatment("")
                    .build();
            medicalRecordRepository.save(record);

            consultation.setMedicalRecord(record);
            consultationRepository.save(consultation);
        }

        // Prossegue com o upload no Cloudinary
        Map uploadResult = cloudinaryService.upload(file);
        String url = (String) uploadResult.get("url");
        String publicId = (String) uploadResult.get("public_id");
        String format = (String) uploadResult.getOrDefault("format", "file");

        MedicalAttachment attachment = MedicalAttachment.builder()
                .medicalRecord(record)
                .fileName(file.getOriginalFilename())
                .fileUrl(url)
                .publicId(publicId)
                .fileType(format)
                .build();

        medicalAttachmentRepository.save(attachment);
        return url;
    }

    // --- MÉTODO 2: UPLOAD DIRETO (VIA ID DO PRONTUÁRIO) ---
    // Restaurado para corrigir o erro de compilação no Controller
    @Transactional
    public String addAttachmentToMedicalRecord(Long recordId, MultipartFile file) throws IOException {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new NoSuchElementException("Prontuário não encontrado com o ID: " + recordId));

        Map uploadResult = cloudinaryService.upload(file);
        String url = (String) uploadResult.get("url");
        String publicId = (String) uploadResult.get("public_id");
        String format = (String) uploadResult.getOrDefault("format", "file");

        MedicalAttachment attachment = MedicalAttachment.builder()
                .medicalRecord(record)
                .fileName(file.getOriginalFilename())
                .fileUrl(url)
                .publicId(publicId)
                .fileType(format)
                .build();

        medicalAttachmentRepository.save(attachment);
        return url;
    }

    @Transactional
    public VeterinaryResponseDTO createVeterinary(VeterinaryRequestDTO dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new IllegalStateException("Este e-mail já está em uso por outro usuário.");
        }

        String imageUrl = DEFAULT_IMAGE_URL;

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
        initializeWorkScheduleFor(userAccount);

        return veterinaryMapper.toDTO(savedVeterinary);
    }

    private void initializeWorkScheduleFor(UserModel userAccount) {
        for (DayOfWeek day : DayOfWeek.values()) {
            WorkSchedule schedule = WorkSchedule.builder()
                    .professionalUser(userAccount)
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
        WorkSchedule schedule = workScheduleRepository.findByProfessionalUserIdAndDayOfWeek(vet.getUserAccount().getId(), dayOfWeek)
                .orElse(null);

        if (schedule == null || !schedule.isWorking() || schedule.getStartTime() == null || schedule.getEndTime() == null) {
            return new ArrayList<>();
        }

        List<LocalTime> allPossibleSlots = new ArrayList<>();
        LocalTime currentSlot = schedule.getStartTime();

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

        if(dto.name() != null) userAccount.setUsername(dto.name());
        if(dto.email() != null) userAccount.setEmail(dto.email());
        if(dto.phone() != null) userAccount.setPhone(dto.phone());
        if(dto.imageurl() != null) userAccount.setImageurl(dto.imageurl());
        if(dto.rg() != null) userAccount.setRg(dto.rg());
        if (dto.password() != null && !dto.password().isEmpty()) {
            userAccount.setPassword(passwordEncoder.encode(dto.password()));
        }

        if(dto.name() != null) vet.setName(dto.name());
        if(dto.crmv() != null) vet.setCrmv(dto.crmv());
        if(dto.specialityenum() != null) vet.setSpecialityenum(dto.specialityenum());
        if(dto.phone() != null) vet.setPhone(dto.phone());
        if(dto.imageurl() != null) vet.setImageurl(dto.imageurl());

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

        if (vet.getImagePublicId() != null) {
            try {
                cloudinaryService.delete(vet.getImagePublicId());
            } catch (IOException e) {
                System.err.println("Erro ao deletar imagem do veterinário: " + e.getMessage());
            }
        }

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
        allRatings.add(newRating);
        double totalRating = allRatings.stream().mapToDouble(VeterinaryRating::getRating).sum();
        vet.setRatingCount(allRatings.size());
        vet.setAverageRating(totalRating / allRatings.size());

        veterinaryRepository.save(vet);
    }

    @Transactional(readOnly = true)
    public List<VeterinaryResponseDTO> findAll() {
        return veterinaryRepository.findAllWithUser().stream()
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
            result = veterinaryRepository.findByNameAndSpeciality(name, speciality);
        } else if (name != null && !name.isEmpty()) {
            result = veterinaryRepository.findByName(name);
        } else if (speciality != null) {
            result = veterinaryRepository.findBySpecialityenum(speciality);
        } else {
            result = veterinaryRepository.findAllWithUser();
        }
        return result.stream().map(veterinaryMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VeterinarianMonthlyReportDTO getMonthlyReport(UserModel user) {
        VeterinaryModel vet = veterinaryRepository.findByUserAccount(user)
                .orElseThrow(() -> new IllegalStateException("Perfil de veterinário não encontrado."));

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
        VeterinaryModel vet = veterinaryRepository.findByUserAccount(user).orElseThrow();
        List<ConsultationModel> filteredConsultations = consultationRepository.findWithFilters(startDate, endDate, vet.getId(), null);

        long total = filteredConsultations.size();
        Map<String, Long> byStatus = filteredConsultations.stream().collect(Collectors.groupingBy(c -> c.getStatus().getDescricao(), Collectors.counting()));
        Map<String, Long> bySpeciality = filteredConsultations.stream().collect(Collectors.groupingBy(c -> c.getSpecialityEnum().getDescricao(), Collectors.counting()));

        List<ConsultationModel> finalized = filteredConsultations.stream().filter(c -> c.getStatus() == ConsultationStatus.FINALIZADA && c.getClinicService() != null).collect(Collectors.toList());

        BigDecimal revenue = finalized.stream().map(c -> c.getClinicService().getPrice()).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, BigDecimal> revByService = finalized.stream().collect(Collectors.groupingBy(c -> c.getClinicService().getName(), Collectors.reducing(BigDecimal.ZERO, c -> c.getClinicService().getPrice(), BigDecimal::add)));

        return new ReportSummaryDTO(total, byStatus, bySpeciality, revenue, revByService);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionTemplateResponseDTO> findMyTemplates(UserModel user) {
        return prescriptionTemplateRepository.findByVeterinaryUserId(user.getId()).stream().map(prescriptionTemplateMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public PrescriptionTemplateResponseDTO createTemplate(PrescriptionTemplateRequestDTO dto, UserModel user) {
        PrescriptionTemplate template = prescriptionTemplateMapper.toModel(dto, user);
        return prescriptionTemplateMapper.toDTO(prescriptionTemplateRepository.save(template));
    }

    @Transactional
    public void createPrescription(Long consultationId, PrescriptionRequestDTO dto) {
        ConsultationModel consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new NoSuchElementException("Consulta não encontrada: " + consultationId));

        MedicalRecord medicalRecord = consultation.getMedicalRecord();

        // CRIA PRONTUÁRIO SE NÃO EXISTIR
        if (medicalRecord == null) {
            medicalRecord = MedicalRecord.builder()
                    .consultation(consultation)
                    .veterinary(consultation.getVeterinario())
                    .pet(consultation.getPet()) // Garante o pet_id
                    .diagnosis("Prescrição avulsa")
                    .treatment("")
                    .build();
            medicalRecordRepository.save(medicalRecord);
            consultation.setMedicalRecord(medicalRecord);
            consultationRepository.save(consultation);
        }

        Prescription prescription = Prescription.builder()
                .consultation(consultation)
                .medicalRecord(medicalRecord)
                .medicationName(dto.medicationName())
                .dosage(dto.dosage())
                .frequency(dto.frequency())
                .duration(dto.duration())
                .build();

        prescriptionRepository.save(prescription);
    }

    @Transactional(readOnly = true)
    public byte[] generatePrescriptionPdf(Long prescriptionId) throws DocumentException, IOException {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new NoSuchElementException("Prescrição não encontrada: " + prescriptionId));

        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLUE);
        Paragraph title = new Paragraph("PetVita - Prescrição Médica", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.addCell(createCell("Paciente:", Element.ALIGN_LEFT, FontFactory.HELVETICA_BOLD));
        infoTable.addCell(createCell(prescription.getConsultation().getPet().getName(), Element.ALIGN_LEFT, FontFactory.HELVETICA));
        infoTable.addCell(createCell("Tutor:", Element.ALIGN_LEFT, FontFactory.HELVETICA_BOLD));
        infoTable.addCell(createCell(prescription.getConsultation().getPet().getUsuario().getUsername(), Element.ALIGN_LEFT, FontFactory.HELVETICA));
        infoTable.addCell(createCell("Veterinário:", Element.ALIGN_LEFT, FontFactory.HELVETICA_BOLD));
        infoTable.addCell(createCell(prescription.getConsultation().getVeterinario().getName(), Element.ALIGN_LEFT, FontFactory.HELVETICA));
        infoTable.addCell(createCell("Data:", Element.ALIGN_LEFT, FontFactory.HELVETICA_BOLD));
        infoTable.addCell(createCell(prescription.getCreatedAt().toLocalDate().toString(), Element.ALIGN_LEFT, FontFactory.HELVETICA));
        document.add(infoTable);
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph("Medicação:", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        document.add(new Paragraph(prescription.getMedicationName()));
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("Dosagem:", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        document.add(new Paragraph(prescription.getDosage()));
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("Frequência:", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        document.add(new Paragraph(prescription.getFrequency()));
        document.add(Chunk.NEWLINE);

        if (prescription.getDuration() != null && !prescription.getDuration().isEmpty()) {
            document.add(new Paragraph("Duração:", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            document.add(new Paragraph(prescription.getDuration()));
            document.add(Chunk.NEWLINE);
        }

        document.close();
        return baos.toByteArray();
    }

    private PdfPCell createCell(String content, int alignment, String fontName) {
        Font font = FontFactory.getFont(fontName);
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    // Lista anexos pelo ID do Prontuário
    @Transactional(readOnly = true)
    public List<MedicalAttachmentResponseDTO> getAttachmentsByRecordId(Long recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new NoSuchElementException("Prontuário não encontrado"));

        if (record.getAttachments() == null) {
            return new ArrayList<>();
        }

        return record.getAttachments().stream()
                .map(att -> new MedicalAttachmentResponseDTO(
                        att.getId(),
                        recordId,
                        att.getFileName(),
                        att.getFileUrl(),
                        null
                ))
                .collect(Collectors.toList());
    }
}