package br.com.milhas.gerenciador.controller;

import br.com.milhas.gerenciador.dto.UsuarioAtualizacaoDTO;
import br.com.milhas.gerenciador.dto.UsuarioCadastroDTO;
import br.com.milhas.gerenciador.dto.UsuarioResponseDTO;
import br.com.milhas.gerenciador.model.Usuario;
import br.com.milhas.gerenciador.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UsuarioController usuarioController;

    @Test
    @DisplayName("Deve cadastrar usuário com sucesso e retornar 201 Created")
    void deveCadastrarUsuarioComSucesso() {
        UsuarioCadastroDTO cadastroDTO = new UsuarioCadastroDTO("Welber", "welber@email.com", "senha123");
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Welber");
        usuario.setEmail("welber@email.com");

        when(usuarioService.cadastrar(cadastroDTO)).thenReturn(usuario);

        ResponseEntity<UsuarioResponseDTO> resposta = usuarioController.cadastrarUsuario(cadastroDTO);

        assertNotNull(resposta);
        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertEquals("welber@email.com", resposta.getBody().email());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao falhar cadastro de usuário")
    void deveRetornarBadRequestAoFalharCadastro() {
        UsuarioCadastroDTO cadastroDTO = new UsuarioCadastroDTO("Welber", "welber@email.com", "senha123");
        when(usuarioService.cadastrar(cadastroDTO)).thenThrow(new RuntimeException("Erro"));

        ResponseEntity<UsuarioResponseDTO> resposta = usuarioController.cadastrarUsuario(cadastroDTO);

        assertNotNull(resposta);
        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertNull(resposta.getBody());
    }

    @Test
    @DisplayName("Deve listar todos os usuários e retornar 200 OK")
    void deveListarUsuarios() {
        UsuarioResponseDTO dto = new UsuarioResponseDTO(1L, "Welber", "welber@email.com");
        when(usuarioService.listarTodos()).thenReturn(Collections.singletonList(dto));

        ResponseEntity<List<UsuarioResponseDTO>> resposta = usuarioController.listarUsuarios();

        assertNotNull(resposta);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(1, resposta.getBody().size());
    }

    @Test
    @DisplayName("Deve validar endpoint de teste de autenticação")
    void deveValidarTesteAuth() {
        ResponseEntity<String> resposta = usuarioController.testeAutenticacao();
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("Você está autenticado!", resposta.getBody());
    }

    @Test
    @DisplayName("Deve atualizar perfil com sucesso e retornar 200 OK")
    void deveAtualizarPerfilComSucesso() {
        UsuarioAtualizacaoDTO dto = new UsuarioAtualizacaoDTO("Welber Novo", "novaSenha");
        Usuario usuario = new Usuario();
        usuario.setNome("Welber Novo");

        when(authentication.getName()).thenReturn("welber@email.com");
        when(usuarioService.atualizarPerfil("welber@email.com", dto)).thenReturn(usuario);

        ResponseEntity<Usuario> resposta = usuarioController.atualizarPerfil(dto, authentication);

        assertNotNull(resposta);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("Welber Novo", resposta.getBody().getNome());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao falhar atualização de perfil")
    void deveRetornarBadRequestAoFalharAtualizacao() {
        UsuarioAtualizacaoDTO dto = new UsuarioAtualizacaoDTO("Welber Novo", "novaSenha");
        when(authentication.getName()).thenReturn("welber@email.com");
        when(usuarioService.atualizarPerfil("welber@email.com", dto)).thenThrow(new RuntimeException("Erro"));

        ResponseEntity<Usuario> resposta = usuarioController.atualizarPerfil(dto, authentication);

        assertNotNull(resposta);
        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
    }
}