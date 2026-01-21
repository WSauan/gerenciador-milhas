package br.com.milhas.gerenciador.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.milhas.gerenciador.dto.ResetSenhaDTO;
import br.com.milhas.gerenciador.dto.SolicitacaoSenhaDTO;
import br.com.milhas.gerenciador.dto.UsuarioAtualizacaoDTO;
import br.com.milhas.gerenciador.dto.UsuarioCadastroDTO;
import br.com.milhas.gerenciador.model.Usuario;
import br.com.milhas.gerenciador.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender; // Injeta o enviador de e-mail

    // ... (métodos cadastrar, listarTodos, atualizarPerfil continuam iguais) ...
    // Se quiser, copie eles do arquivo antigo, mas foque na mudança abaixo:

    @Transactional
    public void solicitarResetSenha(SolicitacaoSenhaDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com este e-mail."));

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

        usuario.setResetToken(token);
        usuario.setResetTokenExpiry(expiryDate);
        usuarioRepository.save(usuario);

        // Envia o e-mail em vez de retornar o token
        enviaEmailRecuperacao(usuario.getEmail(), token);
    }

    private void enviaEmailRecuperacao(String emailDestino, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@gerenciadormilhas.com.br");
        message.setTo(emailDestino);
        message.setSubject("Recuperação de Senha - Gerenciador de Milhas");
        
        String link = "http://127.0.0.1:5500/frontend/reset-password.html?token=" + token;
        
        message.setText("Olá!\n\n" +
                "Você solicitou a recuperação de senha.\n" +
                "Clique no link abaixo para criar uma nova senha:\n\n" +
                link + "\n\n" +
                "Ou copie este token: " + token + "\n\n" +
                "Este link expira em 1 hora.");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao enviar e-mail.");
        }
    }

    @Transactional
    public void resetarSenha(ResetSenhaDTO dto) {
        // ... (Mantenha a lógica de validação de senhas e token igual ao anterior) ...
        if (dto.novaSenha() == null || !dto.novaSenha().equals(dto.confirmacaoSenha())) {
            throw new RuntimeException("As senhas não coincidem.");
        }
        Usuario usuario = usuarioRepository.findByResetToken(dto.token())
                .orElseThrow(() -> new RuntimeException("Token inválido ou não encontrado."));

        if (usuario.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expirado.");
        }

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiry(null);
        usuarioRepository.save(usuario);
    }
    
    // --- (Não esqueça de manter o método cadastrar e atualizarPerfil) ---
    @Transactional
    public Usuario cadastrar(UsuarioCadastroDTO dados) {
        if (usuarioRepository.findByEmail(dados.email()).isPresent()) {
            throw new RuntimeException("E-mail já cadastrado.");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome());
        usuario.setEmail(dados.email());
        usuario.setSenha(passwordEncoder.encode(dados.senha()));
        return usuarioRepository.save(usuario);
    }
    
    public java.util.List<br.com.milhas.gerenciador.dto.UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(br.com.milhas.gerenciador.dto.UsuarioResponseDTO::new)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public Usuario atualizarPerfil(String email, UsuarioAtualizacaoDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (dto.nome() != null && !dto.nome().isBlank()) {
            usuario.setNome(dto.nome());
        }
        return usuario;
    }
}