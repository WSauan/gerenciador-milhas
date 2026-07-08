package br.com.milhas.gerenciador.service;

import br.com.milhas.gerenciador.model.Usuario;
import br.com.milhas.gerenciador.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AutenticacaoService autenticacaoService;

    @Test
    @DisplayName("Deve carregar usuário pelo e-mail com sucesso no login")
    void deveCarregarUsuarioPorUsernameComSucesso() {
        Usuario usuario = new Usuario();
        usuario.setEmail("welber@email.com");
        usuario.setSenha("123456");

        when(usuarioRepository.findByEmail("welber@email.com")).thenReturn(Optional.of(usuario));

        UserDetails resultado = autenticacaoService.loadUserByUsername("welber@email.com");

        assertNotNull(resultado);
        assertEquals("welber@email.com", resultado.getUsername());
    }

    @Test
    @DisplayName("Deve lançar exceção se o e-mail não for encontrado no login")
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        when(usuarioRepository.findByEmail("invalido@email.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            autenticacaoService.loadUserByUsername("invalido@email.com");
        });
    }
}