package br.com.milhas.gerenciador.controller;

import br.com.milhas.gerenciador.model.Bandeira;
import br.com.milhas.gerenciador.service.BandeiraService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BandeiraControllerTest {

    @Mock
    private BandeiraService bandeiraService;

    @InjectMocks
    private BandeiraController bandeiraController;

    @Test
    @DisplayName("Deve retornar HttpStatus 201 ao cadastrar uma bandeira válida")
    void deveCadastrarBandeiraComSucesso() {
        // Arrange
        Bandeira bandeira = new Bandeira();
        bandeira.setNome("Visa");
        when(bandeiraService.cadastrar(any(Bandeira.class))).thenReturn(bandeira);

        // Act
        ResponseEntity<Bandeira> resposta = bandeiraController.cadastrarBandeira(bandeira);

        // Assert
        assertNotNull(resposta);
        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertEquals("Visa", resposta.getBody().getNome());
        verify(bandeiraService, times(1)).cadastrar(bandeira);
    }

    @Test
    @DisplayName("Deve retornar HttpStatus 400 quando o service lançar exceção no cadastro")
    void deveRetornarBadRequestAoFalharCadastro() {
        // Arrange
        Bandeira bandeira = new Bandeira();
        when(bandeiraService.cadastrar(any(Bandeira.class))).thenThrow(new RuntimeException("Erro"));

        // Act
        ResponseEntity<Bandeira> resposta = bandeiraController.cadastrarBandeira(bandeira);

        // Assert
        assertNotNull(resposta);
        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertNull(resposta.getBody());
    }

    @Test
    @DisplayName("Deve retornar HttpStatus 200 ao listar todas as bandeiras")
    void deveListarTodasAsBandeiras() {
        // Arrange
        Bandeira bandeira = new Bandeira();
        when(bandeiraService.listarTodas()).thenReturn(Collections.singletonList(bandeira));

        // Act
        ResponseEntity<List<Bandeira>> resposta = bandeiraController.listarBandeiras();

        // Assert
        assertNotNull(resposta);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(1, resposta.getBody().size());
        verify(bandeiraService, times(1)).listarTodas();
    }
}