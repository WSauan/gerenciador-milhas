package br.com.milhas.gerenciador.service;

import br.com.milhas.gerenciador.dto.AquisicaoCadastroDTO;
import br.com.milhas.gerenciador.dto.AquisicaoResponseDTO;
import br.com.milhas.gerenciador.model.Aquisicao;
import br.com.milhas.gerenciador.model.Cartao;
import br.com.milhas.gerenciador.model.Usuario;
import br.com.milhas.gerenciador.repository.AquisicaoRepository;
import br.com.milhas.gerenciador.repository.CartaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AquisicaoServiceTest {

    @Mock
    private CartaoRepository cartaoRepository;

    @Mock
    private AquisicaoRepository aquisicaoRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private AquisicaoService aquisicaoService;

    @Test
    @DisplayName("Deve calcular os pontos corretamente ao registrar aquisição")
    void deveCalcularPontosCorretamente() {
        // 1. PREPARAÇÃO (CENÁRIO)
        String emailUsuario = "teste@email.com";
        Long cartaoId = 1L;
        BigDecimal valorGasto = new BigDecimal("100.00");
        BigDecimal fatorConversao = new BigDecimal("2.0"); // 2 pontos por real

        // Criando o usuário Fake
        Usuario usuarioFake = new Usuario();
        usuarioFake.setEmail(emailUsuario);

        // Criando o cartão Fake
        Cartao cartaoFake = new Cartao();
        cartaoFake.setId(cartaoId);
        cartaoFake.setUsuario(usuarioFake);
        cartaoFake.setFatorConversao(fatorConversao);
        
        // --- CORREÇÃO: Inicializar o saldo com ZERO para não dar erro ---
        cartaoFake.setSaldoDePontos(BigDecimal.ZERO);
        // ----------------------------------------------------------------

        // Criando o DTO de entrada
        AquisicaoCadastroDTO dto = new AquisicaoCadastroDTO(
                "Compra Teste",
                valorGasto,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                cartaoId
        );

        // Simulando o arquivo de upload
        MockMultipartFile arquivoFake = new MockMultipartFile("comprovante", "teste.pdf", "application/pdf", "bytes".getBytes());

        // 2. COMPORTAMENTO DOS MOCKS
        when(cartaoRepository.findById(cartaoId)).thenReturn(Optional.of(cartaoFake));
        
        // Mockamos o save do cartão para evitar problemas, já que o service agora salva o saldo
        when(cartaoRepository.save(any(Cartao.class))).thenAnswer(i -> i.getArguments()[0]);

        when(aquisicaoRepository.save(any(Aquisicao.class))).thenAnswer(invocation -> {
            Aquisicao a = invocation.getArgument(0);
            a.setId(1L);
            return a;
        });

        when(fileStorageService.storeFile(any(), any())).thenReturn("arquivo-salvo.pdf");

        // 3. EXECUÇÃO
        AquisicaoResponseDTO resultado = aquisicaoService.registrarAquisicao(dto, arquivoFake, emailUsuario);

        // 4. VERIFICAÇÃO
        assertNotNull(resultado);
        
        BigDecimal pontosEsperados = new BigDecimal("200.00");
        
        // Usamos compareTo para comparar BigDecimal (evita erros de precisão decimal)
        assertEquals(0, resultado.pontosCalculados().compareTo(pontosEsperados));
    }
}