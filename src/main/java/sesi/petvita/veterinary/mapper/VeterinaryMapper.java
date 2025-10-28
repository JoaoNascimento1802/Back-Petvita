package sesi.petvita.veterinary.mapper;

import org.springframework.stereotype.Component;
import sesi.petvita.veterinary.dto.VeterinaryResponseDTO;
import sesi.petvita.veterinary.model.VeterinaryModel;

@Component
public class VeterinaryMapper {

    public VeterinaryResponseDTO toDTO(VeterinaryModel model) {
        // Acessa a conta de usuário associada para buscar os dados necessários
        String email = (model.getUserAccount() != null) ? model.getUserAccount().getEmail() : null;
        String rg = (model.getUserAccount() != null) ? model.getUserAccount().getRg() : null; // CORREÇÃO APLICADA AQUI

        return new VeterinaryResponseDTO(
                model.getId(),
                model.getName(),
                email,
                model.getCrmv(),
                rg, // O campo RG agora é mapeado corretamente
                model.getSpecialityenum(),
                model.getPhone(),
                model.getImageurl(),
                model.getAverageRating(),
                model.getRatingCount()
        );
    }
}