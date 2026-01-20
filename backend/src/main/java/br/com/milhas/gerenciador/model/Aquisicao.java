package br.com.milhas.gerenciador.model;
// Classe responsável por representar uma compra ou transação que gera pontos.
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Representa uma compra ou transação que gera pontos.
 * Vincula um comprovante (arquivo) e armazena os pontos calculados.
 */
@Entity
@Table(name = "aquisicoes")
@Getter
@Setter
public class Aquisicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descricao; // Ex: "Compra na Amazon"

    @Column(nullable = false)
    private BigDecimal valorGasto; // Valor da compra

    @Column(nullable = false)
    private BigDecimal pontosCalculados; // Pontos a serem recebidos

    @Column(nullable = false)
    private LocalDate dataCompra;

    @Column(nullable = false)
    private LocalDate dataPrevistaCredito; // Para "quanto tempo falta"

    @Enumerated(EnumType.STRING) // Salva o nome do enum (ex: "PENDENTE") no banco
    @Column(nullable = false)
    private StatusCredito status;

    private String caminhoComprovante; // Caminho/nome do arquivo de upload

    @ManyToOne
    @JoinColumn(name = "cartao_id", nullable = false)
    private Cartao cartao;
}