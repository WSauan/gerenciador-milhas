package br.com.milhas.gerenciador.service;

import br.com.milhas.gerenciador.dto.PontosPorCartaoDTO;
import br.com.milhas.gerenciador.dto.PrazoMedioDTO;
import br.com.milhas.gerenciador.repository.AquisicaoRepository;
import br.com.milhas.gerenciador.repository.CartaoRepository;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private CartaoRepository cartaoRepository;

    @Mock
    private AquisicaoRepository aquisicaoRepository;

    @Mock
    private AquisicaoService aquisicaoService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Deve obter a agregação de pontos por cartão com sucesso")
    void deveCalcularPontosPorCartao() {
        // Arrange
        String email = "welber@email.com";
        PontosPorCartaoDTO dtoFake = new PontosPorCartaoDTO(1L, "Visa Black", new BigDecimal("5000"));
        
        when(cartaoRepository.findPontosPorCartaoByUsuarioEmail(email)).thenReturn(Collections.singletonList(dtoFake));

        // Act
        List<PontosPorCartaoDTO> resultado = dashboardService.getPontosPorCartao(email);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Visa Black", resultado.get(0).nomeCartao());
        verify(cartaoRepository, times(1)).findPontosPorCartaoByUsuarioEmail(email);
    }

    @Test
    @DisplayName("Deve obter o prazo médio de recebimento quando houver aquisições")
    void deveObterPrazoMedioComDados() {
        // Arrange
        String email = "welber@email.com";
        BigDecimal mediaFake = new BigDecimal("15.5");
        
        when(aquisicaoRepository.findPrazoMedioRecebimentoPorUsuario(email)).thenReturn(mediaFake);

        // Act
        PrazoMedioDTO resultado = dashboardService.getPrazoMedioRecebimento(email);

        // Assert
        assertNotNull(resultado);
        assertEquals(mediaFake, resultado.mediaEmDias());
        verify(aquisicaoRepository, times(1)).findPrazoMedioRecebimentoPorUsuario(email);
    }

    @Test
    @DisplayName("Deve retornar prazo médio igual a zero quando a consulta nativa retornar nulo")
    void deveRetornarZeroQuandoMediaForNula() {
        // Arrange
        String email = "welber@email.com";
        
        when(aquisicaoRepository.findPrazoMedioRecebimentoPorUsuario(email)).thenReturn(null);

        // Act
        PrazoMedioDTO resultado = dashboardService.getPrazoMedioRecebimento(email);

        // Assert
        assertNotNull(resultado);
        assertEquals(BigDecimal.ZERO, resultado.mediaEmDias());
        verify(aquisicaoRepository, times(1)).findPrazoMedioRecebimentoPorUsuario(email);
    }

    @Test
    @DisplayName("Deve executar a geração do relatório CSV com sucesso")
    void deveEscreverHistoricoAquisicoesCSV() throws IOException {
        // Arrange
        String email = "welber@email.com";
        Writer mockWriter = mock(Writer.class);
        
        when(aquisicaoService.listarPorUsuario(email)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertDoesNotThrow(() -> dashboardService.escreverHistoricoAquisicoesCSV(mockWriter, email));
        verify(aquisicaoService, times(1)).listarPorUsuario(email);
    }

    @Test
    @DisplayName("Deve executar a geração do relatório PDF com sucesso")
    void deveEscreverHistoricoAquisicoesPDF() throws IOException {
        // Arrange
        String email = "welber@email.com";
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        
        // Criação de uma implementação anônima segura para evitar problemas de assinatura do ServletOutputStream
        ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setWriteListener(WriteListener writeListener) {}
            @Override
            public void write(int b) throws IOException {}
        };
        
        when(mockResponse.getOutputStream()).thenReturn(servletOutputStream);
        when(aquisicaoService.listarPorUsuario(email)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertDoesNotThrow(() -> dashboardService.escreverHistoricoAquisicoesPDF(mockResponse, email));
        verify(aquisicaoService, times(1)).listarPorUsuario(email);
        verify(mockResponse, times(1)).getOutputStream();
    }
}