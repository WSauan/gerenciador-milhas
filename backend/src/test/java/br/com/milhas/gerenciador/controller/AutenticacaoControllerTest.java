package br.com.milhas.gerenciador.controller;

import br.com.milhas.gerenciador.dto.*;
import br.com.milhas.gerenciador.model.Usuario;
import br.com.milhas.gerenciador.security.TokenService;
import br.com.milhas.gerenciador.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacaoControllerTest {

    @Mock
    private AuthenticationManager manager;

    @Mock
    private TokenService tokenService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AutenticacaoController autenticacaoController;

    @Test
    @DisplayName("Deve efetuar login com sucesso e retornar o Token JWT")
    void deveEfetuarLoginComSucesso() {
        DadosAutenticacaoDTO dados = new DadosAutenticacaoDTO("WELBER@EMAIL.COM", "123456");
        Authentication authMock = mock(Authentication.class);
        Usuario usuario = new Usuario();
        usuario.setNome("Welber");
        usuario.setEmail("welber@email.com");

        when(manager.authenticate(any())).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(usuario);
        when(tokenService.gerarToken(usuario)).thenReturn("token-jwt-fake");

        ResponseEntity<TokenJwtDTO> resposta = autenticacaoController.efetuarLogin(dados);

        assertNotNull(resposta);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("token-jwt-fake", resposta.getBody().token());
        assertEquals("Welber", resposta.getBody().nome());
    }

    @Test
    @DisplayName("Deve solicitar redefinição de senha com sucesso")
    void deveSolicitarResetSenhaComSucesso() {
        SolicitacaoSenhaDTO dto = new SolicitacaoSenhaDTO("welber@email.com");
        doNothing().when(usuarioService).solicitarResetSenha(dto);

        ResponseEntity<Void> resposta = autenticacaoController.solicitarResetSenha(dto);

        assertNotNull(resposta);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao falhar solicitação de senha")
    void deveRetornarBadRequestNaSolicitacao() {
        SolicitacaoSenhaDTO dto = new SolicitacaoSenhaDTO("invalido@email.com");
        doThrow(new RuntimeException("Erro")).when(usuarioService).solicitarResetSenha(dto);

        ResponseEntity<Void> resposta = autenticacaoController.solicitarResetSenha(dto);

        assertNotNull(resposta);
        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
    }

    @Test
    @DisplayName("Deve redefinir senha com sucesso")
    void deveResetarSenhaComSucesso() {
        ResetSenhaDTO dto = new ResetSenhaDTO("token", "nova", "nova");
        doNothing().when(usuarioService).resetarSenha(dto);

        ResponseEntity<Void> resposta = autenticacaoController.resetarSenha(dto);

        assertNotNull(resposta);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao falhar redefinição com token inválido")
    void deveRetornarBadRequestNoReset() {
        ResetSenhaDTO dto = new ResetSenhaDTO("token-invalido", "nova", "nova");
        doThrow(new RuntimeException("Erro")).when(usuarioService).resetarSenha(dto);

        ResponseEntity<Void> resposta = autenticacaoController.resetarSenha(dto);

        assertNotNull(resposta);
        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
    }

    @Test
    @DisplayName("Deve atualizar perfil através do principal logado")
    void deveAtualizarPerfilPeloPrincipal() {
        UsuarioUsuarioLogadoMock usuarioLogado = new UsuarioUsuarioLogadoMock("perfil@email.com");
        UsuarioAtualizacaoDTO dados = new UsuarioAtualizacaoDTO("Welber Alterado", "senha");
        Usuario usuarioRetorno = new Usuario();
        usuarioRetorno.setId(1L);
        usuarioRetorno.setNome("Welber Alterado");
        usuarioRetorno.setEmail("perfil@email.com");

        when(usuarioService.atualizarPerfil("perfil@email.com", dados)).thenReturn(usuarioRetorno);

        ResponseEntity<UsuarioResponseDTO> resposta = autenticacaoController.atualizarPerfil(dados, usuarioLogado);

        assertNotNull(resposta);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("Welber Alterado", resposta.getBody().nome());
    }

    // Subclasse interna mock estática para contornar herança/detalhes do UserDetails do Spring Security
    private static class UsuarioUsuarioLogadoMock extends Usuario {
        private final String email;
        public UsuarioUsuarioLogadoMock(String email) { this.email = email; }
        @Override
        public String getEmail() { return this.email; }
    }
}