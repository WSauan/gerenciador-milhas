package br.com.milhas.gerenciador.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired; // NOVO IMPORT
import org.springframework.http.ResponseEntity; // NOVO IMPORT
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.milhas.gerenciador.dto.UsuarioAtualizacaoDTO;
import br.com.milhas.gerenciador.dto.UsuarioCadastroDTO;
import br.com.milhas.gerenciador.dto.UsuarioResponseDTO;
import br.com.milhas.gerenciador.model.Usuario;
import br.com.milhas.gerenciador.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // --- 1. MÉTODO DE CADASTRO ATUALIZADO ---
    /**
     * Endpoint público para cadastro.
     * Recebe UsuarioCadastroDTO (com senha) e retorna UsuarioResponseDTO (sem senha).
     */
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrarUsuario(@RequestBody UsuarioCadastroDTO dados) {
        try {
            // Chama o serviço passando o DTO de entrada
            Usuario usuarioSalvo = usuarioService.cadastrar(dados);
            
            // Converte a entidade salva para o DTO de resposta (seguro)
            UsuarioResponseDTO resposta = new UsuarioResponseDTO(usuarioSalvo);

            return ResponseEntity.status(201).body(resposta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // --- Demais métodos permanecem iguais ---

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarTodos();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/teste-auth")
    public ResponseEntity<String> testeAutenticacao() {
        return ResponseEntity.ok("Você está autenticado!");
    }

    @PutMapping("/perfil")
    public ResponseEntity<Usuario> atualizarPerfil(
            @RequestBody UsuarioAtualizacaoDTO dto, 
            Authentication authentication
    ) {
        try {
            String emailUsuarioLogado = authentication.getName();
            Usuario usuarioAtualizado = usuarioService.atualizarPerfil(emailUsuarioLogado, dto);
            return ResponseEntity.ok(usuarioAtualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}