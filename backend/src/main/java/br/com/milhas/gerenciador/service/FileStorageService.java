package br.com.milhas.gerenciador.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    // Construtor: Cria a pasta de upload se ela não existir
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Não foi possível criar o diretório de uploads.", ex);
        }
    }

    /**
     * Salva o arquivo mantendo a extensão original (.pdf, .png, etc)
     */
    public String storeFile(MultipartFile file, Long aquisicaoId) {
        try {
            // 1. Obtém o nome original para extrair a extensão
            String nomeOriginal = Objects.requireNonNull(file.getOriginalFilename());
            String extensao = "";

            if (nomeOriginal.contains(".")) {
                extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
            } else {
                // Se o arquivo vier sem extensão, define .jpg como padrão
                extensao = ".jpg";
            }

            // 2. Cria um nome padronizado: comprovante_ID.extensao
            // Exemplo: comprovante_15.pdf ou comprovante_15.png
            String novoNomeArquivo = "comprovante_" + aquisicaoId + extensao;

            // 3. Define o caminho de destino
            Path targetLocation = this.fileStorageLocation.resolve(novoNomeArquivo);

            // 4. Salva o arquivo (REPLACE_EXISTING substitui se já houver um antigo)
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return novoNomeArquivo;

        } catch (IOException ex) {
            throw new RuntimeException("Não foi possível salvar o arquivo da aquisição " + aquisicaoId, ex);
        }
    }
}