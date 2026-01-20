package br.com.milhas.gerenciador.security;

import java.io.IOException; // Exceção para tratamento de erros de I/O.

import org.springframework.beans.factory.annotation.Autowired; // Para continuar a cadeia de filtros de requisições.
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Exceção de servlet para tratamento de erros.
import org.springframework.security.core.context.SecurityContextHolder; // Representa a requisição HTTP.
import org.springframework.security.core.userdetails.UserDetails; // Representa a resposta HTTP.
import org.springframework.stereotype.Component; // Injeção de dependências.
import org.springframework.web.filter.OncePerRequestFilter; // Objeto de autenticação do Spring Security.

import br.com.milhas.gerenciador.repository.UsuarioRepository; // Contexto de segurança do Spring.
import jakarta.servlet.FilterChain; // Detalhes do usuário para autenticação.
import jakarta.servlet.ServletException; // Marca esta classe como um componente Spring.
import jakarta.servlet.http.HttpServletRequest; // Filtro que roda uma vez por requisição.
import jakarta.servlet.http.HttpServletResponse;

@Component // 1. Marca esta classe como um componente gerenciado pelo Spring
public class SecurityFilter extends OncePerRequestFilter { // 2. Garante que o filtro rode 1x por requisição

    @Autowired
    private TokenService tokenService; // Nosso serviço de token para validar

    @Autowired
    private UsuarioRepository usuarioRepository; // Para buscar o usuário no banco

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 3. Recupera o token do cabeçalho da requisição
        var tokenJWT = recuperarToken(request);

        if (tokenJWT != null) {
            // 4. Valida o token e pega o subject (email)
            var subject = tokenService.getSubject(tokenJWT);

            if (subject != null) {
                // 5. Se o token for válido, busca o usuário no banco
                UserDetails usuario = usuarioRepository.findByEmail(subject)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado no filtro"));

                // 6. Cria um objeto de autenticação para o Spring Security
                var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

                // 7. Define o usuário como autenticado no contexto de segurança
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 8. Continua a cadeia de filtros (permite a requisição prosseguir)
        filterChain.doFilter(request, response);
    }

    // Método auxiliar para extrair o token do cabeçalho "Authorization"
    private String recuperarToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            // O token vem no formato "Bearer <token>", então removemos o prefixo "Bearer "
            return authorizationHeader.replace("Bearer ", "").trim();
        }
        return null; // Retorna nulo se não encontrar o cabeçalho
    }
}