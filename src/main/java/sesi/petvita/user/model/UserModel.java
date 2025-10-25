package sesi.petvita.user.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.pet.model.PetModel;
import sesi.petvita.user.role.UserRole;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserModel implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false)
    private String username;

    @NotBlank
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
    @Column(nullable = false)
    private String password;

    @Email
    @NotBlank
    @Size(max = 100)
    @Column(unique = true , nullable = false)
    private String email;

    @NotBlank
    @Column(unique = true , nullable = false)
    private String phone;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String address;

    @NotBlank
    @Column(unique = true , nullable = false)
    private String rg;

    @Column(nullable = false)
    private String imageurl;

    private String imagePublicId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @JsonManagedReference
    @OneToMany(mappedBy = "usuario" ,cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    private List<ConsultationModel> consultas;

    @JsonManagedReference
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PetModel> pets;

    /**
     * Retorna o nome de usuário real.
     * O método getUsername() é sobrescrito para retornar o e-mail para autenticação do Spring Security.
     * Este método é usado em outras partes do sistema onde o nome de usuário (e não o e-mail) é necessário.
     * @return O nome do usuário.
     */
    public String getActualUsername() {
        return this.username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        // Spring Security usará o e-mail como "username" para fins de autenticação
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}