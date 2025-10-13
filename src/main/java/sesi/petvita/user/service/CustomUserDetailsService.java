package sesi.petvita.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sesi.petvita.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // ===== LOG DE DIAGNÓSTICO ADICIONADO =====
        System.out.println("\n[DEBUG] Tentando autenticar usuário com e-mail: " + email);

        return userRepository.findByEmail(email)
                .map(user -> {
                    System.out.println("[DEBUG] Usuário encontrado no banco de dados! ID: " + user.getId() + ", Role: " + user.getRole());
                    return user;
                })
                .orElseThrow(() -> {
                    System.err.println("[DEBUG] ERRO: Usuário com e-mail '" + email + "' NÃO foi encontrado no banco de dados.");
                    return new UsernameNotFoundException("Usuário não encontrado com o email: " + email);
                });
    }
}