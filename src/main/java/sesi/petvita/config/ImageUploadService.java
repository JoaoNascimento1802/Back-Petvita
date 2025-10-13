package sesi.petvita.config;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sesi.petvita.pet.model.PetModel;
import sesi.petvita.pet.repository.PetRepository;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.repository.UserRepository;
import sesi.petvita.veterinary.model.VeterinaryModel;
import sesi.petvita.veterinary.repository.VeterinaryRepository;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final VeterinaryRepository veterinaryRepository;

    @Transactional
    public String uploadForUser(Long userId, MultipartFile file) throws IOException {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));

        // Deleta a imagem antiga, se existir
        if (user.getImagePublicId() != null && !user.getImagePublicId().isEmpty()) {
            try {
                cloudinaryService.delete(user.getImagePublicId());
            } catch (IOException e) {
                // Logar o erro, mas não falhar todo o upload por causa da exclusão anterior
                System.err.println("Falha ao deletar imagem antiga do usuário: " + e.getMessage());
            }
        }

        Map uploadResult = cloudinaryService.upload(file);
        String url = (String) uploadResult.get("url");
        String publicId = (String) uploadResult.get("public_id");

        user.setImageurl(url);
        user.setImagePublicId(publicId);
        userRepository.save(user);

        return url;
    }

    @Transactional
    public String uploadForPet(Long petId, MultipartFile file) throws IOException {
        PetModel pet = petRepository.findById(petId)
                .orElseThrow(() -> new NoSuchElementException("Pet não encontrado"));

        if (pet.getImagePublicId() != null && !pet.getImagePublicId().isEmpty()) {
            try {
                cloudinaryService.delete(pet.getImagePublicId());
            } catch (IOException e) {
                System.err.println("Erro ao deletar imagem anterior do pet: " + e.getMessage());
            }
        }

        Map uploadResult = cloudinaryService.upload(file);
        String url = (String) uploadResult.get("url");
        String publicId = (String) uploadResult.get("public_id");

        pet.setImageurl(url);
        pet.setImagePublicId(publicId);
        petRepository.save(pet);

        return url;
    }

    @Transactional
    public String uploadForVeterinary(Long vetId, MultipartFile file) throws IOException {
        VeterinaryModel vet = veterinaryRepository.findById(vetId)
                .orElseThrow(() -> new NoSuchElementException("Veterinário não encontrado"));

        // Se existir imagem antiga, tenta deletar (captura exceção para não interromper o fluxo)
        if (vet.getImagePublicId() != null && !vet.getImagePublicId().isEmpty()) {
            try {
                cloudinaryService.delete(vet.getImagePublicId());
            } catch (IOException e) {
                System.err.println("Erro ao deletar imagem anterior do veterinário: " + e.getMessage());
            }
        }

        Map uploadResult = cloudinaryService.upload(file);
        String url = (String) uploadResult.get("url");
        String publicId = (String) uploadResult.get("public_id");

        vet.setImageurl(url);
        vet.setImagePublicId(publicId);
        veterinaryRepository.save(vet);

        return url;
    }
}
