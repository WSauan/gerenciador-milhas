package br.com.milhas.gerenciador.config;

import java.util.Arrays;
import java.util.List;

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
            // --- CARGA DE BANDEIRAS ---
            if (bandeiraRepository.count() == 0) {
                List<String> bandeiras = Arrays.asList(
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

                for (String nome : bandeiras) {
                    salvarBandeira(bandeiraRepository, nome);
                }
                System.out.println("✅ Bandeiras carregadas com sucesso!");
            }

            // --- CARGA DE PROGRAMAS DE PONTOS ---
            if (programaRepository.count() == 0) {
                List<String> programas = Arrays.asList(
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

                for (String nome : programas) {
                    salvarPrograma(programaRepository, nome);
                }
                System.out.println("✅ Programas de Pontos carregados com sucesso!");
            }
        };
    }

    // --- MÉTODOS AUXILIARES PARA EVITAR O ERRO DE CONSTRUTOR ---

    private void salvarBandeira(BandeiraRepository repo, String nome) {
        Bandeira b = new Bandeira();
        b.setNome(nome);
        repo.save(b);
    }

    private void salvarPrograma(ProgramaDePontosRepository repo, String nome) {
        ProgramaDePontos p = new ProgramaDePontos();
        p.setNome(nome); // Se sua classe usar setNomePrograma, ajuste aqui
        repo.save(p);
    }
}