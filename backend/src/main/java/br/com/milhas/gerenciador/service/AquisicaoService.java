package br.com.milhas.gerenciador.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.milhas.gerenciador.dto.AquisicaoCadastroDTO;
import br.com.milhas.gerenciador.dto.AquisicaoResponseDTO;
import br.com.milhas.gerenciador.model.Aquisicao;
import br.com.milhas.gerenciador.model.Cartao;
import br.com.milhas.gerenciador.model.StatusCredito;
import br.com.milhas.gerenciador.repository.AquisicaoRepository;
import br.com.milhas.gerenciador.repository.CartaoRepository;

/**
 * Classe responsável por gerenciar as aquisições de milhas, incluindo registro,
 * cálculo de pontos e listagem.
 */
@Service
public class AquisicaoService {

    @Autowired
    private AquisicaoRepository aquisicaoRepository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Registra uma nova aquisição, calcula pontos, atualiza o saldo do cartão e salva o comprovante.
     * * @param dto Dados da compra (valor, data, cartão, etc).
     * @param comprovante Arquivo (PDF/Imagem) enviado pelo usuário.
     * @param emailUsuarioLogado E-mail do usuário para validação de segurança.
     * @return DTO com os dados da aquisição criada.
     */
    @Transactional // Garante atomicidade: se o upload falhar, o banco faz rollback (desfaz tudo).
    public AquisicaoResponseDTO registrarAquisicao(AquisicaoCadastroDTO dto, MultipartFile comprovante, String emailUsuarioLogado) {

        // 1. Valida se o cartão existe e pertence ao usuário logado
        Cartao cartao = cartaoRepository.findById(dto.cartaoId())
                .orElseThrow(() -> new RuntimeException("Cartão não encontrado"));
        
        if (!cartao.getUsuario().getEmail().equals(emailUsuarioLogado)) {
            throw new RuntimeException("Este cartão não pertence ao usuário logado.");
        }

        // 2. Calcula os pontos automaticamente (Valor Gasto * Fator do Cartão)
        BigDecimal pontosCalculados = dto.valorGasto().multiply(cartao.getFatorConversao());

        // --- ATUALIZAÇÃO CRÍTICA: SOMAR PONTOS AO CARTÃO ---
        // Pega o saldo atual, soma os novos pontos e atualiza a entidade Cartao
        BigDecimal novoSaldo = cartao.getSaldoDePontos().add(pontosCalculados);
        cartao.setSaldoDePontos(novoSaldo);
        
        // Salva o cartão atualizado no banco de dados
        cartaoRepository.save(cartao);
        // ---------------------------------------------------

        // 3. Cria a entidade Aquisicao e salva no banco (1ª vez) para gerar o ID
        Aquisicao novaAquisicao = new Aquisicao();
        novaAquisicao.setDescricao(dto.descricao());
        novaAquisicao.setValorGasto(dto.valorGasto());
        novaAquisicao.setDataCompra(dto.dataCompra());
        novaAquisicao.setDataPrevistaCredito(dto.dataPrevistaCredito());
        novaAquisicao.setCartao(cartao);
        novaAquisicao.setPontosCalculados(pontosCalculados);
        novaAquisicao.setStatus(StatusCredito.PENDENTE);
        
        Aquisicao aquisicaoSalva = aquisicaoRepository.save(novaAquisicao);

        // 4. Salva o arquivo no disco usando o ID gerado para nomear
        String nomeDoArquivo = fileStorageService.storeFile(comprovante, aquisicaoSalva.getId());

        // 5. Atualiza a entidade com o nome do arquivo e salva novamente
        aquisicaoSalva.setCaminhoComprovante(nomeDoArquivo);
        Aquisicao aquisicaoFinal = aquisicaoRepository.save(aquisicaoSalva);

        // 6. Retorna o DTO de resposta
        return new AquisicaoResponseDTO(aquisicaoFinal);
    }

    /**
     * Lista todas as aquisições vinculadas aos cartões do usuário logado.
     * * @param emailUsuarioLogado E-mail para filtrar os dados.
     * @return Lista de DTOs de aquisição.
     */
    public List<AquisicaoResponseDTO> listarPorUsuario(String emailUsuarioLogado) {
        // Usa o método customizado do repositório para buscar pelo e-mail do dono do cartão
        List<Aquisicao> aquisicoes = aquisicaoRepository.findByCartaoUsuarioEmail(emailUsuarioLogado);

        // Converte a lista de Entidades para uma lista de DTOs
        return aquisicoes.stream()
                .map(AquisicaoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void excluir(Long id) {
        Aquisicao aquisicao = aquisicaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada"));

        // 1. Estorna os pontos do cartão (Usando .subtract para BigDecimal)
        Cartao cartao = aquisicao.getCartao();
        cartao.setSaldoDePontos(cartao.getSaldoDePontos().subtract(aquisicao.getPontosCalculados()));
        cartaoRepository.save(cartao);
        // 2. Deleta a compra
        aquisicaoRepository.delete(aquisicao);
    }
}