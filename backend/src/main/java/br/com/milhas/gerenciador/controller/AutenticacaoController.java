package br.com.milhas.gerenciador.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import br.com.milhas.gerenciador.dto.*;
import br.com.milhas.gerenciador.model.Usuario;
import br.com.milhas.gerenciador.security.TokenService;
import br.com.milhas.gerenciador.service.UsuarioService;

@RestController
@RequestMapping("/api")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager manager;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<TokenJwtDTO> efetuarLogin(@RequestBody DadosAutenticacaoDTO dados) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());
        Authentication authentication = manager.authenticate(authenticationToken);
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        String tokenJWT = tokenService.gerarToken(usuarioAutenticado);
        return ResponseEntity.ok(new TokenJwtDTO(tokenJWT));
    }

    // --- MUDANÇA AQUI: Não retorna mais Token, retorna Void (Vazio) ---
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> solicitarResetSenha(@RequestBody SolicitacaoSenhaDTO dto) {
        try {
            usuarioService.solicitarResetSenha(dto);
            // Retorna 200 OK sempre. O usuário verifica o e-mail.
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            // Para testes: Pode retornar erro. 
            // Em produção: Ideal retornar OK para não vazar e-mails cadastrados.
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetarSenha(@RequestBody ResetSenhaDTO dto) {
        try {
            usuarioService.resetarSenha(dto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}