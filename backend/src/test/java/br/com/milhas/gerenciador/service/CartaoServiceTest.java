package br.com.milhas.gerenciador.service;

import br.com.milhas.gerenciador.dto.CartaoCadastroDTO;
import br.com.milhas.gerenciador.dto.CartaoResponseDTO;
import br.com.milhas.gerenciador.model.Bandeira;
import br.com.milhas.gerenciador.model.Cartao;
import br.com.milhas.gerenciador.model.ProgramaDePontos;
import br.com.milhas.gerenciador.model.Usuario;
import br.com.milhas.gerenciador.repository.AquisicaoRepository;
import br.com.milhas.gerenciador.repository.BandeiraRepository;
import br.com.milhas.gerenciador.repository.CartaoRepository;
import br.com.milhas.gerenciador.repository.ProgramaDePontosRepository;
import br.com.milhas.gerenciador.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartaoServiceTest {

    @Mock
    private CartaoRepository cartaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BandeiraRepository bandeiraRepository;

    @Mock
    private ProgramaDePontosRepository programaRepository;

    @Mock
    private AquisicaoRepository aquisicaoRepository;

    @InjectMocks
    private CartaoService cartaoService;

    @Test
    @DisplayName("Deve cadastrar um cartão com sucesso quando todas as dependências existirem")
    void deveCadastrarCartaoComSucesso() {
        // Arrange
        String email = "welber@email.com";
        CartaoCadastroDTO dto = new CartaoCadastroDTO("Cartão Black", BigDecimal.ZERO, new BigDecimal("2.5"), 1L, 1L);

        Usuario usuarioFake = new Usuario();
        usuarioFake.setId(10L);
        usuarioFake.setEmail(email);

        Bandeira bandeiraFake = new Bandeira();
        bandeiraFake.setId(1L);
        bandeiraFake.setNome("Visa");

        ProgramaDePontos programaFake = new ProgramaDePontos();
        programaFake.setId(1L);
        programaFake.setNome("Livelo");

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioFake));
        when(bandeiraRepository.findById(1L)).thenReturn(Optional.of(bandeiraFake));
        when(programaRepository.findById(1L)).thenReturn(Optional.of(programaFake));
        when(cartaoRepository.save(any(Cartao.class))).thenAnswer(invocation -> {
            Cartao c = invocation.getArgument(0);
            c.setId(100L);
            return c;
        });

        // Act
        CartaoResponseDTO resultado = cartaoService.cadastrar(dto, email);

        // Assert
        assertNotNull(resultado);
        assertEquals(100L, resultado.id());
        assertEquals("Cartão Black", resultado.nome());
        verify(cartaoRepository, times(1)).save(any(Cartao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar cartão se o usuário não for encontrado")
    void deveLancarExcecaoQuandoUsuarioNaoEncontradoNoCadastro() {
        // Arrange
        String email = "invalido@email.com";
        CartaoCadastroDTO dto = new CartaoCadastroDTO("Teste", BigDecimal.ZERO, BigDecimal.ONE, 1L, 1L);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> cartaoService.cadastrar(dto, email));
        assertEquals("Usuário não encontrado", ex.getMessage());
        verify(cartaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar cartão se a bandeira não for encontrada")
    void deveLancarExcecaoQuandoBandeiraNaoEncontrada() {
        // Arrange
        String email = "welber@email.com";
        CartaoCadastroDTO dto = new CartaoCadastroDTO("Teste", BigDecimal.ZERO, BigDecimal.ONE, 99L, 1L);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(new Usuario()));
        when(bandeiraRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> cartaoService.cadastrar(dto, email));
        assertEquals("Bandeira não encontrada", ex.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar cartão se o programa de pontos não for encontrado")
    void deveLancarExcecaoQuandoProgramaNaoEncontrado() {
        // Arrange
        String email = "welber@email.com";
        CartaoCadastroDTO dto = new CartaoCadastroDTO("Teste", BigDecimal.ZERO, BigDecimal.ONE, 1L, 99L);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(new Usuario()));
        when(bandeiraRepository.findById(1L)).thenReturn(Optional.of(new Bandeira()));
        when(programaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> cartaoService.cadastrar(dto, email));
        assertEquals("Programa de pontos não encontrado", ex.getMessage());
    }

    @Test
    @DisplayName("Deve listar cartões por usuário com sucesso")
    void deveListarCartoesPorUsuarioComSucesso() {
        // Arrange
        String email = "welber@email.com";
        Usuario usuario = new Usuario();
        usuario.setId(10L);

        Cartao cartao = new Cartao();
        cartao.setId(50L);
        cartao.setNome("Meu Cartão");
        cartao.setSaldoDePontos(BigDecimal.ZERO);
        cartao.setFatorConversao(BigDecimal.ONE);
        cartao.setBandeira(new Bandeira());
        cartao.setProgramaDePontos(new ProgramaDePontos());

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(cartaoRepository.findByUsuarioId(10L)).thenReturn(Collections.singletonList(cartao));

        // Act
        List<CartaoResponseDTO> resultado = cartaoService.listarPorUsuario(email);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Meu Cartão", resultado.get(0).nome());
    }

    @Test
    @DisplayName("Deve lançar exceção ao listar se o usuário não existir")
    void deveLancarExcecaoAoListarUsuarioInexistente() {
        // Arrange
        String email = "invalido@email.com";
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> cartaoService.listarPorUsuario(email));
        assertEquals("Usuário não encontrado", ex.getMessage());
    }

    @Test
    @DisplayName("Deve excluir cartão e histórico de aquisições com sucesso")
    void deveExcluirCartaoComSucesso() {
        // Arrange
        Long cartaoId = 1L;
        when(cartaoRepository.existsById(cartaoId)).thenReturn(true);

        // Act
        cartaoService.excluir(cartaoId);

        // Assert
        verify(aquisicaoRepository, times(1)).deleteByCartaoId(cartaoId);
        verify(cartaoRepository, times(1)).deleteById(cartaoId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir cartão inexistente")
    void deveLancarExcecaoAoExcluirInexistente() {
        // Arrange
        Long cartaoId = 99L;
        when(cartaoRepository.existsById(cartaoId)).thenReturn(false);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> cartaoService.excluir(cartaoId));
        assertEquals("Cartão não encontrado", ex.getMessage());
        verify(aquisicaoRepository, never()).deleteByCartaoId(anyLong());
        verify(cartaoRepository, never()).deleteById(anyLong());
    }
}