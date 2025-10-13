package sesi.petvita.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import sesi.petvita.user.dto.UserRequestDTO;
import sesi.petvita.user.dto.UserResponseDTO;
import sesi.petvita.user.mapper.UserMapper;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.repository.UserRepository;
import sesi.petvita.user.role.UserRole;

// Imports corretos para JUnit 5 e Mockito
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UserRequestDTO userRequestDTO;
    private UserModel userModel;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        userRequestDTO = new UserRequestDTO(
                "John Doe", "StrongPassword@123", "john.doe@example.com",
                "11987654321", "123 Main St", "123456789", "http://image.url/pic.jpg"
        );

        userModel = new UserModel();
        userModel.setId(1L);
        userModel.setUsername("John Doe");
        userModel.setEmail("john.doe@example.com");
        userModel.setRole(UserRole.USER);

        userResponseDTO = new UserResponseDTO(
                1L, "John Doe", "john.doe@example.com", "11987654321",
                "123 Main St", "http://image.url/pic.jpg", UserRole.USER
        );
    }

    @Test
    void registerUser_shouldSucceedAndReturnUserResponseDTO() {
        // Arrange (Organizar)
        when(userMapper.toModel(any(UserRequestDTO.class))).thenReturn(userModel);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserModel.class))).thenReturn(userModel);
        when(userMapper.toDTO(any(UserModel.class))).thenReturn(userResponseDTO);

        // Act (Agir)
        UserResponseDTO result = userService.registerUser(userRequestDTO);

        // Assert (Verificar)
        assertNotNull(result);
        assertEquals("John Doe", result.username());
        assertEquals("john.doe@example.com", result.email());

        // Verifica se os métodos mockados foram chamados como esperado
        verify(passwordEncoder, times(1)).encode("StrongPassword@123");
        verify(userRepository, times(1)).save(any(UserModel.class));
    }
}