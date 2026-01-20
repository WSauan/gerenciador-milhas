package br.com.milhas.gerenciador.controller;
// Classe responsável por gerenciar os endpoints relacionados à autenticação e recuperação de senha.
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.milhas.gerenciador.dto.DadosAutenticacaoDTO;
import br.com.milhas.gerenciador.dto.ResetSenhaDTO;
import br.com.milhas.gerenciador.dto.RespostaTokenDTO;
import br.com.milhas.gerenciador.dto.SolicitacaoSenhaDTO;
import br.com.milhas.gerenciador.dto.TokenJwtDTO;
import br.com.milhas.gerenciador.model.Usuario;
import br.com.milhas.gerenciador.security.TokenService;
import br.com.milhas.gerenciador.service.UsuarioService;

@RestController
@RequestMapping("/api") // Define o URL base para "/api"
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager manager; // Gerencia o processo de autenticação

    @Autowired
    private TokenService tokenService; // Serviço para gerar tokens JWT

    @Autowired
    private UsuarioService usuarioService; // 3. INJETAR O SERVIÇO DE USUÁRIO

    /**
     * Endpoint público para efetuar login.
     * Acessível via: POST /api/login
     */
    @PostMapping("/login")
    public ResponseEntity<TokenJwtDTO> efetuarLogin(@RequestBody DadosAutenticacaoDTO dados) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());
        Authentication authentication = manager.authenticate(authenticationToken);
        var usuarioAutenticado = (Usuario) authentication.getPrincipal();
        String tokenJWT = tokenService.gerarToken(usuarioAutenticado);
        return ResponseEntity.ok(new TokenJwtDTO(tokenJWT));
    }
    /**
     * Endpoint público para solicitar a recuperação de senha.
     * Acessível via: POST /api/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<RespostaTokenDTO> solicitarResetSenha(@RequestBody SolicitacaoSenhaDTO dto) {
        try {
            String token = usuarioService.solicitarResetSenha(dto);
            // Retorna o token para o usuário (simulando envio de e-mail)
            return ResponseEntity.ok(new RespostaTokenDTO(token));
        } catch (RuntimeException e) {
            // Não retornamos "Usuário não encontrado" por segurança (para não vazar e-mails)
            // Mas para o teste, vamos retornar o erro:
            return ResponseEntity.badRequest().build();
        }
    }
    /**
     * Endpoint público para efetivar a troca de senha.
     * Acessível via: POST /api/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetarSenha(@RequestBody ResetSenhaDTO dto) {
        try {
            usuarioService.resetarSenha(dto);
            // Retorna 200 OK (Vazio) se a senha foi trocada
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            // Retorna 400 Bad Request se o token for inválido, expirado ou senhas não baterem
            return ResponseEntity.badRequest().build();
        }
    }
}