// sesi/petvita/veterinary/mapper/VeterinaryMapper.java
package sesi.petvita.veterinary.mapper;

import org.springframework.stereotype.Component;
import sesi.petvita.veterinary.dto.VeterinaryResponseDTO;
import sesi.petvita.veterinary.model.VeterinaryModel;
import sesi.petvita.user.model.UserModel;

@Component
public class VeterinaryMapper {

    public VeterinaryResponseDTO toDTO(VeterinaryModel model) {
        if (model == null) {
            return null;
        }

        UserModel user = model.getUserAccount();

        // Nome: Tenta Vet -> Tenta User -> Fallback
        String name = model.getName();
        if ((name == null || name.isEmpty()) && user != null) {
            name = user.getActualUsername();
        }
        if (name == null) name = "Nome Indisponível";

        // Email: Tenta User -> Fallback
        String email = (user != null) ? user.getEmail() : "Email não informado";

        // RG: Tenta User -> Fallback
        String rg = (user != null) ? user.getRg() : "";

        // Phone: Tenta Vet -> Tenta User -> Fallback
        String phone = model.getPhone();
        if ((phone == null || phone.isEmpty()) && user != null) {
            phone = user.getPhone();
        }
        if (phone == null) phone = "";

        // Image: Tenta Vet -> Tenta User -> Padrão
        String imageUrl = model.getImageurl();
        if ((imageUrl == null || imageUrl.isEmpty()) && user != null) {
            imageUrl = user.getImageurl();
        }
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = "https://i.imgur.com/2qgrCI2.png";
        }

        Double rating = model.getAverageRating() != null ? model.getAverageRating() : 0.0;
        Integer ratingCount = model.getRatingCount() != null ? model.getRatingCount() : 0;

        return new VeterinaryResponseDTO(
                model.getId(),
                name,
                email,
                model.getCrmv(),
                rg,
                model.getSpecialityenum(),
                phone,
                imageUrl,
                rating,
                ratingCount
        );
    }
}