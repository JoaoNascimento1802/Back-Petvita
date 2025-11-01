package sesi.petvita.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.repository.UserRepository;
import sesi.petvita.user.role.UserRole;
import sesi.petvita.veterinary.model.WorkSchedule; // <-- IMPORTAR
import sesi.petvita.veterinary.repository.WorkScheduleRepository; // <-- IMPORTAR

import java.time.DayOfWeek; // <-- IMPORTAR
import java.time.LocalTime; // <-- IMPORTAR
import java.util.Optional;

@Configuration
public class AdminInitializer {

    // --- NOVO MÉTODO PRIVADO ---
    // Método auxiliar para criar horários padrão
    private void initializeWorkScheduleFor(UserModel userAccount, WorkScheduleRepository workScheduleRepository) {
        System.out.println("Inicializando horários para: " + userAccount.getEmail());
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

    @Bean
    public CommandLineRunner initAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder, WorkScheduleRepository workScheduleRepository) { // <-- INJETAR REPOSITÓRIO
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
                // (Admins não precisam de horário de trabalho)
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
                UserModel savedEmployee = userRepository.save(newEmployee);
                System.out.println("Usuário 'funcionario@petvita.com' criado com sucesso!");

                // --- CORREÇÃO APLICADA AQUI ---
                // Inicializa os horários para o funcionário de teste
                initializeWorkScheduleFor(savedEmployee, workScheduleRepository);
            }
        };
    }
}