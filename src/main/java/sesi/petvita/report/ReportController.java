package sesi.petvita.report;

import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // MÉTODO ATUALIZADO para receber os parâmetros de data
    @GetMapping("/consultations-pdf")
    public ResponseEntity<byte[]> getConsultationsPdfReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> endDate,
            @RequestParam Optional<Long> veterinarioId,
            @RequestParam Optional<SpecialityEnum> speciality) {

        try {
            // A chamada agora passa todos os parâmetros, incluindo as datas
            byte[] pdfBytes = reportService.generateConsultationReportPdf(startDate, endDate, veterinarioId, speciality);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "relatorio_consultas_" + LocalDate.now() + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (DocumentException e) {
            System.err.println("Erro ao gerar relatório PDF de consultas: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}