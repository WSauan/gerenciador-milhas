package br.com.milhas.gerenciador.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors; // IMPORT ATUALIZADO

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.milhas.gerenciador.dto.ResetSenhaDTO;
import br.com.milhas.gerenciador.dto.SolicitacaoSenhaDTO;
import br.com.milhas.gerenciador.dto.UsuarioAtualizacaoDTO;
import br.com.milhas.gerenciador.dto.UsuarioCadastroDTO;
import br.com.milhas.gerenciador.dto.UsuarioResponseDTO;
import br.com.milhas.gerenciador.model.Usuario;
import br.com.milhas.gerenciador.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- 1. MÉTODO CADASTRAR ATUALIZADO ---
    /**
     * Cadastra um novo usuário a partir do DTO de cadastro.
     * Criptografa a senha antes de salvar.
     */
    @Transactional
    public Usuario cadastrar(UsuarioCadastroDTO dados) {
        if (usuarioRepository.findByEmail(dados.email()).isPresent()) {
            throw new RuntimeException("E-mail já cadastrado.");
        }

        // Converte DTO para Entidade manualmente
        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome());
        usuario.setEmail(dados.email());
        
        // Criptografa a senha que veio no DTO
        String senhaCriptografada = passwordEncoder.encode(dados.senha());
        usuario.setSenha(senhaCriptografada);

        return usuarioRepository.save(usuario);
    }

    // --- Demais métodos permanecem iguais ---

    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Usuario atualizarPerfil(String emailUsuarioLogado, UsuarioAtualizacaoDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (dto.nome() != null && !dto.nome().isBlank()) {
            usuario.setNome(dto.nome());
        }
        
        return usuario;
    }

    @Transactional
    public String solicitarResetSenha(SolicitacaoSenhaDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com este e-mail."));

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

        usuario.setResetToken(token);
        usuario.setResetTokenExpiry(expiryDate);
        usuarioRepository.save(usuario);

        return token;
    }

    @Transactional
    public void resetarSenha(ResetSenhaDTO dto) {
        if (dto.novaSenha() == null || !dto.novaSenha().equals(dto.confirmacaoSenha())) {
            throw new RuntimeException("As senhas não coincidem.");
        }

        Usuario usuario = usuarioRepository.findByResetToken(dto.token())
                .orElseThrow(() -> new RuntimeException("Token inválido ou não encontrado."));

        if (usuario.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado. Solicite uma nova recuperação.");
        }

        String senhaCriptografada = passwordEncoder.encode(dto.novaSenha());
        usuario.setSenha(senhaCriptografada);

        usuario.setResetToken(null);
        usuario.setResetTokenExpiry(null);
        usuarioRepository.save(usuario);
    }
}