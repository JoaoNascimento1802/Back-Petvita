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

                        // Endpoints Públicos
                        .requestMatchers(
                                "/auth/**",
                                "/users/register",
                                // --- CORREÇÃO APLICADA AQUI ---
                                // Permite o cadastro de novos veterinários sem autenticação.
                                "/veterinary",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/api/public/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/veterinary/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/employee/all").authenticated()

                        // Endpoints Autenticados (Qualquer Role)
                        .requestMatchers(
                                "/users/me",
                                "/upload/**",
                                "/chat/**",
                                "/notifications/**"
                        ).authenticated()

                        // Regras de AÇÃO para o VETERINÁRIO
                        .requestMatchers(
                                "/consultas/{id:[0-9]+}/accept",
                                "/consultas/{id:[0-9]+}/reject",
                                "/consultas/{id:[0-9]+}/finalize"
                        ).hasRole("VETERINARY")
                        .requestMatchers(HttpMethod.PUT, "/consultas/{id:[0-9]+}/report").hasRole("VETERINARY")

                        // Regras de AÇÃO que ambos (USER e VETERINARY) podem fazer
                        .requestMatchers(HttpMethod.POST, "/consultas/{id:[0-9]+}/cancel").hasAnyRole("USER", "VETERINARY")

                        // Regras específicas de AÇÃO para o USUÁRIO
                        .requestMatchers(HttpMethod.PUT, "/consultas/{id:[0-9]+}").hasRole("USER")

                        // Regra de VISUALIZAÇÃO de detalhes
                        .requestMatchers(HttpMethod.GET, "/consultas/{id:[0-9]+}").hasAnyRole("USER", "VETERINARY", "ADMIN", "EMPLOYEE")

                        // Demais endpoints do Cliente (USER)
                        .requestMatchers("/pets/**", "/agendar-consulta", "/api/service-schedules/**").hasRole("USER")
                        .requestMatchers("/consultas/my-consultations").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/consultas").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/veterinary/{id:[0-9]+}/rate").hasRole("USER")

                        // Endpoints do Veterinário (VETERINARY)
                        .requestMatchers("/vet/**").hasRole("VETERINARY")

                        // Endpoints do Funcionário (EMPLOYEE)
                        .requestMatchers("/api/employee/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/admin/consultations").hasAnyRole("ADMIN", "EMPLOYEE")

                        // Endpoints do Administrador (ADMIN)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/reports/**").hasRole("ADMIN")

                        // Qualquer outra requisição deve ser autenticada
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