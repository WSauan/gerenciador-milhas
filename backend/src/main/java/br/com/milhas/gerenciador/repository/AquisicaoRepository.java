package br.com.milhas.gerenciador.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.milhas.gerenciador.model.Aquisicao;

@Repository
public interface AquisicaoRepository extends JpaRepository<Aquisicao, Long> {

    /** * Busca todas as aquisições vinculadas aos cartões de um usuário (por e-mail). 
     */
    List<Aquisicao> findByCartaoUsuarioEmail(String email);

    /**
     * OBRIGATÓRIO PARA O DELETE EM CASCATA:
     * Apaga todas as aquisições de um cartão específico.
     */
    void deleteByCartaoId(Long cartaoId);

    /**
     * Calcula a média de dias entre a data da compra e a data prevista de crédito.
     */
    @Query(value = "SELECT AVG(a.data_prevista_credito - a.data_compra) " +
                   "FROM aquisicoes a " +
                   "JOIN cartoes c ON a.cartao_id = c.id " +
                   "JOIN usuarios u ON c.usuario_id = u.id " +
                   "WHERE u.email = :emailUsuario",
           nativeQuery = true)
    BigDecimal findPrazoMedioRecebimentoPorUsuario(@Param("emailUsuario") String emailUsuario);
}