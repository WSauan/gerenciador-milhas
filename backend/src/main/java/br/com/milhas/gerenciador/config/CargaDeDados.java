package br.com.milhas.gerenciador.config;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.milhas.gerenciador.model.Bandeira;
import br.com.milhas.gerenciador.model.ProgramaDePontos;
import br.com.milhas.gerenciador.repository.BandeiraRepository;
import br.com.milhas.gerenciador.repository.ProgramaDePontosRepository;

@Configuration
public class CargaDeDados {

    @Bean
    public CommandLineRunner carregarDadosIniciais(
            BandeiraRepository bandeiraRepository,
            ProgramaDePontosRepository programaRepository) {
        
        return args -> {
            // =================================================================================
            // --- CARGA DE BANDEIRAS ---
            // =================================================================================
            List<String> bandeirasParaCadastrar = Arrays.asList(
                "Visa",
                "Mastercard",
                "Elo",
                "American Express",
                "Hipercard",
                "Diners Club",
                "Discover",
                "JCB",
                "Aura",
                "Cabal",
                "Sorocred",
                "Mais!",
                "UnionPay",
                "Outros"
            );

            // 1. Busca todas as bandeiras que JÁ existem no banco para evitar duplicidade
            Set<String> bandeirasExistentes = bandeiraRepository.findAll().stream()
                .map(Bandeira::getNome)
                .collect(Collectors.toSet());

            // 2. Percorre a lista desejada e só salva o que não existe
            for (String nome : bandeirasParaCadastrar) {
                if (!bandeirasExistentes.contains(nome)) {
                    salvarBandeira(bandeiraRepository, nome);
                    System.out.println("➕ Nova Bandeira adicionada: " + nome);
                }
            }
            System.out.println("✅ Verificação de Bandeiras concluída!");

            // =================================================================================
            // --- CARGA DE PROGRAMAS DE PONTOS ---
            // =================================================================================
            List<String> programasParaCadastrar = Arrays.asList(
                "Livelo (Bradesco/BB)",
                "Esfera (Santander)",
                "Smiles (GOL)",
                "Latam Pass (LATAM)",
                "TudoAzul (Azul)",
                "Átomos (C6 Bank)",
                "Nubank Rewards / Ultravioleta",
                "Inter Loop (Banco Inter)",
                "Pontos Caixa",
                "Curtaí (BRB)",
                "Coopera (Sicoob)",
                "Membership Rewards (Amex)",
                "Iupp / Itaú Shop",
                "PDA (Pão de Açúcar)",
                "Dotz",
                "Km de Vantagens",
                "Outros"
            );

            // 1. Busca todos os programas que JÁ existem
            Set<String> programasExistentes = programaRepository.findAll().stream()
                .map(ProgramaDePontos::getNome)
                .collect(Collectors.toSet());

            // 2. Salva apenas os novos
            for (String nome : programasParaCadastrar) {
                if (!programasExistentes.contains(nome)) {
                    salvarPrograma(programaRepository, nome);
                    System.out.println("➕ Novo Programa adicionado: " + nome);
                }
            }
            System.out.println("✅ Verificação de Programas concluída!");
        };
    }

    private void salvarBandeira(BandeiraRepository repo, String nome) {
        Bandeira b = new Bandeira();
        b.setNome(nome);
        repo.save(b);
    }

    private void salvarPrograma(ProgramaDePontosRepository repo, String nome) {
        ProgramaDePontos p = new ProgramaDePontos();
        p.setNome(nome);
        repo.save(p);
    }
}