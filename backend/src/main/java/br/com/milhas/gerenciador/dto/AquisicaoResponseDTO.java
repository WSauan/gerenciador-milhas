package br.com.milhas.gerenciador.dto;
// Classe responsável por representar os dados de resposta de uma aquisição de milhas.
import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.milhas.gerenciador.model.Aquisicao;

// DTO que nossa API devolverá (como JSON)
public record AquisicaoResponseDTO(
        Long id,
        String descricao,
        BigDecimal valorGasto,
        BigDecimal pontosCalculados,
        LocalDate dataCompra,
        LocalDate dataPrevistaCredito,
        String status,
        String caminhoComprovante,
        Long cartaoId,
        String nomeCartao
) {

    // Construtor para converter a Entidade Aquisicao neste DTO
    public AquisicaoResponseDTO(Aquisicao aquisicao) {
        this(
                aquisicao.getId(),
                aquisicao.getDescricao(),
                aquisicao.getValorGasto(),
                aquisicao.getPontosCalculados(),
                aquisicao.getDataCompra(),
                aquisicao.getDataPrevistaCredito(),
                aquisicao.getStatus().name(), // Converte o Enum para String
                aquisicao.getCaminhoComprovante(),
                aquisicao.getCartao().getId(),
                aquisicao.getCartao().getNome()
        );
    }
}