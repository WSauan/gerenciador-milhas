package br.com.milhas.gerenciador.security;

import br.com.milhas.gerenciador.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        // Injeta a chave secreta usando a classe utilitária do Spring que você já tem no classpath
        ReflectionTestUtils.setField(tokenService, "secret", "minhachavesecretamuitograndeparatestes1234567890123456");
    }

    @Test
    @DisplayName("Deve gerar o token JWT com sucesso")
    void deveGerarToken() {
        Usuario usuario = new Usuario();
        usuario.setEmail("welber@email.com");

        String token = tokenService.gerarToken(usuario);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("Deve extrair o e-mail (subject) de um token válido")
    void deveRetornarSubjectCorreto() {
        Usuario usuario = new Usuario();
        usuario.setEmail("welber@email.com");

        String token = tokenService.gerarToken(usuario);
        String subject = tokenService.getSubject(token);

        assertEquals("welber@email.com", subject);
    }

    @Test
    @DisplayName("Deve retornar nulo se o token for inválido ou nulo")
    void deveRetornarNuloSeTokenInvalido() {
        assertNull(tokenService.getSubject(null));
        assertNull(tokenService.getSubject("token.invalido.assinatura"));
    }
}