package br.com.milhas.gerenciador.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.milhas.gerenciador.dto.CartaoCadastroDTO;
import br.com.milhas.gerenciador.dto.CartaoResponseDTO;
import br.com.milhas.gerenciador.model.Bandeira;
import br.com.milhas.gerenciador.model.Cartao;
import br.com.milhas.gerenciador.model.ProgramaDePontos;
import br.com.milhas.gerenciador.model.Usuario;
import br.com.milhas.gerenciador.repository.AquisicaoRepository; // IMPORTANTE
import br.com.milhas.gerenciador.repository.BandeiraRepository;
import br.com.milhas.gerenciador.repository.CartaoRepository;
import br.com.milhas.gerenciador.repository.ProgramaDePontosRepository;
import br.com.milhas.gerenciador.repository.UsuarioRepository;

@Service
public class CartaoService {

    @Autowired
    private CartaoRepository cartaoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private BandeiraRepository bandeiraRepository;
    @Autowired
    private ProgramaDePontosRepository programaRepository;
    
    @Autowired
    private AquisicaoRepository aquisicaoRepository; // --- INJEÇÃO NOVA (Necessária para a limpeza) ---

    public CartaoResponseDTO cadastrar(CartaoCadastroDTO dto, String emailUsuarioLogado) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Bandeira bandeira = bandeiraRepository.findById(dto.bandeiraId())
                .orElseThrow(() -> new RuntimeException("Bandeira não encontrada"));

        ProgramaDePontos programa = programaRepository.findById(dto.programaId())
                .orElseThrow(() -> new RuntimeException("Programa de pontos não encontrado"));

        Cartao novoCartao = new Cartao();
        novoCartao.setNome(dto.nome());
        novoCartao.setSaldoDePontos(dto.saldoDePontos());
        novoCartao.setFatorConversao(dto.fatorConversao());
        novoCartao.setUsuario(usuario);
        novoCartao.setBandeira(bandeira);
        novoCartao.setProgramaDePontos(programa);

        Cartao cartaoSalvo = cartaoRepository.save(novoCartao);
        return new CartaoResponseDTO(cartaoSalvo);
    }

    public List<CartaoResponseDTO> listarPorUsuario(String emailUsuarioLogado) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Cartao> cartoesDoUsuario = cartaoRepository.findByUsuarioId(usuario.getId());

        return cartoesDoUsuario.stream()
                .map(CartaoResponseDTO::new)
                .collect(Collectors.toList());
    }

    // --- LÓGICA DE EXCLUSÃO CORRIGIDA (CASCATA) ---
    @Transactional // Garante que apaga tudo ou nada
    public void excluir(Long id) {
        if (!cartaoRepository.existsById(id)) {
            throw new RuntimeException("Cartão não encontrado");
        }
        
        // 1. LIMPEZA: Apaga o histórico de compras desse cartão primeiro
        // (Isso evita o erro de DataIntegrityViolationException)
        aquisicaoRepository.deleteByCartaoId(id);

        // 2. EXCLUSÃO: Apaga o cartão (agora que ele não tem mais compras presas a ele)
        cartaoRepository.deleteById(id);
    }
}