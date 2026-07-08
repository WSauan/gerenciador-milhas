package br.com.milhas.gerenciador.controller;

import br.com.milhas.gerenciador.dto.PontosPorCartaoDTO;
import br.com.milhas.gerenciador.dto.PrazoMedioDTO;
import br.com.milhas.gerenciador.service.DashboardService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    @DisplayName("Deve retornar 200 OK ao buscar pontos por cartão no dashboard")
    void deveObterPontosPorCartao() {
        PontosPorCartaoDTO dto = new PontosPorCartaoDTO(1L, "Visa Black", new BigDecimal("5000"));
        when(authentication.getName()).thenReturn("welber@email.com");
        when(dashboardService.getPontosPorCartao("welber@email.com")).thenReturn(Collections.singletonList(dto));

        ResponseEntity<List<PontosPorCartaoDTO>> resposta = dashboardController.getPontosPorCartao(authentication);

        assertNotNull(resposta);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(1, resposta.getBody().size());
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao buscar prazo médio no dashboard")
    void deveObterPrazoMedioRecebimento() {
        PrazoMedioDTO dto = new PrazoMedioDTO(new BigDecimal("12"));
        when(authentication.getName()).thenReturn("welber@email.com");
        when(dashboardService.getPrazoMedioRecebimento("welber@email.com")).thenReturn(dto);

        ResponseEntity<PrazoMedioDTO> resposta = dashboardController.getPrazoMedioRecebimento(authentication);

        assertNotNull(resposta);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(new BigDecimal("12"), resposta.getBody().mediaEmDias());
    }

    @Test
    @DisplayName("Deve executar exportação CSV pelo controller com sucesso")
    void deveExportarHistoricoCSV() throws IOException {
        when(authentication.getName()).thenReturn("welber@email.com");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));
        doNothing().when(dashboardService).escreverHistoricoAquisicoesCSV(any(), anyString());

        assertDoesNotThrow(() -> dashboardController.exportarHistoricoCSV(response, authentication));
    }

    @Test
    @DisplayName("Deve executar exportação PDF pelo controller com sucesso")
    void deveExportarHistoricoPDF() throws IOException {
        when(authentication.getName()).thenReturn("welber@email.com");
        doNothing().when(dashboardService).escreverHistoricoAquisicoesPDF(any(), anyString());

        assertDoesNotThrow(() -> dashboardController.exportarHistoricoPDF(response, authentication));
    }
}