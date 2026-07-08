package br.com.milhas.gerenciador.service;

import br.com.milhas.gerenciador.dto.AquisicaoCadastroDTO;
import br.com.milhas.gerenciador.dto.AquisicaoResponseDTO;
import br.com.milhas.gerenciador.model.Aquisicao;
import br.com.milhas.gerenciador.model.Cartao;
import br.com.milhas.gerenciador.model.Usuario;
import br.com.milhas.gerenciador.repository.AquisicaoRepository;
import br.com.milhas.gerenciador.repository.CartaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AquisicaoServiceTest {

    @Mock
    private CartaoRepository cartaoRepository;

    @Mock
    private AquisicaoRepository aquisicaoRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private AquisicaoService aquisicaoService;

    @Test
    @DisplayName("Deve calcular os pontos corretamente ao registrar aquisição")
    void deveCalcularPontosCorretamente() {
        // 1. PREPARAÇÃO (CENÁRIO)
        String emailUsuario = "teste@email.com";
        Long cartaoId = 1L;
        BigDecimal valorGasto = new BigDecimal("100.00");
        BigDecimal fatorConversao = new BigDecimal("2.0");

        Usuario usuarioFake = new Usuario();
        usuarioFake.setEmail(emailUsuario);

        Cartao cartaoFake = new Cartao();
        cartaoFake.setId(cartaoId);
        cartaoFake.setUsuario(usuarioFake);
        cartaoFake.setFatorConversao(fatorConversao);
        cartaoFake.setSaldoDePontos(BigDecimal.ZERO);

        AquisicaoCadastroDTO dto = new AquisicaoCadastroDTO(
                "Compra Teste",
                valorGasto,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                cartaoId
        );

        MockMultipartFile arquivoFake = new MockMultipartFile("comprovante", "teste.pdf", "application/pdf", "bytes".getBytes());

        // 2. COMPORTAMENTO DOS MOCKS
        when(cartaoRepository.findById(cartaoId)).thenReturn(Optional.of(cartaoFake));
        when(cartaoRepository.save(any(Cartao.class))).thenAnswer(i -> i.getArguments()[0]);
        when(aquisicaoRepository.save(any(Aquisicao.class))).thenAnswer(invocation -> {
            Aquisicao a = invocation.getArgument(0);
            a.setId(1L);
            return a;
        });
        when(fileStorageService.storeFile(any(), any())).thenReturn("arquivo-salvo.pdf");

        // 3. EXECUÇÃO
        AquisicaoResponseDTO resultado = aquisicaoService.registrarAquisicao(dto, arquivoFake, emailUsuario);

        // 4. VERIFICAÇÃO
        assertNotNull(resultado);
        BigDecimal pontosEsperados = new BigDecimal("200.00");
        assertEquals(0, resultado.pontosCalculados().compareTo(pontosEsperados));
        assertEquals(0, cartaoFake.getSaldoDePontos().compareTo(pontosEsperados));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar registrar aquisição com cartão inexistente")
    void deveLancarExcecaoQuandoCartaoNaoEncontrado() {
        // Arrange
        Long cartaoId = 99L;
        AquisicaoCadastroDTO dto = new AquisicaoCadastroDTO("Teste", BigDecimal.TEN, LocalDate.now(), LocalDate.now(), cartaoId);
        MockMultipartFile arquivoFake = new MockMultipartFile("comprovante", "t.pdf", "text/plain", "b".getBytes());
        
        when(cartaoRepository.findById(cartaoId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            aquisicaoService.registrarAquisicao(dto, arquivoFake, "user@email.com");
        });

        assertEquals("Cartão não encontrado", exception.getMessage());
        verify(cartaoRepository, never()).save(any());
        verify(aquisicaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o cartão não pertencer ao usuário logado")
    void deveLancarExcecaoQuandoCartaoNaoPertenceAoUsuario() {
        // Arrange
        Long cartaoId = 1L;
        Usuario usuarioDono = new Usuario();
        usuarioDono.setEmail("dono@email.com");

        Cartao cartaoFake = new Cartao();
        cartaoFake.setId(cartaoId);
        cartaoFake.setUsuario(usuarioDono);

        AquisicaoCadastroDTO dto = new AquisicaoCadastroDTO("Teste", BigDecimal.TEN, LocalDate.now(), LocalDate.now(), cartaoId);
        MockMultipartFile arquivoFake = new MockMultipartFile("comprovante", "t.pdf", "text/plain", "b".getBytes());

        when(cartaoRepository.findById(cartaoId)).thenReturn(Optional.of(cartaoFake));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            aquisicaoService.registrarAquisicao(dto, arquivoFake, "usuario-intruso@email.com");
        });

        assertEquals("Este cartão não pertence ao usuário logado.", exception.getMessage());
        verify(cartaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar aquisições por usuário logado com sucesso")
    void deveListarAquisicoesPorUsuario() {
        // Arrange
        String email = "teste@email.com";
        Aquisicao aquisicao = new Aquisicao();
        aquisicao.setId(10L);
        aquisicao.setDescricao("Compra de Passagem");
        aquisicao.setValorGasto(new BigDecimal("500"));
        aquisicao.setPontosCalculados(new BigDecimal("1000"));
        
        Cartao c = new Cartao();
        c.setId(1L);
        aquisicao.setCartao(c);

        when(aquisicaoRepository.findByCartaoUsuarioEmail(email)).thenReturn(Collections.singletonList(aquisicao));

        // Act
        List<AquisicaoResponseDTO> resultado = aquisicaoService.listarPorUsuario(email);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).id());
        assertEquals("Compra de Passagem", resultado.get(0).descricao());
    }

    @Test
    @DisplayName("Deve excluir aquisição e estornar os pontos do cartão com sucesso")
    void deveExcluirAquisicaoEEstornarPontos() {
        // Arrange
        Long aquisicaoId = 5L;
        
        Cartao cartao = new Cartao();
        cartao.setId(1L);
        cartao.setSaldoDePontos(new BigDecimal("2500.00"));

        Aquisicao aquisicao = new Aquisicao();
        aquisicao.setId(aquisicaoId);
        aquisicao.setPontosCalculados(new BigDecimal("500.00"));
        aquisicao.setCartao(cartao);

        when(aquisicaoRepository.findById(aquisicaoId)).thenReturn(Optional.of(aquisicao));

        // Act
        aquisicaoService.excluir(aquisicaoId);

        // Assert
        BigDecimal saldoEsperadoAposEstorno = new BigDecimal("2000.00");
        assertEquals(0, cartao.getSaldoDePontos().compareTo(saldoEsperadoAposEstorno));
        
        verify(cartaoRepository, times(1)).save(cartao);
        verify(aquisicaoRepository, times(1)).delete(aquisicao);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir compra inexistente")
    void deveLancarExcecaoAoExcluirInexistente() {
        // Arrange
        Long idInexistente = 999L;
        when(aquisicaoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            aquisicaoService.excluir(idInexistente);
        });

        assertEquals("Compra não encontrada", exception.getMessage());
        verify(cartaoRepository, never()).save(any());
        verify(aquisicaoRepository, never()).delete(any());
    }
    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tiver aquisições")
    void deveRetornarListaVaziaQuandoUsuarioNaoTiverAquisicoes() {
        // Arrange
        String email = "sem.aquisicoes@email.com";
        when(aquisicaoRepository.findByCartaoUsuarioEmail(email)).thenReturn(Collections.emptyList());

        // Act
        List<AquisicaoResponseDTO> resultado = aquisicaoService.listarPorUsuario(email);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}