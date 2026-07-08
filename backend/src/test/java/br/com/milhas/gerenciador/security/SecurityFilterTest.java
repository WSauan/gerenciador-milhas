package br.com.milhas.gerenciador.security;

import br.com.milhas.gerenciador.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock private TokenService tokenService;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private SecurityFilter securityFilter;

    @Test
    @DisplayName("Deve prosseguir com a cadeia de filtros quando não houver header Authorization")
    void deveProsseguirSemToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve prosseguir sem autenticar quando o token for inválido")
    void deveProsseguirComTokenInvalido() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-invalido");
        when(tokenService.getSubject("token-invalido")).thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}