package sesi.petvita.veterinary.speciality;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "Especialidades da medicina veterinária e serviços gerais")
public enum SpecialityEnum {

    // Especialidades Médicas
    CLINICO_GERAL("Clínico Geral"),
    ANESTESIOLOGIA("Anestesiologia"),
    CARDIOLOGIA("Cardiologia"),
    DERMATOLOGIA("Dermatologia"),
    ENDOCRINOLOGIA("Endocrinologia"),
    GASTROENTEROLOGIA("Gastroenterologia"),
    NEUROLOGIA("Neurologia"),
    NUTRICAO("Nutrição"),
    OFTALMOLOGIA("Oftalmologia"),
    ONCOLOGIA("Oncologia"),
    ORTOPEDIA("Ortopedia"),
    REPRODUCAO_ANIMAL("Reprodução Animal"),
    PATOLOGIA("Patologia"),
    CIRURGIA_GERAL("Cirurgia Geral"),
    CIRURGIA_ORTOPEDICA("Cirurgia Ortopédica"),
    ODONTOLOGIA("Odontologia"),
    ZOOTECNIA("Zootecnia"),
    EXOTICOS("Animais Exóticos"),
    ACUPUNTURA("Acupuntura"),
    FISIOTERAPIA("Fisioterapia"),
    IMAGINOLOGIA("Diagnóstico por Imagem"),

    // Especialidade para serviços não-médicos
    ESTETICA("Estética");

    private final String descricao;
}