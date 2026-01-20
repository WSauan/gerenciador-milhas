package br.com.milhas.gerenciador.model;
// Classe responsável por representar um cartão de crédito cadastrado pelo usuário.
import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Representa um cartão de crédito cadastrado pelo usuário.
 * Armazena o saldo de pontos e o fator de conversão para cálculos.
 */
@Entity
@Table(name = "cartoes")
@Getter
@Setter
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private BigDecimal saldoDePontos;
    /**
     * Fator multiplicador de pontos (ex: 2.5 pontos por dólar/real).
     * Essencial para o cálculo automático de aquisições.
     */
    @Column(nullable = false)
    private BigDecimal fatorConversao;

    // --- RELACIONAMENTOS ---

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "bandeira_id", nullable = false)
    private Bandeira bandeira;

    @ManyToOne
    @JoinColumn(name = "programa_id", nullable = false)
    private ProgramaDePontos programaDePontos;

    @OneToMany(mappedBy = "cartao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Aquisicao> aquisicoes;
}