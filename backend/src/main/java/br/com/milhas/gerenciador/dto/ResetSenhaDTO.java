package br.com.milhas.gerenciador.dto;
// Classe responsável por representar os dados necessários para o reset de senha.
public record ResetSenhaDTO(
        String token,
        String novaSenha,
        String confirmacaoSenha
) {
}