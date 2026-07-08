package br.com.milhas.gerenciador.service;

import br.com.milhas.gerenciador.dto.ResetSenhaDTO;
import br.com.milhas.gerenciador.dto.SolicitacaoSenhaDTO;
import br.com.milhas.gerenciador.dto.UsuarioAtualizacaoDTO;
import br.com.milhas.gerenciador.dto.UsuarioCadastroDTO;
import br.com.milhas.gerenciador.dto.UsuarioResponseDTO;
import br.com.milhas.gerenciador.model.Usuario;
import br.com.milhas.gerenciador.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Deve cadastrar um usuário com sucesso se o e-mail não existir")
    void deveCadastrarUsuarioComSucesso() {
        // Arrange
        UsuarioCadastroDTO dto = new UsuarioCadastroDTO("Welber", "WELBER@email.com", "senha123");
        when(usuarioRepository.findByEmail("welber@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("senhaCripto");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Usuario resultado = usuarioService.cadastrar(dto);

        // Assert
        assertNotNull(resultado);
        assertEquals("welber@email.com", resultado.getEmail());
        assertEquals("senhaCripto", resultado.getSenha());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar se o e-mail já estiver cadastrado")
    void deveLancarExcecaoQuandoEmailJaCadastrado() {
        // Arrange
        UsuarioCadastroDTO dto = new UsuarioCadastroDTO("Welber", "welber@email.com", "senha123");
        when(usuarioRepository.findByEmail("welber@email.com")).thenReturn(Optional.of(new Usuario()));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioService.cadastrar(dto));
        assertEquals("E-mail já cadastrado.", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar todos os usuários cadastrados com sucesso")
    void deveListarTodosOsUsuarios() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Welber");
        usuario.setEmail("welber@email.com");
        when(usuarioRepository.findAll()).thenReturn(Collections.singletonList(usuario));

        // Act
        List<UsuarioResponseDTO> resultado = usuarioService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Welber", resultado.get(0).nome());
    }

    @Test
    @DisplayName("Deve atualizar o perfil do usuário corretamente mudando nome e senha")
    void deveAtualizarPerfilCompleto() {
        // Arrange
        String email = "welber@email.com";
        Usuario usuario = new Usuario();
        usuario.setNome("Nome Antigo");
        usuario.setSenha("senhaAntiga");

        UsuarioAtualizacaoDTO dto = new UsuarioAtualizacaoDTO("Nome Novo", "senhaNova");

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("senhaNova")).thenReturn("senhaNovaCripto");

        // Act
        Usuario resultado = usuarioService.atualizarPerfil(email, dto);

        // Assert
        assertEquals("Nome Novo", resultado.getNome());
        assertEquals("senhaNovaCripto", resultado.getSenha());
    }

    @Test
    @DisplayName("Deve manter os dados antigos se a atualização vier com valores vazios ou nulos")
    void deveManterDadosAntigosNaAtualizacaoSeVazio() {
        // Arrange
        String email = "welber@email.com";
        Usuario usuario = new Usuario();
        usuario.setNome("Welber");
        usuario.setSenha("senhaFirme");

        UsuarioAtualizacaoDTO dto = new UsuarioAtualizacaoDTO("", " ");

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

        // Act
        Usuario resultado = usuarioService.atualizarPerfil(email, dto);

        // Assert
        assertEquals("Welber", resultado.getNome());
        assertEquals("senhaFirme", resultado.getSenha());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("Deve gerar o token de recuperação e enviar o e-mail com sucesso")
    void deveSolicitarResetSenhaComSucesso() {
        // Arrange
        SolicitacaoSenhaDTO dto = new SolicitacaoSenhaDTO("WELBER@email.com");
        Usuario usuario = new Usuario();
        usuario.setEmail("welber@email.com");

        when(usuarioRepository.findByEmail("welber@email.com")).thenReturn(Optional.of(usuario));

        // Act
        usuarioService.solicitarResetSenha(dto);

        // Assert
        assertNotNull(usuario.getResetToken());
        assertNotNull(usuario.getResetTokenExpiry());
        verify(usuarioRepository, times(1)).save(usuario);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Deve redefinir a nova senha com sucesso ao validar token válido")
    void deveResetarSenhaComSucesso() {
        // Arrange
        ResetSenhaDTO dto = new ResetSenhaDTO("token-valido", "novaSenha123", "novaSenha123");
        Usuario usuario = new Usuario();
        usuario.setResetToken("token-valido");
        usuario.setResetTokenExpiry(LocalDateTime.now().plusHours(1));

        when(usuarioRepository.findByResetToken("token-valido")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novaSenhaCripto");

        // Act
        usuarioService.resetarSenha(dto);

        // Assert
        assertEquals("novaSenhaCripto", usuario.getSenha());
        assertNull(usuario.getResetToken());
        assertNull(usuario.getResetTokenExpiry());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Deve lançar exceção ao redefinir se as senhas informadas não coincidirem")
    void deveLancarExcecaoSeSenhasNaoCoincidirem() {
        // Arrange
        ResetSenhaDTO dto = new ResetSenhaDTO("token", "senha1", "senha2");

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioService.resetarSenha(dto));
        assertEquals("As senhas não coincidem.", ex.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção se tentar redefinir com token expirado")
    void deveLancarExcecaoSeTokenExpirado() {
        // Arrange
        ResetSenhaDTO dto = new ResetSenhaDTO("token-expirado", "senha1", "senha1");
        Usuario usuario = new Usuario();
        usuario.setResetToken("token-expirado");
        usuario.setResetTokenExpiry(LocalDateTime.now().minusMinutes(1)); // Expirado há 1 minuto

        when(usuarioRepository.findByResetToken("token-expirado")).thenReturn(Optional.of(usuario));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioService.resetarSenha(dto));
        assertEquals("Token expirado.", ex.getMessage());
    }

    // =========================================================================================
    // NOVOS TESTES ADICIONADOS PARA COMPLETAR AS BRANCHES (NADA ACIMA FOI REMOVIDO)
    // =========================================================================================

    @Test
    @DisplayName("Deve lançar exceção ao solicitar reset com e-mail inexistente")
    void deveLancarExcecaoAoSolicitarResetComEmailInexistente() {
        SolicitacaoSenhaDTO dto = new SolicitacaoSenhaDTO("fantasma@email.com");
        when(usuarioRepository.findByEmail("fantasma@email.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioService.solicitarResetSenha(dto));
        assertTrue(ex.getMessage().toLowerCase().contains("não encontrado"));
    }

    @Test
    @DisplayName("Deve lançar exceção se tentar redefinir com token inválido ou não encontrado")
    void deveLancarExcecaoSeTokenInvalido() {
        ResetSenhaDTO dto = new ResetSenhaDTO("token-invalido", "senha1", "senha1");
        when(usuarioRepository.findByResetToken("token-invalido")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioService.resetarSenha(dto));
        assertTrue(ex.getMessage().toLowerCase().contains("inválido"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar perfil de usuário inexistente")
    void deveLancarExcecaoAoAtualizarPerfilDeUsuarioInexistente() {
        UsuarioAtualizacaoDTO dto = new UsuarioAtualizacaoDTO("Nome Novo", "Senha");
        when(usuarioRepository.findByEmail("fantasma@email.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> usuarioService.atualizarPerfil("fantasma@email.com", dto));
        assertTrue(ex.getMessage().toLowerCase().contains("não encontrado"));
    }
}