package br.com.milhas.gerenciador.service;

import org.springframework.beans.factory.annotation.Autowired; // 1. IMPORTAR
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.milhas.gerenciador.dto.UsuarioAtualizacaoDTO;
import br.com.milhas.gerenciador.model.Usuario;
import br.com.milhas.gerenciador.repository.UsuarioRepository; // 2. IMPORTAR

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Método que já existia ---
    public Usuario cadastrar(Usuario usuario) {
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("E-mail já cadastrado.");
        }
        
        String senhaCriptografada = passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(senhaCriptografada);
        
        return usuarioRepository.save(usuario);
    }

    // --- 3. NOVO MÉTODO ADICIONADO ---
    /**
     * Atualiza o nome do usuário logado.
     * @param emailUsuarioLogado O e-mail do usuário (vindo do token).
     * @param dto Os dados de atualização (contendo o novo nome).
     * @return O objeto Usuario atualizado.
     */
    @Transactional // Garante que a operação seja atômica (ou tudo ou nada)
    public Usuario atualizarPerfil(String emailUsuarioLogado, UsuarioAtualizacaoDTO dto) {
        
        // 1. Busca o usuário no banco
        Usuario usuario = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 2. Valida se o novo nome não está vazio
        if (dto.nome() != null && !dto.nome().isBlank()) {
            usuario.setNome(dto.nome());
        }
        
        // 3. O Spring Data JPA/Hibernate salva a alteração automaticamente
        //    ao final do método, graças ao @Transactional.
        //    (Mas poderíamos chamar usuarioRepository.save(usuario) explicitamente se quiséssemos)
        return usuario;
    }
}