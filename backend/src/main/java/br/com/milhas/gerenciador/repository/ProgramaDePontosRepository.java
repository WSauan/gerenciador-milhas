package br.com.milhas.gerenciador.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.milhas.gerenciador.model.ProgramaDePontos;

@Repository
public interface ProgramaDePontosRepository extends JpaRepository<ProgramaDePontos, Long> {

    //O Spring cria a consulta para buscar um programa pelo nome
    Optional<ProgramaDePontos> findByNome(String nome);
}