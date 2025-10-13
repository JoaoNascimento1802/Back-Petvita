package sesi.petvita.veterinary.dto;

import java.util.Set;

public record VeterinarianMonthlyReportDTO(
        int year,
        int month,
        long totalConsultations,
        long finalizedConsultations,
        long pendingConsultations,
        Set<String> patientsAttended // Usamos Set para não repetir nomes de pets
) {}