package br.com.milhas.gerenciador.dto;
// Classe responsável por representar os dados de atualização do usuário.
/**
 * DTO para receber os dados de atualização de perfil do usuário.
 * Apenas o nome pode ser alterado por este DTO.
 */
public record UsuarioAtualizacaoDTO(
        String nome,
        String senha
) {
}