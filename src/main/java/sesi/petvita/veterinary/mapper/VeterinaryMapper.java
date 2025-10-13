package sesi.petvita.veterinary.mapper;

import org.springframework.stereotype.Component;
import sesi.petvita.veterinary.dto.VeterinaryResponseDTO;
import sesi.petvita.veterinary.model.VeterinaryModel;

@Component
public class VeterinaryMapper {

    // O método toModel não é mais necessário aqui, pois a lógica está no service.

    public VeterinaryResponseDTO toDTO(VeterinaryModel model) {
        // Busca o email da conta de usuário associada
        String email = (model.getUserAccount() != null) ? model.getUserAccount().getEmail() : null;

        return new VeterinaryResponseDTO(
                model.getId(),
                model.getName(),
                email, // Usa o email do userAccount
                model.getCrmv(),
                model.getSpecialityenum(),
                model.getPhone(),
                model.getImageurl(),
                model.getAverageRating(),
                model.getRatingCount()
        );
    }
}