package sesi.petvita.config;

import lombok.RequiredArgsConstructor;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Rotas públicas
                        .requestMatchers(
                                "/auth/login",
                                "/users/register",
                                "/auth/forgot-password",
                                "/auth/reset-password",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/veterinary/**").permitAll() // Horários de vets são públicos
                        .requestMatchers(HttpMethod.GET, "/admin/clinic-services").permitAll() // Listar serviços é público para agendamento

                        // Rotas de Usuário (USER)
                        .requestMatchers("/pets/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/consultas").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/service-schedules").hasRole("USER") // Agendar serviços
                        .requestMatchers(HttpMethod.PUT, "/consultas/{id}").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/consultas/{id}/cancel").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/veterinary/{id}/rate").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/pets/{petId}/medical-records/**").hasRole("USER")

                        // Rotas de Veterinário (VETERINARY)
                        .requestMatchers("/vet/work-schedule/**").hasRole("VETERINARY")
                        .requestMatchers("/consultas/{id}/accept", "/consultas/{id}/reject", "/consultas/{id}/finalize").hasRole("VETERINARY")
                        .requestMatchers(HttpMethod.PUT, "/consultas/{id}/report").hasRole("VETERINARY")
                        .requestMatchers(HttpMethod.GET, "/veterinary/me/**").hasRole("VETERINARY")
                        .requestMatchers(HttpMethod.GET, "/consultas/vet/my-consultations").hasRole("VETERINARY")

                        // Rotas de Funcionário (EMPLOYEE)
                        .requestMatchers("/api/employee/**").hasRole("EMPLOYEE")

                        // Rotas de Admin (ADMIN)
                        .requestMatchers("/admin/clinic-services/**").hasRole("ADMIN") // Apenas admin gerencia serviços
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/reports/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/veterinary").hasRole("ADMIN")

                        // Rotas compartilhadas por autenticados
                        .requestMatchers("/chat/**").hasAnyRole("USER", "ADMIN", "VETERINARY", "EMPLOYEE")
                        .requestMatchers("/notifications/**").hasAnyRole("USER", "ADMIN", "VETERINARY", "EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/upload/**").hasAnyRole("USER", "ADMIN", "VETERINARY", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/consultas/**").authenticated()

                        // Qualquer outra requisição precisa de autenticação
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
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "https://vet-clinic-api-front.vercel.app"));
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
