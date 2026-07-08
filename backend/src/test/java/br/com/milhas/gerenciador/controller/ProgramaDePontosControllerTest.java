package br.com.milhas.gerenciador.controller;

import br.com.milhas.gerenciador.model.ProgramaDePontos;
import br.com.milhas.gerenciador.service.ProgramaDePontosService;
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
class ProgramaDePontosControllerTest {

    @Mock
    private ProgramaDePontosService programaService;

    @InjectMocks
    private ProgramaDePontosController programaDePontosController;

    @Test
    @DisplayName("Deve retornar HttpStatus 201 ao cadastrar um programa de pontos válido")
    void deveCadastrarProgramaComSucesso() {
        // Arrange
        ProgramaDePontos programa = new ProgramaDePontos();
        programa.setNome("Livelo");
        when(programaService.cadastrar(any(ProgramaDePontos.class))).thenReturn(programa);

        // Act
        ResponseEntity<ProgramaDePontos> resposta = programaDePontosController.cadastrarPrograma(programa);

        // Assert
        assertNotNull(resposta);
        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertEquals("Livelo", resposta.getBody().getNome());
        verify(programaService, times(1)).cadastrar(programa);
    }

    @Test
    @DisplayName("Deve retornar HttpStatus 400 quando o service lançar exceção no cadastro do programa")
    void deveRetornarBadRequestAoFalharCadastro() {
        // Arrange
        ProgramaDePontos programa = new ProgramaDePontos();
        when(programaService.cadastrar(any(ProgramaDePontos.class))).thenThrow(new RuntimeException("Erro"));

        // Act
        ResponseEntity<ProgramaDePontos> resposta = programaDePontosController.cadastrarPrograma(programa);

        // Assert
        assertNotNull(resposta);
        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertNull(resposta.getBody());
    }

    @Test
    @DisplayName("Deve retornar HttpStatus 200 ao listar todos os programas de pontos")
    void deveListarTodosOsProgramas() {
        // Arrange
        ProgramaDePontos programa = new ProgramaDePontos();
        when(programaService.listarTodos()).thenReturn(Collections.singletonList(programa));

        // Act
        ResponseEntity<List<ProgramaDePontos>> resposta = programaDePontosController.listarProgramas();

        // Assert
        assertNotNull(resposta);
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(1, resposta.getBody().size());
        verify(programaService, times(1)).listarTodos();
    }
}