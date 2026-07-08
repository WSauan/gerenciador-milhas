package br.com.milhas.gerenciador.dto;

import br.com.milhas.gerenciador.model.Aquisicao;
import br.com.milhas.gerenciador.model.Cartao;
import br.com.milhas.gerenciador.model.StatusCredito;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AquisicaoResponseDTOTest {

    @Test
    @DisplayName("Deve retornar PENDENTE se o status da entidade for nulo")
    void deveRetornarPendenteQuandoStatusNull() {
        Aquisicao a = criarAquisicaoBase();
        a.setStatus(null);
        
        AquisicaoResponseDTO dto = new AquisicaoResponseDTO(a);
        assertEquals("PENDENTE", dto.status());
    }

    @Test
    @DisplayName("Deve retornar CANCELADO quando o status real for CANCELADO")
    void deveRetornarCancelado() {
        Aquisicao a = criarAquisicaoBase();
        a.setStatus(StatusCredito.CANCELADO);
        
        AquisicaoResponseDTO dto = new AquisicaoResponseDTO(a);
        assertEquals("CANCELADO", dto.status());
    }

    @Test
    @DisplayName("Deve retornar APROVADO quando o status real for CREDITADO")
    void deveRetornarAprovadoQuandoCreditado() {
        Aquisicao a = criarAquisicaoBase();
        a.setStatus(StatusCredito.CREDITADO);
        
        AquisicaoResponseDTO dto = new AquisicaoResponseDTO(a);
        assertEquals("APROVADO", dto.status());
    }

    @Test
    @DisplayName("Deve retornar APROVADO visualmente se estiver PENDENTE mas a data for hoje")
    void deveRetornarAprovadoQuandoPendenteEDataForHoje() {
        Aquisicao a = criarAquisicaoBase();
        a.setStatus(StatusCredito.PENDENTE);
        a.setDataPrevistaCredito(LocalDate.now()); // Data de hoje
        
        AquisicaoResponseDTO dto = new AquisicaoResponseDTO(a);
        assertEquals("APROVADO", dto.status());
    }

    @Test
    @DisplayName("Deve retornar APROVADO visualmente se estiver PENDENTE mas a data já passou")
    void deveRetornarAprovadoQuandoPendenteEDataJaPassou() {
        Aquisicao a = criarAquisicaoBase();
        a.setStatus(StatusCredito.PENDENTE);
        a.setDataPrevistaCredito(LocalDate.now().minusDays(2)); // Passado
        
        AquisicaoResponseDTO dto = new AquisicaoResponseDTO(a);
        assertEquals("APROVADO", dto.status());
    }

    @Test
    @DisplayName("Deve retornar PENDENTE se a data de crédito ainda for no futuro")
    void deveRetornarPendenteQuandoPendenteEDataFutura() {
        Aquisicao a = criarAquisicaoBase();
        a.setStatus(StatusCredito.PENDENTE);
        a.setDataPrevistaCredito(LocalDate.now().plusDays(5)); // Futuro
        
        AquisicaoResponseDTO dto = new AquisicaoResponseDTO(a);
        assertEquals("PENDENTE", dto.status());
    }

    @Test
    @DisplayName("Deve retornar PENDENTE se a data de previsão for nula")
    void deveRetornarPendenteQuandoDataNull() {
        Aquisicao a = criarAquisicaoBase();
        a.setStatus(StatusCredito.PENDENTE);
        a.setDataPrevistaCredito(null); 
        
        AquisicaoResponseDTO dto = new AquisicaoResponseDTO(a);
        assertEquals("PENDENTE", dto.status());
    }

    @Test
    @DisplayName("Deve mapear como 'Cartão Excluído' quando a aquisição não tiver cartão")
    void deveMapearCartaoNulo() {
        Aquisicao a = criarAquisicaoBase();
        a.setCartao(null); 
        
        AquisicaoResponseDTO dto = new AquisicaoResponseDTO(a);
        assertNull(dto.cartaoId());
        assertEquals("Cartão Excluído", dto.nomeCartao());
    }

    // Método utilitário para não repetir código
    private Aquisicao criarAquisicaoBase() {
        Aquisicao a = new Aquisicao();
        a.setId(1L);
        a.setDescricao("Teste");
        a.setValorGasto(BigDecimal.TEN);
        a.setPontosCalculados(BigDecimal.TEN);
        a.setDataCompra(LocalDate.now());
        a.setCaminhoComprovante("caminho/teste.pdf");
        
        Cartao c = new Cartao();
        c.setId(10L);
        c.setNome("Visa Teste");
        a.setCartao(c);
        
        return a;
    }
}