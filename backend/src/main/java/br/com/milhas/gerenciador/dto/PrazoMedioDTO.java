package br.com.milhas.gerenciador.dto;
// Classe responsável por representar os dados do prazo médio de recebimento de pontos.
import java.math.BigDecimal;

/**
 * DTO para encapsular o valor do prazo médio de recebimento de pontos.
 */
public record PrazoMedioDTO(
    BigDecimal mediaEmDias
) {
}