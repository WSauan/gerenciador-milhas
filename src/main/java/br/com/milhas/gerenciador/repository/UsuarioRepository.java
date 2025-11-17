package br.com.milhas.gerenciador.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.milhas.gerenciador.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // --- Método que já existia ---
    Optional<Usuario> findByEmail(String email);

    // --- 1. NOVO MÉTODO ADICIONADO ---
    /**
     * Busca um usuário pelo seu token de recuperação de senha.
     * @param resetToken O token (UUID) a ser buscado.
     * @return Um Optional contendo o usuário, se encontrado.
     */
    Optional<Usuario> findByResetToken(String resetToken);
}