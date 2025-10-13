package sesi.petvita.pet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sesi.petvita.pet.dto.PetRequestDTO;
import sesi.petvita.pet.dto.PetResponseDTO;
import sesi.petvita.pet.mapper.PetMapper;
import sesi.petvita.pet.model.PetModel;
import sesi.petvita.pet.repository.PetRepository;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final PetMapper petMapper;

    public List<PetResponseDTO> findAllPets() {
        return petRepository.findAll().stream()
                .map(petMapper::toDTO)
                .collect(Collectors.toList());
    }

    public PetResponseDTO findPetById(Long id) {
        return petRepository.findById(id)
                .map(petMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Pet não encontrado com o ID: " + id));
    }

    public PetResponseDTO createPet(PetRequestDTO petDto) {
        UserModel owner = userRepository.findById(petDto.usuarioId())
                .orElseThrow(() -> new NoSuchElementException("Usuário dono do pet não encontrado com o ID: " + petDto.usuarioId()));

        PetModel pet = new PetModel();
        pet.setName(petDto.name());
        pet.setAge(petDto.age());
        pet.setImageurl(petDto.imageurl());
        pet.setPersonalizatedSpecies(petDto.personalizatedSpecies());
        pet.setPersonalizedBreed(petDto.personalizedBreed());
        pet.setSpeciespet(petDto.speciespet());
        pet.setPorte(petDto.porte());
        pet.setGender(petDto.gender());
        pet.setBirdBreed(petDto.birdBreed());
        pet.setCatBreed(petDto.catBreed());
        pet.setDogBreed(petDto.dogBreed());
        pet.setFishBreed(petDto.fishBreed());
        pet.setRabbitBreed(petDto.rabbitBreed());
        pet.setReptileBreed(petDto.reptileBreed());
        pet.setRodentBreed(petDto.rodentBreed());
        pet.setUsuario(owner);

        // ===== LINHA DE DEBUG ADICIONADA AQUI =====
        System.out.println(">>> [SERVICE] Objeto PetModel prestes a salvar: " + pet.toString());
        // ==========================================

        PetModel savedPet = petRepository.save(pet);
        return petMapper.toDTO(savedPet);
    }

    public PetResponseDTO updatePet(Long id, PetRequestDTO petDto) {
        PetModel existingPet = petRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pet não encontrado com o ID: " + id));

        UserModel owner = userRepository.findById(petDto.usuarioId())
                .orElseThrow(() -> new NoSuchElementException("Usuário dono do pet não encontrado com o ID: " + petDto.usuarioId()));

        existingPet.setName(petDto.name());
        existingPet.setAge(petDto.age());
        existingPet.setImageurl(petDto.imageurl());
        existingPet.setPersonalizatedSpecies(petDto.personalizatedSpecies());
        existingPet.setPersonalizedBreed(petDto.personalizedBreed());
        existingPet.setSpeciespet(petDto.speciespet());
        existingPet.setPorte(petDto.porte());
        existingPet.setGender(petDto.gender());
        existingPet.setBirdBreed(petDto.birdBreed());
        existingPet.setCatBreed(petDto.catBreed());
        existingPet.setDogBreed(petDto.dogBreed());
        existingPet.setFishBreed(petDto.fishBreed());
        existingPet.setRabbitBreed(petDto.rabbitBreed());
        existingPet.setReptileBreed(petDto.reptileBreed());
        existingPet.setRodentBreed(petDto.rodentBreed());
        existingPet.setUsuario(owner);

        PetModel updatedPet = petRepository.save(existingPet);
        return petMapper.toDTO(updatedPet);
    }

    public void deletePet(Long id) {
        if (!petRepository.existsById(id)) {
            throw new NoSuchElementException("Pet não encontrado com o ID: " + id);
        }
        petRepository.deleteById(id);
    }

    public List<PetResponseDTO> findPetsByUser(UserModel user) {
        return petRepository.findByUsuario(user)
                .stream()
                .map(petMapper::toDTO)
                .collect(Collectors.toList());
    }
}