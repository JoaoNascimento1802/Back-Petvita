package sesi.petvita.veterinary.dto;

import sesi.petvita.veterinary.speciality.SpecialityEnum;

public record VeterinaryResponseDTO(
        Long id,
        String name,
        String email,
        String crmv,
        String rg,
        SpecialityEnum specialityenum,
        String phone,
        String imageurl,
        Double averageRating, // NOVO CAMPO
        Integer ratingCount   // NOVO CAMPO
) {}