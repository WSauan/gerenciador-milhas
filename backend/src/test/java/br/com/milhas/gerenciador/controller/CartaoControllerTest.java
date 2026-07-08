package br.com.milhas.gerenciador.controller;

import br.com.milhas.gerenciador.dto.CartaoCadastroDTO;
import br.com.milhas.gerenciador.dto.CartaoResponseDTO;
import br.com.milhas.gerenciador.service.CartaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartaoControllerTest {

    @Mock
    private CartaoService cartaoService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CartaoController cartaoController;

    @Test
    @DisplayName("Deve cadastrar cartão com sucesso e retornar 201 Created")
    void deveCadastrarCartaoComSucesso() {
        // Arrange com os 5 parâmetros reais do construtor
        CartaoCadastroDTO dto = new CartaoCadastroDTO("Visa Gold", BigDecimal.ZERO, new BigDecimal("2.0"), 1L, 1L);
        
        // Arrange com os 6 parâmetros reais do construtor de resposta
        CartaoResponseDTO responseDTO = new CartaoResponseDTO(1L, "Visa Gold", BigDecimal.ZERO, new BigDecimal("2.0"), "Visa", "Livelo");
        
        when(authentication.getName()).thenReturn("welber@email.com");
        when(cartaoService.cadastrar(dto, "welber@email.com")).thenReturn(responseDTO);

        // Act
        ResponseEntity<CartaoResponseDTO> resposta = cartaoController.cadastrarCartao(dto, authentication);

        // Assert
        assertNotNull(resposta);
        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertEquals("Visa Gold", resposta.getBody().nome());
        verify(cartaoService, times(1)).cadastrar(dto, "welber@email.com");
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request se houver erro ao cadastrar cartão")
    void deveRetornarBadRequestNoCadastro() {
        // Arrange com os 5 parâmetros reais
        CartaoCadastroDTO dto = new CartaoCadastroDTO("Visa Gold", BigDecimal.ZERO, new BigDecimal("2.0"), 1L, 1L);
        
        when(authentication.getName()).thenReturn("welber@email.com");
        when(cartaoService.cadastrar(dto, "welber@email.com")).thenThrow(new RuntimeException("Erro"));

        // Act
        ResponseEntity<CartaoResponseDTO> resposta = cartaoController.cadastrarCartao(dto, authentication);

        // Assert
        assertNotNull(resposta);
        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertNull(resposta.getBody());
    }

    @Test
    @DisplayName("Deve listar cartões do usuário e retornar 200 OK")
    void deveListarCartoesDoUsuario() {
        // Arrange com os 6 parâmetros reais
        CartaoResponseDTO responseDTO = new CartaoResponseDTO(1L, "Visa Gold", BigDecimal.ZERO, new BigDecimal("2.0"), "Visa", "Livelo");
        
        when(authentication.getName()).thenReturn("welber@email.com");
        when(cartaoService.listarPorUsuario("welber@email.com")).thenReturn(Collections.singletonList(responseDTO));

        // Act
        ResponseEntity<List<CartaoResponseDTO>> resposta = cartaoController.listarCartoesDoUsuario(authentication);

        // Assert
        assertNotNull(resposta);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(1, resposta.getBody().size());
        assertEquals("Visa Gold", resposta.getBody().get(0).nome());
        verify(cartaoService, times(1)).listarPorUsuario("welber@email.com");
    }

    @Test
    @DisplayName("Deve excluir cartão com sucesso e retornar 204 No Content")
    void deveExcluirCartaoComSucesso() {
        // Arrange
        doNothing().when(cartaoService).excluir(1L);

        // Act
        ResponseEntity<Void> resposta = cartaoController.excluirCartao(1L);

        // Assert
        assertNotNull(resposta);
        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        verify(cartaoService, times(1)).excluir(1L);
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found se o cartão não existir na exclusão")
    void deveRetornarNotFoundNaExclusao() {
        // Arrange
        doThrow(new RuntimeException("Não encontrado")).when(cartaoService).excluir(99L);

        // Act
        ResponseEntity<Void> resposta = cartaoController.excluirCartao(99L);

        // Assert
        assertNotNull(resposta);
        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        verify(cartaoService, times(1)).excluir(99L);
    }
}