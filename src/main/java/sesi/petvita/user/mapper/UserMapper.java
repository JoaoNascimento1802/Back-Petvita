package sesi.petvita.user.mapper;

import org.springframework.stereotype.Component;
import sesi.petvita.user.dto.UserRequestDTO;
import sesi.petvita.user.dto.UserResponseDTO;
import sesi.petvita.user.model.UserModel;

@Component
public class UserMapper {

    public UserModel toModel(UserRequestDTO dto) {
        return UserModel.builder()
                .username(dto.username())
                .password(dto.password()) // A senha será codificada no service
                .email(dto.email())
                .phone(dto.phone())
                .address(dto.address())
                .rg(dto.rg())
                .imageurl(dto.imageurl())
                .role(dto.role())
                .build();
    }

    public UserResponseDTO toDTO(UserModel model) {
        return new UserResponseDTO(
                model.getId(),
                model.getUsername(),
                model.getEmail(),
                model.getPhone(),
                model.getAddress(),    // <-- ADICIONADO
                model.getImageurl(),   // <-- ADICIONADO
                model.getRole()
        );
    }
}