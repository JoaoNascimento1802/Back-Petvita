package sesi.petvita.report;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.consultation.repository.ConsultationRepository;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ReportService {

    private final ConsultationRepository consultationRepository;

    // Adiciona a anotação @Transactional(readOnly = true) para manter a sessão do BD aberta durante a execução do método
    @Transactional(readOnly = true)
    public byte[] generateConsultationReportPdf(
            Optional<LocalDate> startDateOpt,
            Optional<LocalDate> endDateOpt,
            Optional<Long> veterinarioId,
            Optional<SpecialityEnum> speciality) throws DocumentException {

        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
            Paragraph title = new Paragraph("Relatório de Consultas", fontTitle);

            if (startDateOpt.isPresent() && endDateOpt.isPresent()) {
                title.add(new Chunk("\nPeríodo: " + startDateOpt.get().format(formatter) + " a " + endDateOpt.get().format(formatter)));
            }

            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            float[] columnWidths = {1.5f, 1f, 2f, 2f, 2f};
            table.setWidths(columnWidths);

            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            BaseColor headerBgColor = new BaseColor(74, 85, 104); // Cor mais sóbria
            addTableHeader(table, "Data", fontHeader, headerBgColor);
            addTableHeader(table, "Hora", fontHeader, headerBgColor);
            addTableHeader(table, "Paciente (Pet)", fontHeader, headerBgColor);
            addTableHeader(table, "Veterinário", fontHeader, headerBgColor);
            addTableHeader(table, "Especialidade", fontHeader, headerBgColor);

            // Utiliza o método do repositório que já faz o JOIN FETCH, garantindo que os dados venham carregados
            List<ConsultationModel> filteredConsultations = consultationRepository.findWithFilters(
                    startDateOpt.orElse(null),
                    endDateOpt.orElse(null),
                    veterinarioId.orElse(null),
                    speciality.orElse(null)
            );

            Font fontCell = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            for (ConsultationModel consultation : filteredConsultations) {
                // Como usamos JOIN FETCH, o acesso aos dados relacionados agora é seguro.
                String petName = consultation.getPet().getName();
                String vetName = consultation.getVeterinario().getName();

                addTableCell(table, consultation.getConsultationdate().format(formatter), fontCell);
                addTableCell(table, consultation.getConsultationtime().toString(), fontCell);
                addTableCell(table, petName, fontCell);
                addTableCell(table, vetName, fontCell);
                addTableCell(table, consultation.getSpecialityEnum().getDescricao(), fontCell);
            }
            document.add(table);
            document.close();
            return baos.toByteArray();

        } catch (DocumentException e) {
            System.err.println("Erro ao gerar PDF: " + e.getMessage());
            throw e;
        }
    }

    private void addTableHeader(PdfPTable table, String headerText, Font font, BaseColor bgColor) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(bgColor);
        header.setPadding(8);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
        header.setBorderColor(BaseColor.WHITE);
        header.setPhrase(new Phrase(headerText, font));
        table.addCell(header);
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }
}