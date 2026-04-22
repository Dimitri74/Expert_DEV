package br.com.expertdev.service;

import br.com.expertdev.model.MetricaPerformance;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.jfree.chart.JFreeChart;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço para exportar relatórios executivos de ROI em PDF com layout profissional.
 */
public class ReportService {

    private final PerformanceService performanceService;
    private static final float MARGIN = 50;
    private static final Color COLOR_PRIMARY = new Color(0, 51, 102); // Azul escuro
    private static final Color COLOR_SUCCESS = new Color(0, 102, 0);  // Verde
    private static final Color COLOR_HEADER = new Color(240, 240, 240); // Cinza claro

    public ReportService(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    public String exportarRelatorioExecutivo(List<MetricaPerformance> metricas, JFreeChart chartTendencia) throws IOException {
        String dirPath = "export" + File.separator + "reports";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = "Relatorio_ROI_ExpertDev_" + System.currentTimeMillis() + ".pdf";
        File file = new File(dir, fileName);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float width = page.getMediaBox().getWidth();
            float height = page.getMediaBox().getHeight();
            float y = height - MARGIN;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Logo e Cabeçalho
                try {
                    InputStream is = getClass().getResourceAsStream("/icons/logo_transparente.png");
                    if (is != null) {
                        byte[] logoBytes = toByteArray(is);
                        PDImageXObject logo = PDImageXObject.createFromByteArray(document, logoBytes, "logo");
                        contentStream.drawImage(logo, MARGIN, y - 40, 100, 40);
                    }
                } catch (Exception e) {
                    // Ignora se não conseguir carregar a logo
                }

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.setNonStrokingColor(COLOR_PRIMARY);
                contentStream.newLineAtOffset(MARGIN + 110, y - 20);
                contentStream.showText("Relatório de ROI e Performance");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.newLineAtOffset(MARGIN + 110, y - 35);
                contentStream.showText("ExpertDev Tooling | Gerado em: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                contentStream.endText();

                y -= 80;

                // Linha divisória
                contentStream.setStrokingColor(COLOR_PRIMARY);
                contentStream.setLineWidth(1.5f);
                contentStream.moveTo(MARGIN, y);
                contentStream.lineTo(width - MARGIN, y);
                contentStream.stroke();

                y -= 30;

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

                // Caixa de Resumo
                contentStream.setNonStrokingColor(new Color(245, 245, 245));
                contentStream.addRect(MARGIN, y - 80, width - 2 * MARGIN, 80);
                contentStream.fill();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.setNonStrokingColor(COLOR_PRIMARY);
                contentStream.newLineAtOffset(MARGIN + 15, y - 20);
                contentStream.showText("Resumo Executivo da Sprint");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.newLineAtOffset(MARGIN + 15, y - 40);
                contentStream.showText("Tarefas Analisadas: " + tarefasConcluidas);
                contentStream.newLineAtOffset(200, 0);
                contentStream.showText(String.format("Esforço Estimado (Scrum): %.2f h", totalHorasScrum));
                contentStream.newLineAtOffset(-200, -20);
                contentStream.showText(String.format("Esforço Realizado (ExpertDev): %.2f h", totalHorasExpert));
                
                double economia = totalHorasScrum - totalHorasExpert;
                double perc = totalHorasScrum > 0 ? (economia / totalHorasScrum) * 100 : 0;
                contentStream.newLineAtOffset(200, 0);
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                contentStream.setNonStrokingColor(COLOR_SUCCESS);
                contentStream.showText(String.format("Economia Total: %.2f h (%.1f%%)", economia, perc));
                contentStream.endText();

                y -= 110;

                // Tabela de Detalhes
                drawTableHeader(contentStream, MARGIN, y, width - 2 * MARGIN);
                y -= 20;

                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.setNonStrokingColor(Color.BLACK);
                for (MetricaPerformance m : metricas) {
                    if (y < 350 && chartTendencia != null) { // Abrir espaço para o gráfico ou nova página
                        break; 
                    }
                    
                    double hExpert = m.getFimExpertDev() != null ? java.time.Duration.between(m.getInicioExpertDev(), m.getFimExpertDev()).toMinutes() / 60.0 : 0.0;
                    double hScrum = (m.getEstimativaPoker() != null ? m.getEstimativaPoker() : 0.0);
                    double ganho = hScrum - hExpert;

                    drawTableRow(contentStream, MARGIN, y, width - 2 * MARGIN, 
                        m.getSprint() != null ? String.valueOf(m.getSprint()) : "-",
                        m.getRtcNumero(), 
                        String.format("%.1f h", hScrum), 
                        String.format("%.1f h", hExpert), 
                        String.format("%.1f h", ganho));
                    
                    y -= 18;
                }

                y -= 30;

                // Adicionar Gráfico
                if (chartTendencia != null && y > 300) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.setNonStrokingColor(COLOR_PRIMARY);
                    contentStream.newLineAtOffset(MARGIN, y);
                    contentStream.showText("Tendência de Produtividade");
                    contentStream.endText();
                    
                    y -= 260;

                    BufferedImage bi = chartTendencia.createBufferedImage(500, 240);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    javax.imageio.ImageIO.write(bi, "png", baos);
                    PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, baos.toByteArray(), "chart");
                    contentStream.drawImage(pdImage, MARGIN + 25, y, 500, 240);
                }
            }

            document.save(file);
        }

        return file.getAbsolutePath();
    }

    private void drawTableHeader(PDPageContentStream cs, float x, float y, float width) throws IOException {
        cs.setNonStrokingColor(COLOR_HEADER);
        cs.addRect(x, y - 5, width, 20);
        cs.fill();
        
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
        cs.setNonStrokingColor(COLOR_PRIMARY);
        cs.newLineAtOffset(x + 5, y + 5);
        cs.showText("Sprint");
        cs.newLineAtOffset(50, 0);
        cs.showText("ID Tarefa (RTC)");
        cs.newLineAtOffset(150, 0);
        cs.showText("Est. Scrum");
        cs.newLineAtOffset(100, 0);
        cs.showText("ExpertDev");
        cs.newLineAtOffset(100, 0);
        cs.showText("Economia");
        cs.endText();

        cs.setStrokingColor(Color.LIGHT_GRAY);
        cs.setLineWidth(0.5f);
        cs.moveTo(x, y - 5);
        cs.lineTo(x + width, y - 5);
        cs.stroke();
    }

    private void drawTableRow(PDPageContentStream cs, float x, float y, float width, String sprint, String rtc, String scrum, String expert, String ganho) throws IOException {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.setNonStrokingColor(Color.BLACK);
        cs.newLineAtOffset(x + 5, y);
        cs.showText(sprint);
        cs.newLineAtOffset(50, 0);
        cs.showText(rtc);
        cs.newLineAtOffset(150, 0);
        cs.showText(scrum);
        cs.newLineAtOffset(100, 0);
        cs.showText(expert);
        cs.newLineAtOffset(100, 0);
        cs.setNonStrokingColor(COLOR_SUCCESS);
        cs.showText(ganho);
        cs.endText();

        cs.setStrokingColor(new Color(240, 240, 240));
        cs.setLineWidth(0.5f);
        cs.moveTo(x, y - 5);
        cs.lineTo(x + width, y - 5);
        cs.stroke();
    }

    private byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}
