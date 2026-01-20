package br.com.milhas.gerenciador.dto;
// Classe responsável por representar os dados necessários para o cadastro de uma aquisição de milhas.
import java.math.BigDecimal;
import java.time.LocalDate;

// DTO que o frontend enviará (como JSON)
public record AquisicaoCadastroDTO(
        String descricao,
        BigDecimal valorGasto,
        LocalDate dataCompra,
        LocalDate dataPrevistaCredito,
        Long cartaoId
) {
}