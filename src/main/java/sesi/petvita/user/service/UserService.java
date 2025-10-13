package sesi.petvita.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.admin.dto.AdminUserCreateRequestDTO;
import sesi.petvita.admin.dto.UserDetailsWithPetsDTO;
import sesi.petvita.auth.PasswordResetToken;
import sesi.petvita.auth.PasswordResetTokenRepository;
import sesi.petvita.config.CloudinaryService;
import sesi.petvita.notification.service.EmailService;
import sesi.petvita.pet.dto.PetResponseDTO;
import sesi.petvita.pet.mapper.PetMapper;
import sesi.petvita.user.dto.UserProfileUpdateDTO;
import sesi.petvita.user.dto.UserRequestDTO;
import sesi.petvita.user.dto.UserResponseDTO;
import sesi.petvita.user.dto.UserUpdateRequestDTO;
import sesi.petvita.user.mapper.UserMapper;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.repository.UserRepository;
import sesi.petvita.user.role.UserRole;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final CloudinaryService cloudinaryService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Transactional
    public UserResponseDTO updateUserProfile(Long userId, UserProfileUpdateDTO dto) {
        UserModel existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Utilizador não encontrado com o ID: " + userId));
        if (dto.username() != null) existingUser.setUsername(dto.username());
        if (dto.email() != null) existingUser.setEmail(dto.email());
        if (dto.phone() != null) existingUser.setPhone(dto.phone());
        if (dto.address() != null) existingUser.setAddress(dto.address());
        UserModel savedUser = userRepository.save(existingUser);
        return userMapper.toDTO(savedUser);
    }

    public Page<UserResponseDTO> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDTO);
    }

    public Page<UserResponseDTO> searchByName(String name, Pageable pageable) {
        Page<UserModel> userPage = userRepository.findByUsernameContainingIgnoreCase(name, pageable);
        return userPage.map(userMapper::toDTO);
    }

    public UserResponseDTO findUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Utilizador não encontrado com o ID: " + id));
    }

    public UserResponseDTO registerUser(UserRequestDTO requestDTO) {
        UserModel user = userMapper.toModel(requestDTO);
        user.setRole(UserRole.USER);
        user.setPassword(passwordEncoder.encode(requestDTO.password()));

        UserModel savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }

    public UserResponseDTO createUserByAdmin(AdminUserCreateRequestDTO dto) {
        UserModel user = new UserModel();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setPhone(dto.phone());
        user.setAddress(dto.address());
        user.setRg(dto.rg());
        user.setImageurl(dto.imageurl() != null && !dto.imageurl().isEmpty() ? dto.imageurl() : "https://i.imgur.com/2qgrCI2.png");
        user.setRole(dto.role());

        UserModel savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }

    public UserResponseDTO updateUser(Long id, UserUpdateRequestDTO requestDTO) {
        UserModel existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Utilizador não encontrado com o ID: " + id));
        if (requestDTO.username() != null) existingUser.setUsername(requestDTO.username());
        if (requestDTO.email() != null) existingUser.setEmail(requestDTO.email());
        if (requestDTO.phone() != null) existingUser.setPhone(requestDTO.phone());
        if (requestDTO.address() != null) existingUser.setAddress(requestDTO.address());
        if (requestDTO.password() != null && !requestDTO.password().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(requestDTO.password()));
        }

        UserModel savedUser = userRepository.save(existingUser);
        return userMapper.toDTO(savedUser);
    }

    public void deleteUser(Long id) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Utilizador não encontrado com o ID: " + id));
        if (user.getImagePublicId() != null && !user.getImagePublicId().isEmpty()) {
            try {
                cloudinaryService.delete(user.getImagePublicId());
            } catch (IOException e) {
                System.err.println("Erro ao apagar imagem do utilizador no Cloudinary: " + e.getMessage());
            }
        }
        userRepository.delete(user);
    }

    public UserDetailsWithPetsDTO getUserWithPets(Long userId) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Utilizador não encontrado com o ID: " + userId));
        List<PetResponseDTO> petDTOs = user.getPets().stream()
                .map(petMapper::toDTO)
                .collect(Collectors.toList());
        return new UserDetailsWithPetsDTO(userMapper.toDTO(user), petDTOs);
    }

    @Transactional
    public void requestPasswordReset(String userEmail) {
        UserModel user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NoSuchElementException("Utilizador não encontrado com o e-mail: " + userEmail));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(resetToken);

        String resetUrl = "https://vet-clinic-api-front.vercel.app/reset-password?token=" + token;

        Map<String, Object> emailModel = new HashMap<>();
        emailModel.put("titulo", "Redefinição de Senha");
        emailModel.put("nomeUsuario", user.getActualUsername());
        emailModel.put("corpoMensagem", "Você solicitou a redefinição da sua senha. Clique no link a seguir para criar uma nova senha. O link é válido por 1 hora. Se você não solicitou isso, por favor ignore este e-mail.\n\nLink: " + resetUrl);
        emailModel.put("mostrarDetalhesConsulta", false);

        emailService.sendHtmlEmailFromTemplate(user.getEmail(), "Redefinição de Senha - Pet Vita", emailModel);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Token de redefinição inválido ou expirado."));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalStateException("Token de redefinição expirado.");
        }

        UserModel user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

    // --- NOVO MÉTODO PARA BUSCAR UTILIZADORES POR ROLE ---
    public List<UserResponseDTO> findUsersByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }
    // -----------------------------------------
}