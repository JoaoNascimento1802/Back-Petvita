package sesi.petvita.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import sesi.petvita.auth.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Value("${cors.allowed.origins}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // ====================================================
                        // 1. PREFLIGHT E PÚBLICOS (Prioridade Máxima)
                        // ====================================================
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/auth/**",
                                "/users/register",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/api/public/**"
                        ).permitAll()

                        // ====================================================
                        // 2. CORREÇÃO DEFINITIVA DE AVALIAÇÃO E USUÁRIO
                        // ====================================================

                        // Libera qualquer sub-rota de /users/ para quem estiver logado (para update de perfil).
                        .requestMatchers("/users/**").authenticated()

                        // Libera a nova rota centralizada de avaliações
                        .requestMatchers("/api/ratings/**").authenticated()

                        // ====================================================
                        // 3. PERFIL PÚBLICO (VETERINÁRIO)
                        // ====================================================
                        // Deve vir ANTES das regras restritivas de /veterinary/** para permitir visualização sem login
                        .requestMatchers(HttpMethod.GET, "/veterinary/**").permitAll()


                        // ====================================================
                        // 4. ÁREAS RESTRITAS (ADMIN, VET, FUNC)
                        // ====================================================

                        // ADMINISTRAÇÃO
                        .requestMatchers(HttpMethod.DELETE, "/admin/consultations/*").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                        .requestMatchers("/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                        .requestMatchers("/api/schedules/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                        .requestMatchers("/admin/service-schedules/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")

                        // FUNCIONÁRIO
                        .requestMatchers("/api/employee/all").authenticated() // Lista para agendamento
                        .requestMatchers("/api/employee/me/**").hasAnyAuthority("EMPLOYEE", "ROLE_EMPLOYEE")
                        .requestMatchers("/api/employee/ratings/**").hasAnyAuthority("EMPLOYEE", "ROLE_EMPLOYEE")
                        .requestMatchers("/api/employee/**").hasAnyAuthority("EMPLOYEE", "ADMIN", "ROLE_EMPLOYEE", "ROLE_ADMIN")

                        // SERVIÇOS (Funcionário/Admin/User)
                        .requestMatchers("/api/service-schedules/**").hasAnyAuthority("USER", "EMPLOYEE", "ADMIN", "ROLE_USER", "ROLE_EMPLOYEE", "ROLE_ADMIN")

                        // === AQUI ESTÁ A CORREÇÃO DO ERRO 403 DO FUNCIONÁRIO ===
                        // Permite que EMPLOYEE, ADMIN e VETERINARY acessem os horários de funcionário
                        .requestMatchers("/api/schedules/employee/**").hasAnyAuthority("EMPLOYEE", "ROLE_EMPLOYEE", "ADMIN", "ROLE_ADMIN", "VETERINARY", "ROLE_VETERINARY")

                        // VETERINÁRIO (Área Logada e Gestão)
                        // Rotas específicas do Veterinário
                        .requestMatchers("/vet/**").hasAnyAuthority("VETERINARY", "ROLE_VETERINARY")
                        .requestMatchers("/api/schedules/vet/**").hasAnyAuthority("VETERINARY", "ROLE_VETERINARY")
                        .requestMatchers("/veterinary/ratings/my-ratings").hasAnyAuthority("VETERINARY", "ROLE_VETERINARY")

                        // Consultas e Ações Médicas
                        .requestMatchers("/veterinary/consultations/**").hasAnyAuthority("VETERINARY", "ROLE_VETERINARY")
                        .requestMatchers("/consultas/*/accept", "/consultas/*/reject", "/consultas/*/finalize").hasAnyAuthority("VETERINARY", "ROLE_VETERINARY")
                        .requestMatchers(HttpMethod.PUT, "/consultas/*/report").hasAnyAuthority("VETERINARY", "ROLE_VETERINARY")

                        // Upload e Prescrição
                        .requestMatchers(HttpMethod.POST, "/veterinary/consultations/*/attachments").hasAnyAuthority("VETERINARY", "ROLE_VETERINARY")
                        .requestMatchers(HttpMethod.POST, "/veterinary/consultations/*/prescriptions").hasAnyAuthority("VETERINARY", "ROLE_VETERINARY")

                        // Edição de Perfil Veterinário (Protegido - vem depois do GET público lá em cima)
                        .requestMatchers("/veterinary/**").hasAnyAuthority("VETERINARY", "ADMIN", "ROLE_VETERINARY", "ROLE_ADMIN")


                        // ====================================================
                        // 5. CLIENTE E ROTAS GERAIS
                        // ====================================================
                        .requestMatchers("/agendar-consulta").hasAnyAuthority("USER", "ROLE_USER")
                        .requestMatchers("/consultas/my-consultations").hasAnyAuthority("USER", "ROLE_USER")

                        // Cancelamento (Compartilhado)
                        .requestMatchers(HttpMethod.POST, "/consultas/*/cancel").authenticated()

                        // Prontuários e Pets (Leitura compartilhada)
                        .requestMatchers(HttpMethod.GET, "/api/pets/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/medical-records/**").authenticated()
                        .requestMatchers("/pets/**").authenticated()

                        // Chat e Notificações
                        .requestMatchers("/chat/**").authenticated()
                        .requestMatchers("/upload/**", "/notifications/**").authenticated()

                        // Rotas gerais de consulta (ex: detalhes GET)
                        .requestMatchers("/consultas/**").authenticated()
                        .requestMatchers("/api/available-times/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN", "EMPLOYEE", "ROLE_EMPLOYEE", "USER", "ROLE_USER")

                        // 6. REGRA FINAL
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // CORS TOTALMENTE PERMISSIVO
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("Content-Disposition"); // Útil para download de PDF

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}