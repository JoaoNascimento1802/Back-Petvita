package sesi.petvita.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sesi.petvita.clinic.model.ClinicService;
import sesi.petvita.clinic.repository.ClinicServiceRepository;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.math.BigDecimal;

@Configuration
public class ServiceInitializer {

    @Bean
    public CommandLineRunner initServices(ClinicServiceRepository serviceRepository) {
        return args -> {
            System.out.println("Verificando serviços padrão da clínica...");

            // 1. Serviço de VACINAÇÃO (Médico)
            createServiceIfNotFound(
                    serviceRepository,
                    "Vacinação",
                    "Aplicação de vacinas anuais e preventivas.",
                    new BigDecimal("80.00"),
                    SpecialityEnum.CLINICO_GERAL,
                    true // É médico
            );

            // 2. Serviço de CONSULTA GERAL (Médico)
            createServiceIfNotFound(
                    serviceRepository,
                    "Consulta Veterinária",
                    "Avaliação clínica completa do estado de saúde do animal.",
                    new BigDecimal("150.00"),
                    SpecialityEnum.CLINICO_GERAL,
                    true // É médico
            );

            // 3. Serviço de BANHO E TOSA (Estético - Não Médico)
            createServiceIfNotFound(
                    serviceRepository,
                    "Banho e Tosa",
                    "Banho completo com produtos hipoalergênicos e tosa higiênica.",
                    new BigDecimal("60.00"),
                    SpecialityEnum.ESTETICA,
                    false // Não é médico
            );

            // 4. Serviço de EXAMES (Médico)
            createServiceIfNotFound(
                    serviceRepository,
                    "Exames Laboratoriais",
                    "Coleta de sangue e exames de rotina.",
                    new BigDecimal("100.00"),
                    SpecialityEnum.PATOLOGIA,
                    true // É médico
            );

            // 5. Serviço de ANESTESIOLOGIA (Médico - Específico)
            createServiceIfNotFound(
                    serviceRepository,
                    "Avaliação Anestésica",
                    "Consulta pré-cirúrgica.",
                    new BigDecimal("200.00"),
                    SpecialityEnum.ANESTESIOLOGIA,
                    true // É médico
            );

            System.out.println("Inicialização de serviços concluída.");
        };
    }

    private void createServiceIfNotFound(
            ClinicServiceRepository repository,
            String name,
            String description,
            BigDecimal price,
            SpecialityEnum speciality,
            boolean isMedical
    ) {
        // Verifica se já existe para não duplicar
        if (repository.findByName(name).isEmpty()) {
            ClinicService service = new ClinicService();
            service.setName(name);
            service.setDescription(description);
            service.setPrice(price);
            service.setSpeciality(speciality);
            service.setMedicalService(isMedical);

            repository.save(service);
            System.out.println("Serviço criado: " + name);
        }
    }
}