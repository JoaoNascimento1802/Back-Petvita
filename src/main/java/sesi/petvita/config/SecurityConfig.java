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
                                "/v3/api-docs/**",
                                "/swagger-ui/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/veterinary/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/services").permitAll()

                        // --- CORREÇÃO AQUI ---
                        // Permite que qualquer usuário autenticado veja a lista de funcionários.
                        .requestMatchers(HttpMethod.GET, "/api/employee/all").authenticated()

                        // Endpoints para Usuários Autenticados (qualquer role)
                        .requestMatchers("/chat/**", "/notifications/**", "/upload/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users/me", "/consultas/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/users/me").authenticated()

                        // Endpoints para Role "USER"
                        .requestMatchers("/pets/**").hasAnyRole("USER", "EMPLOYEE", "ADMIN")
                        .requestMatchers("/api/service-schedules/**").hasAnyRole("USER", "EMPLOYEE", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/consultas").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/consultas/{id}").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/consultas/{id}/cancel").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/veterinary/{id}/rate").hasRole("USER")

                        // Endpoints para Role "VETERINARY"
                        .requestMatchers("/vet/**").hasRole("VETERINARY")
                        .requestMatchers("/veterinary/medical-records/**").hasRole("VETERINARY")
                        .requestMatchers("/veterinary/consultations/**").hasRole("VETERINARY")
                        .requestMatchers("/veterinary/prescription-templates/**").hasRole("VETERINARY")
                        .requestMatchers("/veterinary/prescriptions/**").hasRole("VETERINARY")
                        .requestMatchers(
                                "/consultas/{id}/accept",
                                "/consultas/{id}/reject",
                                "/consultas/{id}/finalize"
                        ).hasRole("VETERINARY")
                        .requestMatchers(HttpMethod.PUT, "/consultas/{id}/report").hasRole("VETERINARY")

                        // Endpoints para Role "EMPLOYEE"
                        .requestMatchers("/api/employee/**").hasAnyRole("EMPLOYEE", "ADMIN")

                        // Endpoints para Role "ADMIN"
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
        configuration.setAllowedOrigins(List.of(allowedOrigins));
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