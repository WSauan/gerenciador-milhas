package br.com.milhas.gerenciador.dto;

import br.com.milhas.gerenciador.model.Usuario;

/**
 * DTO utilizado para devolver dados de usuário nas respostas da API.
 * NÃO contém a senha, garantindo a segurança.
 */
public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email
) {
    // Construtor auxiliar para converter a Entidade em DTO
    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}