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
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 1. ENDPOINTS PÚBLICOS (Acesso total sem login)
                        .requestMatchers(
                                "/auth/**",
                                "/users/register",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/api/public/**"
                        ).permitAll()

                        // GET em /veterinary é público (para busca no agendamento)
                        .requestMatchers(HttpMethod.GET, "/veterinary/**").permitAll()

                        // POST/PUT/DELETE em /veterinary é restrito a Vets e Admins (CRÍTICO PARA PRESCRIÇÃO/PERFIL)
                        .requestMatchers("/veterinary/**").hasAnyRole("VETERINARY", "ADMIN")

                        // 2. ENDPOINTS ESPECÍFICOS (Ordem importa: Específicos antes dos genéricos)
                        .requestMatchers(HttpMethod.GET, "/admin/consultations").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers("/api/employee/all").authenticated() // Qualquer autenticado pode listar funcionários para agendar

                        // 3. ENDPOINTS DE ADMIN (Genérico)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/schedules/admin/**").hasRole("ADMIN")

                        // 4. ENDPOINTS DE PRONTUÁRIO E PETS (Acesso compartilhado)
                        .requestMatchers(HttpMethod.GET, "/api/pets/{petId}/medical-records").hasAnyRole("USER", "VETERINARY", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/medical-records/**").hasAnyRole("USER", "VETERINARY", "ADMIN")
                        .requestMatchers("/pets/**").hasAnyRole("USER", "VETERINARY", "ADMIN")

                        // 5. ENDPOINTS DE SERVIÇOS E FUNCIONÁRIOS
                        .requestMatchers("/api/service-schedules/**").hasAnyRole("USER", "EMPLOYEE", "ADMIN")
                        .requestMatchers("/api/employee/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/api/schedules/employee/my-schedule").hasRole("EMPLOYEE")

                        // 6. ENDPOINTS DE VETERINÁRIO (Área logada)
                        .requestMatchers("/vet/**").hasRole("VETERINARY")
                        .requestMatchers("/api/schedules/vet/my-schedule").hasRole("VETERINARY")
                        .requestMatchers(
                                "/consultas/{id:[0-9]+}/accept",
                                "/consultas/{id:[0-9]+}/reject",
                                "/consultas/{id:[0-9]+}/finalize"
                        ).hasRole("VETERINARY")
                        .requestMatchers(HttpMethod.PUT, "/consultas/{id:[0-9]+}/report").hasRole("VETERINARY")

                        // 7. ENDPOINTS DE CLIENTE (USER)
                        .requestMatchers("/agendar-consulta").hasRole("USER")
                        .requestMatchers("/consultas/my-consultations").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/consultas").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/consultas/{id:[0-9]+}").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/veterinary/{id:[0-9]+}/rate").hasRole("USER")

                        // 8. ENDPOINTS COMPARTILHADOS DE CONSULTA
                        .requestMatchers(HttpMethod.POST, "/consultas/{id:[0-9]+}/cancel").hasAnyRole("USER", "VETERINARY")
                        .requestMatchers(HttpMethod.GET, "/consultas/{id:[0-9]+}").hasAnyRole("USER", "VETERINARY", "ADMIN", "EMPLOYEE")
                        // Regra geral para outras rotas de consulta não mapeadas acima
                        .requestMatchers("/consultas/**").authenticated()

                        // 9. ENDPOINTS GERAIS AUTENTICADOS (Perfil, Upload, Chat, Notificações)
                        .requestMatchers(
                                "/users/me",
                                "/upload/**",
                                "/chat/**",
                                "/notifications/**"
                        ).authenticated()

                        // 10. REGRA FINAL (Segurança padrão)
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
        // Permitindo origens locais e de produção
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

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