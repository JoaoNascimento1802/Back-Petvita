package sesi.petvita.consultation.status;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "Status da Consulta")
public enum ConsultationStatus {
    @Schema(description = "Consulta solicitada pelo usuário, aguardando aprovação do médico")
    PENDENTE("Pendente"),

    @Schema(description = "Consulta confirmada pelo médico")
    AGENDADA("Agendada"),

    @Schema(description = "O paciente chegou à clínica e aguarda atendimento")
    CHECKED_IN("Check-in Realizado"),

    @Schema(description = "O paciente está em atendimento com o veterinário")
    EM_ANDAMENTO("Em Andamento"),

    @Schema(description = "Consulta foi finalizada e o relatório pode ser preenchido")
    FINALIZADA("Finalizada"),

    @Schema(description = "Consulta foi cancelada pelo médico ou usuário")
    CANCELADA("Cancelada"),

    @Schema(description = "Consulta foi recusada pelo médico")
    RECUSADA("Recusada");

    private final String descricao;
}