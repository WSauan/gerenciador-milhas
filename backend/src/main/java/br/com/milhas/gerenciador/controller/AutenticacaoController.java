package br.com.milhas.gerenciador.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.milhas.gerenciador.dto.DadosAutenticacaoDTO;
import br.com.milhas.gerenciador.dto.ResetSenhaDTO;
import br.com.milhas.gerenciador.dto.SolicitacaoSenhaDTO;
import br.com.milhas.gerenciador.dto.TokenJwtDTO;
import br.com.milhas.gerenciador.dto.UsuarioAtualizacaoDTO;
import br.com.milhas.gerenciador.dto.UsuarioResponseDTO;
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
        
        String emailPadronizado = dados.email().toLowerCase();

        var authenticationToken = new UsernamePasswordAuthenticationToken(emailPadronizado, dados.senha());
        Authentication authentication = manager.authenticate(authenticationToken);
        Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
        String tokenJWT = tokenService.gerarToken(usuarioAutenticado);
        return ResponseEntity.ok(new TokenJwtDTO(tokenJWT, usuarioAutenticado.getNome()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> solicitarResetSenha(@RequestBody SolicitacaoSenhaDTO dto) {
        try {
            // A conversão para minúsculo já está sendo feita dentro do usuarioService.solicitarResetSenha
            usuarioService.solicitarResetSenha(dto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            // Retorna erro 400 se não encontrar (para fins de desenvolvimento)
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

    @PutMapping("/perfil")
    public ResponseEntity<UsuarioResponseDTO> atualizarPerfil(
            @RequestBody UsuarioAtualizacaoDTO dados,
            @AuthenticationPrincipal Usuario usuarioLogado) { 
        
        Usuario usuarioAtualizado = usuarioService.atualizarPerfil(usuarioLogado.getEmail(), dados);

        return ResponseEntity.ok(new UsuarioResponseDTO(usuarioAtualizado));
    }
}