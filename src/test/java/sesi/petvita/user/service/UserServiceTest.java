// src/test/java/sesi/petvita/user/service/UserServiceTest.java
package sesi.petvita.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import sesi.petvita.user.dto.UserRequestDTO;
import sesi.petvita.user.dto.UserResponseDTO;
import sesi.petvita.user.mapper.UserMapper;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.repository.UserRepository;
import sesi.petvita.user.role.UserRole;
import sesi.petvita.veterinary.repository.VeterinaryRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private VeterinaryRepository veterinaryRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // RequestDTO (Entrada - mantém os campos de cadastro)
        UserRequestDTO requestDTO = new UserRequestDTO(
                "John Doe",
                "password",
                "john@example.com",
                "123456789",
                "Address",
                "12345", // RG
                "url",
                null,
                UserRole.USER
        );

        // Modelo Simulado
        UserModel userModel = new UserModel();
        userModel.setId(1L);
        userModel.setUsername("John Doe");
        userModel.setEmail("john@example.com");
        userModel.setRole(UserRole.USER);

        // ResponseDTO (Saída) - CORRIGIDO PARA 7 ARGUMENTOS
        UserResponseDTO responseDTO = new UserResponseDTO(
                1L,                 // id
                "John Doe",         // username
                "john@example.com", // email
                "123456789",        // phone
                "Address",          // address
                "url",              // imageurl
                UserRole.USER       // role
        );

        // Mocks
        when(userMapper.toModel(requestDTO)).thenReturn(userModel);
        when(passwordEncoder.encode(requestDTO.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserModel.class))).thenReturn(userModel);
        when(userMapper.toDTO(userModel)).thenReturn(responseDTO);

        // Execução
        UserResponseDTO result = userService.registerUser(requestDTO);

        // Verificação
        assertNotNull(result);
        assertEquals("John Doe", result.username());
        verify(userRepository, times(1)).save(any(UserModel.class));
    }
}