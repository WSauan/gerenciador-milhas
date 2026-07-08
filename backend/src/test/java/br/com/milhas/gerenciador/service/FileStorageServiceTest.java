package br.com.milhas.gerenciador.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path pastaTemporaria;

    @BeforeEach
    void setUp() {
        // Inicializa apontando para o diretório temporário isolado de testes
        fileStorageService = new FileStorageService(pastaTemporaria.toString());
    }

    @Test
    @DisplayName("Deve salvar arquivo com sucesso padronizando a extensão original")
    void deveSalvarArquivoComSucesso() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("comprovante.pdf");
        when(mockFile.getInputStream()).thenReturn(InputStream.nullInputStream());

        // Act
        String novoNome = fileStorageService.storeFile(mockFile, 50L);

        // Assert
        assertEquals("comprovante_50.pdf", novoNome);
    }

    @Test
    @DisplayName("Deve adotar extensão padrão .jpg quando o arquivo original não possuir extensão")
    void deveAdotarJpgQuandoNaoHouverExtensao() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("imagem-sem-extensao");
        when(mockFile.getInputStream()).thenReturn(InputStream.nullInputStream());

        // Act
        String novoNome = fileStorageService.storeFile(mockFile, 60L);

        // Assert
        assertEquals("comprovante_60.jpg", novoNome);
    }

    @Test
    @DisplayName("Deve lançar exceção customizada quando ocorrer erro de I/O na cópia do arquivo")
    void deveLancarExcecaoAoFalharEscrita() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("comprovante.png");
        // Força uma exceção de leitura na stream para ativar o bloco catch de escrita
        when(mockFile.getInputStream()).thenThrow(new IOException("Falha simulada no disco"));

        // Act & Assert
        RuntimeException excecao = assertThrows(RuntimeException.class, () -> {
            fileStorageService.storeFile(mockFile, 70L);
        });

        assertTrue(excecao.getMessage().contains("Não foi possível salvar o arquivo"));
    }

    @Test
    @DisplayName("Deve lançar exceção customizada ao tentar inicializar o serviço com um caminho inválido")
    void deveLancarExcecaoAoInicializarCaminhoInvalido() {
        // Envia um caractere nulo ou inválido para o sistema operacional falhar na criação do diretório
        String caminhoInvalido = "\0";

        assertThrows(RuntimeException.class, () -> {
            new FileStorageService(caminhoInvalido);
        });
    }
}