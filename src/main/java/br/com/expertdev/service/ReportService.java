package br.com.expertdev.service;

import br.com.expertdev.model.MetricaPerformance;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.jfree.chart.JFreeChart;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço para exportar relatórios executivos de ROI em PDF.
 */
public class ReportService {

    private final PerformanceService performanceService;

    public ReportService(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    public String exportarRelatorioExecutivo(List<MetricaPerformance> metricas, JFreeChart chartTendencia) throws IOException {
        String fileName = "Relatorio_ROI_ExpertDev_" + System.currentTimeMillis() + ".pdf";
        File file = new File(fileName);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Cabeçalho
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("ExpertDev - Relatorio Executivo de ROI");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.newLineAtOffset(50, 735);
                contentStream.showText("Gerado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                contentStream.endText();

                // Sumário de Ganhos
                double totalHorasScrum = 0;
                double totalHorasExpert = 0;
                int tarefasConcluidas = 0;

                for (MetricaPerformance m : metricas) {
                    if (m.getFimExpertDev() != null) {
                        double hExpert = java.time.Duration.between(m.getInicioExpertDev(), m.getFimExpertDev()).toMinutes() / 60.0;
                        double hScrum = 0;
                        if (m.getFimScrum() != null && m.getInicioScrum() != null) {
                            hScrum = java.time.Duration.between(m.getInicioScrum(), m.getFimScrum()).toMinutes() / 60.0;
                        } else if (m.getEstimativaPoker() != null) {
                            hScrum = m.getEstimativaPoker();
                        }

                        if (hScrum > 0) {
                            totalHorasScrum += hScrum;
                            totalHorasExpert += hExpert;
                            tarefasConcluidas++;
                        }
                    }
                }

                float y = 700;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Resumo de Performance");
                contentStream.endText();
                y -= 20;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(60, y);
                contentStream.showText("Tarefas Analisadas: " + tarefasConcluidas);
                y -= 15;
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText(String.format("Total Horas (Scrum/Poker): %.2f h", totalHorasScrum));
                y -= 15;
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText(String.format("Total Horas (ExpertDev): %.2f h", totalHorasExpert));
                y -= 15;
                contentStream.newLineAtOffset(0, -15);
                double economia = totalHorasScrum - totalHorasExpert;
                double perc = totalHorasScrum > 0 ? (economia / totalHorasScrum) * 100 : 0;
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.showText(String.format("Economia Total: %.2f h (%.1f%% de ganho)", economia, perc));
                contentStream.endText();
                y -= 40;

                // Adicionar Gráfico se fornecido
                if (chartTendencia != null) {
                    BufferedImage bi = chartTendencia.createBufferedImage(500, 300);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    javax.imageio.ImageIO.write(bi, "png", baos);
                    PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, baos.toByteArray(), "chart");
                    contentStream.drawImage(pdImage, 50, y - 300, 500, 300);
                    y -= 320;
                }

                // Tabela de detalhes simples
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Detalhes por RTC:");
                contentStream.endText();
                y -= 20;

                contentStream.setFont(PDType1Font.HELVETICA, 10);
                for (MetricaPerformance m : metricas) {
                    if (y < 50) {
                        // New page if needed - simplified
                        break;
                    }
                    contentStream.beginText();
                    contentStream.newLineAtOffset(60, y);
                    String info = String.format("RTC: %s | Est: %.1fh | ExpDev: %.1fh", 
                        m.getRtcNumero(), 
                        (m.getEstimativaPoker() != null ? m.getEstimativaPoker() : 0.0),
                        (m.getFimExpertDev() != null ? java.time.Duration.between(m.getInicioExpertDev(), m.getFimExpertDev()).toMinutes()/60.0 : 0.0));
                    contentStream.showText(info);
                    contentStream.endText();
                    y -= 15;
                }
            }

            document.save(file);
        }

        return file.getAbsolutePath();
    }
}
