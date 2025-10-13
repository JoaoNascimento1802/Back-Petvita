package sesi.petvita.pet.mapper;


import org.springframework.stereotype.Component;
import sesi.petvita.pet.dto.PetRequestDTO;
import sesi.petvita.pet.dto.PetResponseDTO;
import sesi.petvita.pet.model.PetModel;

@Component
public class PetMapper {



    public PetModel toModel(PetRequestDTO dto) {
        return PetModel.builder()
                .name(dto.name())
                .imageurl(dto.imageurl())
                .age(dto.age())
                .personalizatedSpecies(dto.personalizatedSpecies())
                .personalizedBreed(dto.personalizedBreed())
                .speciespet(dto.speciespet())
                .porte(dto.porte())
                .gender(dto.gender())
                .birdBreed(dto.birdBreed())
                .catBreed(dto.catBreed())
                .dogBreed(dto.dogBreed())
                .fishBreed(dto.fishBreed())
                .rabbitBreed(dto.rabbitBreed())
                .reptileBreed(dto.reptileBreed())
                .rodentBreed(dto.rodentBreed())
                .build();
    }

    public PetResponseDTO toDTO(PetModel model) {
        String ownerUsername = (model.getUsuario() != null) ? model.getUsuario().getUsername() : "N/A";
        Long ownerId = (model.getUsuario() != null) ? model.getUsuario().getId() : null;

        return new PetResponseDTO(
                model.getId(),
                model.getName(),
                model.getAge(),
                model.getImageurl(),
                model.getPersonalizatedSpecies(),
                model.getPersonalizedBreed(),
                model.getSpeciespet(),
                model.getPorte(),
                model.getGender(),
                model.getBirdBreed(),
                model.getCatBreed(),
                model.getDogBreed(),
                model.getFishBreed(),
                model.getRabbitBreed(),
                model.getReptileBreed(),
                model.getRodentBreed(),
                ownerId,
                ownerUsername
        );
    }
}