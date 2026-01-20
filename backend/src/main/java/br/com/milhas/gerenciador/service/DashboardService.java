package br.com.milhas.gerenciador.service;
// classe de serviço responsável por agregar dados para o Dashboard e gerar relatórios.
import br.com.milhas.gerenciador.dto.AquisicaoResponseDTO;
import br.com.milhas.gerenciador.dto.PontosPorCartaoDTO;
import br.com.milhas.gerenciador.dto.PrazoMedioDTO;
import br.com.milhas.gerenciador.repository.AquisicaoRepository;
import br.com.milhas.gerenciador.repository.CartaoRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.List;

/**
 * Serviço responsável por agregar dados para o Dashboard e gerar relatórios.
 */
@Service
public class DashboardService {

    @Autowired
    private CartaoRepository cartaoRepository;
    
    @Autowired
    private AquisicaoService aquisicaoService;

    // 4. INJETAR O NOVO REPOSITÓRIO
    @Autowired
    private AquisicaoRepository aquisicaoRepository;

    
    public List<PontosPorCartaoDTO> getPontosPorCartao(String emailUsuarioLogado) {
        return cartaoRepository.findPontosPorCartaoByUsuarioEmail(emailUsuarioLogado);
    }
    /**
     * Busca o prazo médio de recebimento de pontos para o usuário.
     * @param emailUsuarioLogado O e-mail do usuário.
     * @return Um DTO contendo a média de dias.
     */
    public PrazoMedioDTO getPrazoMedioRecebimento(String emailUsuarioLogado) {
        // 1. Chama a consulta nativa
        BigDecimal media = aquisicaoRepository.findPrazoMedioRecebimentoPorUsuario(emailUsuarioLogado);

        // 2. Se o usuário não tiver nenhuma aquisição, a média virá nula.
        if (media == null) {
            media = BigDecimal.ZERO;
        }

        // 3. Retorna o DTO encapsulando o valor
        return new PrazoMedioDTO(media);
    }


    /** Gera um relatório CSV usando Apache Commons CSV. */
    public void escreverHistoricoAquisicoesCSV(Writer writer, String emailUsuarioLogado) throws IOException {
        List<AquisicaoResponseDTO> historico = aquisicaoService.listarPorUsuario(emailUsuarioLogado);
        String[] headers = {
            "ID Aquisicao", "Descricao", "Valor Gasto", "Pontos Calculados",
            "Data Compra", "Data Prevista Credito", "Status",
            "ID Cartao", "Nome Cartao"
        };
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                .setHeader(headers)
                .build());
        for (AquisicaoResponseDTO aquisicao : historico) {
            csvPrinter.printRecord(
                aquisicao.id(), aquisicao.descricao(), aquisicao.valorGasto(),
                aquisicao.pontosCalculados(), aquisicao.dataCompra(), aquisicao.dataPrevistaCredito(),
                aquisicao.status(), aquisicao.cartaoId(), aquisicao.nomeCartao()
            );
        }
        csvPrinter.flush();
        csvPrinter.close();
    }
    /** Gera um relatório PDF usando iText 7. */
    public void escreverHistoricoAquisicoesPDF(HttpServletResponse response, String emailUsuarioLogado) throws IOException {
        List<AquisicaoResponseDTO> historico = aquisicaoService.listarPorUsuario(emailUsuarioLogado);
        OutputStream outputStream = response.getOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        document.add(new Paragraph("Historico de Aquisicoes de Pontos"));
        
        // Criação da Tabela
        float[] columnWidths = {1, 3, 2, 2, 2, 2, 2, 1, 3};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // Cabeçalhos da Tabela
        table.addHeaderCell(new Cell().add(new Paragraph("ID")));
        table.addHeaderCell(new Cell().add(new Paragraph("Descricao")));
        table.addHeaderCell(new Cell().add(new Paragraph("Valor Gasto")));
        table.addHeaderCell(new Cell().add(new Paragraph("Pontos")));
        table.addHeaderCell(new Cell().add(new Paragraph("Data Compra")));
        table.addHeaderCell(new Cell().add(new Paragraph("Data Credito")));
        table.addHeaderCell(new Cell().add(new Paragraph("Status")));
        table.addHeaderCell(new Cell().add(new Paragraph("Cartao ID")));
        table.addHeaderCell(new Cell().add(new Paragraph("Nome Cartao")));

        // Dados da Tabela
        for (AquisicaoResponseDTO aquisicao : historico) {
            table.addCell(new Cell().add(new Paragraph(aquisicao.id().toString())));
            table.addCell(new Cell().add(new Paragraph(aquisicao.descricao())));
            table.addCell(new Cell().add(new Paragraph(aquisicao.valorGasto().toString())));
            table.addCell(new Cell().add(new Paragraph(aquisicao.pontosCalculados().toString())));
            table.addCell(new Cell().add(new Paragraph(aquisicao.dataCompra().toString())));
            table.addCell(new Cell().add(new Paragraph(aquisicao.dataPrevistaCredito().toString())));
            table.addCell(new Cell().add(new Paragraph(aquisicao.status())));
            table.addCell(new Cell().add(new Paragraph(aquisicao.cartaoId().toString())));
            table.addCell(new Cell().add(new Paragraph(aquisicao.nomeCartao())));
        }
        document.add(table);
        document.close();
        outputStream.flush();
        outputStream.close();
    }
}