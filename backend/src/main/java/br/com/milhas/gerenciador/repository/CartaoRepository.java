package br.com.milhas.gerenciador.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.milhas.gerenciador.dto.PontosPorCartaoDTO;
import br.com.milhas.gerenciador.model.Cartao;

@Repository
public interface CartaoRepository extends JpaRepository<Cartao, Long> {

    List<Cartao> findByUsuarioId(Long usuarioId);
    /**
     * Busca de forma otimizada os dados para o relatório de pontos por cartão,
     * filtrando pelo e-mail do usuário e já retornando o DTO.
     * @param emailUsuario O e-mail do usuário logado.
     * @return Uma lista de PontosPorCartaoDTO.
     */
    @Query("SELECT new br.com.milhas.gerenciador.dto.PontosPorCartaoDTO(c.nome, c.saldoDePontos) " +
           "FROM Cartao c " +
           "WHERE c.usuario.email = :emailUsuario")
    List<PontosPorCartaoDTO> findPontosPorCartaoByUsuarioEmail(@Param("emailUsuario") String emailUsuario);
}