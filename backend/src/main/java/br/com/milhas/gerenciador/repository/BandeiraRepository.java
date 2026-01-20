package br.com.milhas.gerenciador.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.milhas.gerenciador.model.Bandeira;

@Repository
public interface BandeiraRepository extends JpaRepository<Bandeira, Long> {

    // O Spring cria a consulta para buscar uma bandeira pelo nome
    Optional<Bandeira> findByNome(String nome);
}