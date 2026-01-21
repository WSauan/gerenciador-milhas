package br.com.milhas.gerenciador.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.milhas.gerenciador.model.Aquisicao;
// CORREÇÃO: Importando o Enum correto que você mostrou na imagem
import br.com.milhas.gerenciador.model.StatusCredito;

public record AquisicaoResponseDTO(
        Long id,
        String descricao,
        BigDecimal valorGasto,
        BigDecimal pontosCalculados,
        LocalDate dataCompra,
        LocalDate dataCredito,
        String status, 
        String caminhoComprovante,
        Long cartaoId,
        String nomeCartao
) {

    public AquisicaoResponseDTO(Aquisicao aquisicao) {
        this(
                aquisicao.getId(),
                aquisicao.getDescricao(),
                aquisicao.getValorGasto(),
                aquisicao.getPontosCalculados(),
                aquisicao.getDataCompra(),
                aquisicao.getDataPrevistaCredito(),
                calcularStatusDinamico(aquisicao), 
                aquisicao.getCaminhoComprovante(),
                aquisicao.getCartao() != null ? aquisicao.getCartao().getId() : null,
                aquisicao.getCartao() != null ? aquisicao.getCartao().getNome() : "Cartão Excluído"
        );
    }

    // --- LÓGICA DE NEGÓCIO AUTOMÁTICA ---
    private static String calcularStatusDinamico(Aquisicao a) {
        // Se o status for nulo, retorna PENDENTE por segurança
        if (a.getStatus() == null) return "PENDENTE";

        // 1. Se já foi cancelado, mantém.
        if (a.getStatus() == StatusCredito.CANCELADO) {
            return "CANCELADO";
        }
        
        // Se já está como CREDITADO no banco, retorna APROVADO para ficar verde no site
        if (a.getStatus() == StatusCredito.CREDITADO) {
            return "APROVADO";
        }

        // 2. Se está PENDENTE, vamos verificar a data.
        if (a.getStatus() == StatusCredito.PENDENTE) {
            LocalDate hoje = LocalDate.now();
            LocalDate dataPrevisao = a.getDataPrevistaCredito();

            // Se a data de previsão existe E (é hoje OU já passou)
            // O sistema "engana" o site dizendo que está APROVADO visualmente
            if (dataPrevisao != null && !dataPrevisao.isAfter(hoje)) {
                return "APROVADO"; 
            }
        }

        // 3. Caso contrário, retorna o nome original (PENDENTE, ATRASADO, etc.)
        return a.getStatus().name();
    }
}