package br.com.milhas.gerenciador.dto;
// Classe responsável por representar os dados necessários para o cadastro de um cartão.
import java.math.BigDecimal;

public record CartaoCadastroDTO(
        String nome,
        BigDecimal saldoDePontos,
        BigDecimal fatorConversao,
        Long bandeiraId,
        Long programaId
) {
}