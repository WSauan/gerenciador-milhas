package br.com.milhas.gerenciador.service;

import br.com.milhas.gerenciador.model.Bandeira;
import br.com.milhas.gerenciador.repository.BandeiraRepository;
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
class BandeiraServiceTest {

    @Mock
    private BandeiraRepository bandeiraRepository;

    @InjectMocks
    private BandeiraService bandeiraService;

    @Test
    @DisplayName("Deve cadastrar uma bandeira com sucesso quando o nome for inédito")
    void deveCadastrarBandeiraComSucesso() {
        // Arrange
        Bandeira bandeira = new Bandeira();
        bandeira.setNome("Visa");
        
        when(bandeiraRepository.findByNome("Visa")).thenReturn(Optional.empty());
        when(bandeiraRepository.save(bandeira)).thenReturn(bandeira);

        // Act
        Bandeira cadastrada = bandeiraService.cadastrar(bandeira);

        // Assert
        assertNotNull(cadastrada);
        assertEquals("Visa", cadastrada.getNome());
        verify(bandeiraRepository, times(1)).save(bandeira);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cadastrar bandeira com nome duplicado")
    void deveLancarExcecaoAoCadastrarBandeiraDuplicada() {
        // Arrange
        Bandeira bandeira = new Bandeira();
        bandeira.setNome("Mastercard");
        
        when(bandeiraRepository.findByNome("Mastercard")).thenReturn(Optional.of(bandeira));

        // Act & Assert
        RuntimeException excecao = assertThrows(RuntimeException.class, () -> {
            bandeiraService.cadastrar(bandeira);
        });

        assertEquals("Bandeira com o nome 'Mastercard' já existe.", excecao.getMessage());
        verify(bandeiraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar todas as bandeiras cadastradas com sucesso")
    void deveListarTodasAsBandeiras() {
        // Arrange
        Bandeira bandeira = new Bandeira();
        bandeira.setNome("Elo");
        when(bandeiraRepository.findAll()).thenReturn(Collections.singletonList(bandeira));

        // Act
        List<Bandeira> resultado = bandeiraService.listarTodas();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Elo", resultado.get(0).getNome());
        verify(bandeiraRepository, times(1)).findAll();
    }
}