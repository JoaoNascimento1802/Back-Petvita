package sesi.petvita.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importante!
import sesi.petvita.admin.dto.ReportSummaryDTO;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.consultation.repository.ConsultationRepository;
import sesi.petvita.consultation.status.ConsultationStatus;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ConsultationRepository consultationRepository;

    // A anotação @Transactional mantém a sessão do Hibernate aberta
    // permitindo o acesso a propriedades Lazy (como ClinicService dentro de ConsultationModel)
    @Transactional(readOnly = true)
    public ReportSummaryDTO getSummaryByDateRange(
            LocalDate startDate, LocalDate endDate, Optional<Long> vetId, Optional<SpecialityEnum> speciality) {

        List<ConsultationModel> filteredConsultations = consultationRepository.findWithFilters(
                startDate, endDate, vetId.orElse(null), speciality.orElse(null)
        );

        long total = filteredConsultations.size();

        Map<String, Long> byStatus = filteredConsultations.stream()
                .collect(Collectors.groupingBy(c -> c.getStatus().name(), Collectors.counting()));

        Map<String, Long> bySpeciality = filteredConsultations.stream()
                .collect(Collectors.groupingBy(c -> formatEnumString(c.getSpecialityEnum().name()), Collectors.counting()));

        // Filtra apenas finalizadas para calcular receita
        List<ConsultationModel> finalizedConsultations = filteredConsultations.stream()
                .filter(c -> c.getStatus() == ConsultationStatus.FINALIZADA && c.getClinicService() != null)
                .collect(Collectors.toList());

        // AQUI OCORRIA O ERRO: Acesso a c.getClinicService().getPrice()
        // Com @Transactional, o proxy do ClinicService pode ser inicializado.
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

    private String formatEnumString(String enumName) {
        if (enumName == null) return "";
        String lower = enumName.toLowerCase().replace("_", " ");
        return lower.substring(0, 1).toUpperCase() + lower.substring(1);
    }
}