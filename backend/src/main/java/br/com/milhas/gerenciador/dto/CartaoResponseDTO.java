package br.com.milhas.gerenciador.dto;
// Classe responsável por representar os dados de resposta de um cartão.
import java.math.BigDecimal;

import br.com.milhas.gerenciador.model.Cartao;

public record CartaoResponseDTO(
        Long id,
        String nome,
        BigDecimal saldoDePontos,
        BigDecimal fatorConversao,
        String nomeBandeira,
        String nomePrograma
) {
    
    // Construtor para converter a Entidade Cartao neste DTO
    public CartaoResponseDTO(Cartao cartao) {
        this(
                cartao.getId(),
                cartao.getNome(),
                cartao.getSaldoDePontos(),
                cartao.getFatorConversao(),
                cartao.getBandeira().getNome(),
                cartao.getProgramaDePontos().getNome()
        );
    }
}