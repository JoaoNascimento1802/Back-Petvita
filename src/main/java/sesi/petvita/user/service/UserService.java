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
import sesi.petvita.auth.TokenService;
import sesi.petvita.config.CloudinaryService;
import sesi.petvita.notification.service.EmailService;
import sesi.petvita.pet.dto.PetResponseDTO;
import sesi.petvita.pet.mapper.PetMapper;
import sesi.petvita.user.dto.*;
import sesi.petvita.user.mapper.UserMapper;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.repository.UserRepository;
import sesi.petvita.user.role.UserRole;
import sesi.petvita.veterinary.model.VeterinaryModel;
import sesi.petvita.veterinary.model.WorkSchedule;
import sesi.petvita.veterinary.repository.VeterinaryRepository;
import sesi.petvita.veterinary.repository.WorkScheduleRepository;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final EmailService emailService; // Injeção do EmailService
    private final WorkScheduleRepository workScheduleRepository;
    private final TokenService tokenService;
    private final VeterinaryRepository veterinaryRepository;

    private final String DEFAULT_IMAGE_URL = "https://i.imgur.com/2qgrCI2.png";

    private void initializeWorkScheduleFor(UserModel userAccount) {
        for (DayOfWeek day : DayOfWeek.values()) {
            WorkSchedule schedule = WorkSchedule.builder()
                    .professionalUser(userAccount)
                    .dayOfWeek(day)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(18, 0))
                    .isWorking(day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY)
                    .build();
            workScheduleRepository.save(schedule);
        }
    }

    @Transactional
    public UserAuthResponseDTO updateUserProfile(UserModel authenticatedUser, UserProfileUpdateDTO dto) {
        String newEmail = dto.email();
        String oldEmail = authenticatedUser.getEmail();
        boolean emailChanged = newEmail != null && !newEmail.isBlank() && !newEmail.equalsIgnoreCase(oldEmail);
        String token = null;

        if (dto.username() != null && !dto.username().isBlank()) {
            authenticatedUser.setUsername(dto.username());
            if (authenticatedUser.getRole() == UserRole.VETERINARY) {
                veterinaryRepository.findByUserAccount(authenticatedUser)
                        .ifPresent(vet -> {
                            vet.setName(dto.username());
                            veterinaryRepository.save(vet);
                        });
            }
        }
        if (emailChanged) authenticatedUser.setEmail(newEmail);
        if (dto.phone() != null && !dto.phone().isBlank()) authenticatedUser.setPhone(dto.phone());
        if (dto.address() != null && !dto.address().isBlank()) authenticatedUser.setAddress(dto.address());

        UserModel savedUser = userRepository.save(authenticatedUser);
        if (emailChanged) {
            token = tokenService.generateToken(savedUser);
        }

        return new UserAuthResponseDTO(userMapper.toDTO(savedUser), token);
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

    @Transactional
    public UserResponseDTO registerUser(UserRequestDTO requestDTO) {
        UserModel user = userMapper.toModel(requestDTO);
        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
        } else if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.EMPLOYEE) {
            user.setRole(UserRole.USER);
        }

        user.setPassword(passwordEncoder.encode(requestDTO.password()));
        user.setImageurl(DEFAULT_IMAGE_URL);

        UserModel savedUser = userRepository.save(user);
        if (savedUser.getRole() == UserRole.VETERINARY) {
            createVeterinaryProfile(savedUser, requestDTO.crmv(), SpecialityEnum.CLINICO_GERAL);
        }

        // --- ENVIO DE E-MAIL DE BOAS-VINDAS ---
        try {
            Map<String, Object> emailModel = new HashMap<>();
            emailModel.put("titulo", "Bem-vindo(a) à Pet Vita!");
            emailModel.put("nomeUsuario", savedUser.getActualUsername());
            emailModel.put("corpoMensagem", "Estamos muito felizes em ter você conosco! Agora você pode agendar consultas, gerenciar os cuidados dos seus pets e muito mais.");
            emailModel.put("mostrarDetalhesConsulta", false); // Não é uma consulta

            emailService.sendHtmlEmailFromTemplate(savedUser.getEmail(), "Bem-vindo(a) ao Pet Vita!", emailModel);
        } catch (Exception e) {
            System.err.println("Falha ao enviar e-mail de boas-vindas: " + e.getMessage());
        }
        // ---------------------------------------

        return userMapper.toDTO(savedUser);
    }

    @Transactional
    public UserResponseDTO createUserByAdmin(AdminUserCreateRequestDTO dto) {
        UserModel user = new UserModel();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setPhone(dto.phone());
        user.setAddress(dto.address());
        user.setRg(dto.rg());
        user.setRole(dto.role());
        user.setImageurl(dto.imageurl() != null && !dto.imageurl().isEmpty() ? dto.imageurl() : DEFAULT_IMAGE_URL);

        UserModel savedUser = userRepository.save(user);
        
        if (dto.role() == UserRole.VETERINARY) {
            // Passa a especialidade escolhida ou usa CLINICO_GERAL como fallback
            SpecialityEnum spec = dto.speciality() != null ? dto.speciality() : SpecialityEnum.CLINICO_GERAL;
            createVeterinaryProfile(savedUser, dto.crmv(), spec);
        } else if (dto.role() == UserRole.EMPLOYEE) {
            initializeWorkScheduleFor(savedUser);
        }

        // Opcional: Enviar email com a senha provisória para o usuário criado pelo admin
        try {
            Map<String, Object> emailModel = new HashMap<>();
            emailModel.put("titulo", "Sua conta Pet Vita foi criada");
            emailModel.put("nomeUsuario", savedUser.getActualUsername());
            emailModel.put("corpoMensagem", "Uma conta foi criada para você na Pet Vita.\n\nSua senha provisória é: " + dto.password() + "\n\nRecomendamos que você altere sua senha ao fazer login.");
            emailModel.put("mostrarDetalhesConsulta", false);

            emailService.sendHtmlEmailFromTemplate(savedUser.getEmail(), "Bem-vindo(a) à equipe Pet Vita", emailModel);
        } catch (Exception e) {
            System.err.println("Falha ao enviar e-mail de criação de conta: " + e.getMessage());
        }

        return userMapper.toDTO(savedUser);
    }

    private void createVeterinaryProfile(UserModel savedUser, String crmv, SpecialityEnum speciality) {
        String finalCrmv = (crmv != null && !crmv.trim().isEmpty()) ? crmv : "PENDENTE";

        VeterinaryModel vetProfile = VeterinaryModel.builder()
                .userAccount(savedUser)
                .name(savedUser.getActualUsername())
                .phone(savedUser.getPhone())
                .imageurl(savedUser.getImageurl())
                .specialityenum(speciality) // Usa o valor passado
                .crmv(finalCrmv)
                .build();

        veterinaryRepository.save(vetProfile);
        initializeWorkScheduleFor(savedUser);
    }

    @Transactional
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

    @Transactional
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

    @Transactional(readOnly = true)
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

        String resetUrl = "https://frontvita.vercel.app/reset-password?token=" + token;
        Map<String, Object> emailModel = new HashMap<>();
        emailModel.put("titulo", "Redefinição de Senha");
        emailModel.put("nomeUsuario", user.getActualUsername());
        emailModel.put("corpoMensagem", "Você solicitou a redefinição da sua senha. Clique no link a seguir para criar uma nova senha. O link é válido por 1 hora.\n\nLink: " + resetUrl);
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

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findUsersByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }
}
