package br.com.milhas.gerenciador.service;

import br.com.milhas.gerenciador.model.ProgramaDePontos;
import br.com.milhas.gerenciador.repository.ProgramaDePontosRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgramaDePontosServiceTest {

    @Mock
    private ProgramaDePontosRepository programaRepository;

    @InjectMocks
    private ProgramaDePontosService programaDePontosService;

    @Test
    @DisplayName("Deve cadastrar um programa de pontos com sucesso")
    void deveCadastrarProgramaComSucesso() {
        // Arrange
        ProgramaDePontos programa = new ProgramaDePontos();
        programa.setNome("Livelo");
        
        when(programaRepository.findByNome("Livelo")).thenReturn(Optional.empty());
        when(programaRepository.save(programa)).thenReturn(programa);

        // Act
        ProgramaDePontos cadastrado = programaDePontosService.cadastrar(programa);

        // Assert
        assertNotNull(cadastrado);
        assertEquals("Livelo", cadastrado.getNome());
        verify(programaRepository, times(1)).save(programa);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar programa com nome duplicado")
    void deveLancarExcecaoAoCadastrarProgramaDuplicado() {
        // Arrange
        ProgramaDePontos programa = new ProgramaDePontos();
        programa.setNome("Esfera");
        
        when(programaRepository.findByNome("Esfera")).thenReturn(Optional.of(programa));

        // Act & Assert
        RuntimeException excecao = assertThrows(RuntimeException.class, () -> {
            programaDePontosService.cadastrar(programa);
        });

        assertEquals("Programa de pontos com o nome 'Esfera' já existe.", excecao.getMessage());
        verify(programaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar todos os programas cadastrados com sucesso")
    void deveListarTodosOsProgramas() {
        // Arrange
        ProgramaDePontos programa = new ProgramaDePontos();
        programa.setNome("Smiles");
        when(programaRepository.findAll()).thenReturn(Collections.singletonList(programa));

        // Act
        List<ProgramaDePontos> resultado = programaDePontosService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Smiles", resultado.get(0).getNome());
        verify(programaRepository, times(1)).findAll();
    }
}