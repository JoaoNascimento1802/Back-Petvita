package sesi.petvita.admin.dto;

import sesi.petvita.pet.dto.PetResponseDTO;
import sesi.petvita.user.dto.UserResponseDTO;
import java.util.List;

public record UserDetailsWithPetsDTO(
        UserResponseDTO user,
        List<PetResponseDTO> pets
) {}