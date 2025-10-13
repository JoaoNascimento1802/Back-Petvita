package sesi.petvita.admin.dto;

import java.util.Map;

public record MonthlyReportDTO(
        int year,
        int month,
        long totalConsultations,
        Map<String, Long> consultationsByStatus,
        Map<String, Long> consultationsBySpeciality
) {}