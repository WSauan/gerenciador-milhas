package br.com.milhas.gerenciador.controller;

import br.com.milhas.gerenciador.dto.AquisicaoResponseDTO;
import br.com.milhas.gerenciador.service.AquisicaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AquisicaoControllerTest {

    @Mock
    private AquisicaoService aquisicaoService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AquisicaoController aquisicaoController;

    @Test
    @DisplayName("Deve registrar aquisição com sucesso e retornar 201 Created")
    void deveRegistrarAquisicaoComSucesso() throws Exception {
        String jsonDto = "{\"descricao\":\"Compra\",\"valorGasto\":100.0,\"dataCompra\":\"2026-07-08\",\"dataPrevistaCredito\":\"2026-08-08\",\"cartaoId\":1}";
        MockMultipartFile arquivo = new MockMultipartFile("comprovante", "teste.pdf", "application/pdf", new byte[]{1});
        AquisicaoResponseDTO responseDTO = new AquisicaoResponseDTO(1L, "Compra", BigDecimal.TEN, BigDecimal.TEN, LocalDate.now(), LocalDate.now(), "APROVADO", "caminho/", 1L, "Visa");

        when(authentication.getName()).thenReturn("welber@email.com");
        when(aquisicaoService.registrarAquisicao(any(), any(MultipartFile.class), eq("welber@email.com"))).thenReturn(responseDTO);

        ResponseEntity<AquisicaoResponseDTO> resposta = aquisicaoController.registrarAquisicao(jsonDto, arquivo, authentication);

        assertNotNull(resposta);
        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao falhar ou receber JSON inválido")
    void deveRetornarBadRequestAoFalharRegistro() {
        MockMultipartFile arquivo = new MockMultipartFile("comprovante", new byte[]{0});
        ResponseEntity<AquisicaoResponseDTO> resposta = aquisicaoController.registrarAquisicao("json_invalido", arquivo, authentication);

        assertNotNull(resposta);
        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
    }

    @Test
    @DisplayName("Deve listar aquisições do usuário e retornar 200 OK")
    void deveListarAquisicoes() {
        when(authentication.getName()).thenReturn("welber@email.com");
        when(aquisicaoService.listarPorUsuario("welber@email.com")).thenReturn(Collections.emptyList());

        ResponseEntity<List<AquisicaoResponseDTO>> resposta = aquisicaoController.listarAquisicoes(authentication);

        assertNotNull(resposta);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
    }

    @Test
    @DisplayName("Deve excluir uma aquisição com sucesso e retornar 204 No Content")
    void deveExcluirAquisicao() {
        doNothing().when(aquisicaoService).excluir(1L);

        ResponseEntity<Void> resposta = aquisicaoController.excluir(1L);

        assertNotNull(resposta);
        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        verify(aquisicaoService, times(1)).excluir(1L);
    }
}