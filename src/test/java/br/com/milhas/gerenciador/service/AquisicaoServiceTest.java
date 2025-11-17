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

@ExtendWith(MockitoExtension.class) // Habilita o Mockito para este teste
class AquisicaoServiceTest {

    @Mock // Cria uma "mentira" (mock) do repositório
    private CartaoRepository cartaoRepository;

    @Mock // Cria um mock do repositório de aquisição
    private AquisicaoRepository aquisicaoRepository;

    @Mock // Cria um mock do serviço de arquivos
    private FileStorageService fileStorageService;

    @InjectMocks // Injeta os mocks acima dentro do nosso Service real
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

        // Criando o cartão Fake com o fator de conversão
        Cartao cartaoFake = new Cartao();
        cartaoFake.setId(cartaoId);
        cartaoFake.setUsuario(usuarioFake);
        cartaoFake.setFatorConversao(fatorConversao);

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

        // 2. COMPORTAMENTO DOS MOCKS (ENSINANDO O TESTE A MENTIR)
        
        // Quando o serviço buscar o cartão, retorne o cartaoFake
        when(cartaoRepository.findById(cartaoId)).thenReturn(Optional.of(cartaoFake));
        
        // Quando o serviço tentar salvar a aquisição, retorne uma aquisição com ID 1
        when(aquisicaoRepository.save(any(Aquisicao.class))).thenAnswer(invocation -> {
            Aquisicao a = invocation.getArgument(0);
            a.setId(1L); // Simula o ID gerado pelo banco
            return a;
        });

        // Quando tentar salvar arquivo, retorne um nome qualquer
        when(fileStorageService.storeFile(any(), any())).thenReturn("arquivo-salvo.pdf");

        // 3. EXECUÇÃO (CHAMA O MÉTODO REAL)
        AquisicaoResponseDTO resultado = aquisicaoService.registrarAquisicao(dto, arquivoFake, emailUsuario);

        // 4. VERIFICAÇÃO (ASSERTS)
        assertNotNull(resultado);
        
        // A conta deve ser: 100.00 * 2.0 = 200.00
        BigDecimal pontosEsperados = new BigDecimal("200.00");
        
        assertEquals(0, resultado.pontosCalculados().compareTo(pontosEsperados),        "Os pontos calculados devem ser iguais ao valor gasto multiplicado pelo fator do cartão");    }
}