// sesi/petvita/config/SecurityConfig.java
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


                        // 1. ENDPOINTS PÚBLICOS
                        .requestMatchers(
                                "/auth/**",

                                "/users/register",
                                "/veterinary",
                                "/v3/api-docs/**",

                                "/swagger-ui/**",
                                "/api/public/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/veterinary/**").permitAll()


                        // 2. ENDPOINTS DE ADMIN (Prioridade Alta)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/schedules/admin/**").hasRole("ADMIN")
                        .requestMatchers("/reports/**").hasRole("ADMIN")
                        // --- NOVA ROTA DE ADMIN ADICIONADA ---
                        .requestMatchers("/admin/service-schedules/**").hasRole("ADMIN")


                        // 3. ENDPOINTS DE FUNCIONÁRIO
                        // --- CORREÇÃO APLICADA AQUI ---
                        // Permite que qualquer usuário autenticado VEJA a lista de funcionários (necessário para agendamento)

                        .requestMatchers(HttpMethod.GET, "/api/employee/all").authenticated()
                        // Protege todas as outras ações de /api/employee/ (como POST, PUT, DELETE)
                        .requestMatchers("/api/employee/**").hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/api/schedules/employee/my-schedule").hasRole("EMPLOYEE")

                        .requestMatchers(HttpMethod.GET, "/admin/consultations").hasAnyRole("ADMIN", "EMPLOYEE")

                        // 4. ENDPOINTS DE VETERINÁRIO
                        .requestMatchers("/vet/**").hasRole("VETERINARY")
                        .requestMatchers("/api/schedules/vet/my-schedule").hasRole("VETERINARY")

                        .requestMatchers(
                                "/consultas/{id:[0-9]+}/accept",
                                "/consultas/{id:[0-9]+}/reject",

                                "/consultas/{id:[0-9]+}/finalize"
                        ).hasRole("VETERINARY")
                        .requestMatchers(HttpMethod.PUT, "/consultas/{id:[0-9]+}/report").hasRole("VETERINARY")

                        // 5. ENDPOINTS DE CLIENTE (USER)

                        .requestMatchers("/pets/**", "/agendar-consulta", "/api/service-schedules/**").hasRole("USER")
                        .requestMatchers("/consultas/my-consultations").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/consultas").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/consultas/{id:[0-9]+}").hasRole("USER")

                        .requestMatchers(HttpMethod.POST, "/veterinary/{id:[0-9]+}/rate").hasRole("USER")

                        // 6. ENDPOINTS COMPARTILHADOS
                        .requestMatchers(HttpMethod.POST, "/consultas/{id:[0-9]+}/cancel").hasAnyRole("USER", "VETERINARY")
                        .requestMatchers(HttpMethod.GET, "/consultas/{id:[0-9]+}").hasAnyRole("USER", "VETERINARY", "ADMIN", "EMPLOYEE")


                        // 7. ENDPOINTS GERAIS AUTENTICADOS (Qualquer usuário logado)
                        .requestMatchers(
                                "/users/me",

                                "/upload/**",
                                "/chat/**",
                                "/notifications/**"

                        ).authenticated()

                        // 8. REGRA FINAL
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // ... (resto do arquivo: corsConfigurationSource, authenticationProvider, etc.) ...
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