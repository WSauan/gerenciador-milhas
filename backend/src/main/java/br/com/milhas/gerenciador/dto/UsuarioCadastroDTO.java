package br.com.milhas.gerenciador.dto;

/**
 * DTO utilizado APENAS para o recebimento de dados no cadastro.
 * Contém a senha "crua" que será criptografada pelo Service.
 */
public record UsuarioCadastroDTO(
        String nome,
        String email,
        String senha
) {
}