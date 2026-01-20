package br.com.milhas.gerenciador.dto;
// Classe responsável por representar os dados de autenticação do usuário.
/** 
* DTO para capturar os dados de autenticação do usuário (email e senha)
* Dados que o frontend enviará ao tentar autenticar um usuário.
*/
public record DadosAutenticacaoDTO(String email, String senha) {
}