package sesi.petvita.consultation.dto;

import sesi.petvita.consultation.status.ConsultationStatus;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ConsultationResponseDTO(
        Long id,
        LocalDate consultationdate,
        LocalTime consultationtime,
        ConsultationStatus status,
        String reason,
        String observations,
        String doctorReport,
        Long petId,
        String petName,
        Long usuarioId,
        String userName,
        Long veterinaryId,
        String veterinaryName,
        String serviceName,
        BigDecimal servicePrice,
        SpecialityEnum speciality
) {}