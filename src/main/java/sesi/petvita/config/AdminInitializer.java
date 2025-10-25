package sesi.petvita.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.repository.UserRepository;
import sesi.petvita.user.role.UserRole;
// As importações do VeterinaryRepository, VeterinaryModel e SpecialityEnum foram removidas.
import java.util.Optional;

@Configuration
public class AdminInitializer {

    @Bean
    public CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Criação do usuário ADMIN
            final String adminEmail = "admin@petvita.com";
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                System.out.println("Criando usuário administrador...");
                UserModel newAdmin = new UserModel();
                newAdmin.setUsername("Administrador");
                newAdmin.setEmail(adminEmail);
                newAdmin.setPassword(passwordEncoder.encode("Admin@123"));
                newAdmin.setPhone("11999999999");
                newAdmin.setAddress("Rua Admin, 123");
                newAdmin.setRg("999999999");
                newAdmin.setImageurl("https://i.imgur.com/2qgrCI2.png");
                newAdmin.setRole(UserRole.ADMIN);
                userRepository.save(newAdmin);
                System.out.println("Usuário 'admin@petvita.com' criado com sucesso!");
            }

            // Criação do usuário EMPLOYEE para testes
            final String employeeEmail = "funcionario@petvita.com";
            if (userRepository.findByEmail(employeeEmail).isEmpty()) {
                System.out.println("Criando usuário funcionário de teste...");
                UserModel newEmployee = new UserModel();
                newEmployee.setUsername("Funcionario Teste");
                newEmployee.setEmail(employeeEmail);
                newEmployee.setPassword(passwordEncoder.encode("Funcionario@123"));
                newEmployee.setPhone("11888888888");
                newEmployee.setAddress("Rua Funcionario, 456");
                newEmployee.setRg("888888888");
                newEmployee.setImageurl("https://i.imgur.com/2qgrCI2.png");
                newEmployee.setRole(UserRole.EMPLOYEE);
                userRepository.save(newEmployee);
                System.out.println("Usuário 'funcionario@petvita.com' criado com sucesso!");
            }
        };
    }
}