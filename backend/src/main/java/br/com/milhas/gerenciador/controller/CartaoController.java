package br.com.milhas.gerenciador.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping; // Imports organizados
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.milhas.gerenciador.dto.CartaoCadastroDTO;
import br.com.milhas.gerenciador.dto.CartaoResponseDTO;
import br.com.milhas.gerenciador.service.CartaoService;

@RestController
@RequestMapping("/api/cartoes")
public class CartaoController {

    @Autowired
    private CartaoService cartaoService;

    @PostMapping
    public ResponseEntity<CartaoResponseDTO> cadastrarCartao(@RequestBody CartaoCadastroDTO dto, Authentication authentication) {
        try {
            String emailUsuarioLogado = authentication.getName();
            CartaoResponseDTO cartaoSalvo = cartaoService.cadastrar(dto, emailUsuarioLogado);
            return ResponseEntity.status(201).body(cartaoSalvo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<CartaoResponseDTO>> listarCartoesDoUsuario(Authentication authentication) {
        String emailUsuarioLogado = authentication.getName();
        List<CartaoResponseDTO> listaDeCartoes = cartaoService.listarPorUsuario(emailUsuarioLogado);
        return ResponseEntity.ok(listaDeCartoes);
    }

    // --- DELETE ATUALIZADO ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirCartao(@PathVariable Long id) {
        try {
            cartaoService.excluir(id);
            return ResponseEntity.noContent().build(); // 204: Sucesso
        } catch (RuntimeException e) {
            // Se cair aqui é porque o ID não existe (404)
            // O erro de integridade (409) não acontece mais devido à limpeza no Service
            return ResponseEntity.notFound().build();
        }
    }
}