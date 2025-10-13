package sesi.petvita.report;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.consultation.repository.ConsultationRepository;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate; // ADICIONADO
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // ADICIONADO

@RequiredArgsConstructor
@Service
public class ReportService {

    private final ConsultationRepository consultationRepository;

    // MÉTODO ATUALIZADO para incluir os filtros de data
    public byte[] generateConsultationReportPdf(
            Optional<LocalDate> startDate,
            Optional<LocalDate> endDate,
            Optional<Long> veterinarioId,
            Optional<SpecialityEnum> speciality) throws DocumentException {

        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
            Paragraph title = new Paragraph("Relatório de Consultas", fontTitle);

            // Adiciona informações sobre os filtros de data no título do PDF
            if (startDate.isPresent() && endDate.isPresent()) {
                title.add(new Chunk("\nPeríodo: " + startDate.get() + " a " + endDate.get()));
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
            BaseColor headerBgColor = new BaseColor(52, 152, 219);
            addTableHeader(table, "Data", fontHeader, headerBgColor);
            addTableHeader(table, "Hora", fontHeader, headerBgColor);
            addTableHeader(table, "Paciente", fontHeader, headerBgColor);
            addTableHeader(table, "Veterinário", fontHeader, headerBgColor);
            addTableHeader(table, "Especialidade", fontHeader, headerBgColor);

            // LÓGICA DE FILTRAGEM ATUALIZADA
            // Primeiro, buscamos todos os dados. Em um sistema com muitos dados,
            // o ideal seria criar um método no repositório que já fizesse essa filtragem complexa.
            List<ConsultationModel> allConsultations = consultationRepository.findAll();

            List<ConsultationModel> filteredConsultations = allConsultations.stream()
                    .filter(c -> startDate.map(sd -> !c.getConsultationdate().isBefore(sd)).orElse(true))
                    .filter(c -> endDate.map(ed -> !c.getConsultationdate().isAfter(ed)).orElse(true))
                    .filter(c -> veterinarioId.map(id -> c.getVeterinario().getId().equals(id)).orElse(true))
                    .filter(c -> speciality.map(s -> c.getSpecialityEnum().equals(s)).orElse(true))
                    .collect(Collectors.toList());

            Font fontCell = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            for (ConsultationModel consultation : filteredConsultations) { // Itera sobre a lista JÁ FILTRADA

                String petName = consultation.getPet() != null ? consultation.getPet().getName() : "N/A";
                String vetName = consultation.getVeterinario() != null ? consultation.getVeterinario().getName() : "N/A";

                addTableCell(table, consultation.getConsultationdate().toString(), fontCell);
                addTableCell(table, consultation.getConsultationtime().toString(), fontCell);
                addTableCell(table, petName, fontCell);
                addTableCell(table, vetName, fontCell);
                addTableCell(table, consultation.getSpecialityEnum().name(), fontCell);
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