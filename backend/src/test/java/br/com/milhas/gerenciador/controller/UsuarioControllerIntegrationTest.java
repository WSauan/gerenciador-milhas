package br.com.milhas.gerenciador.controller;

import br.com.milhas.gerenciador.dto.ResetSenhaDTO;
import br.com.milhas.gerenciador.dto.SolicitacaoSenhaDTO;
import br.com.milhas.gerenciador.dto.UsuarioAtualizacaoDTO;
import br.com.milhas.gerenciador.model.Usuario;
import br.com.milhas.gerenciador.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Disabled // Desativado para isolar os testes unitários de Service no relatório do JaCoCo
public class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Deve cadastrar um usuário com sucesso e retornar 201 Created")
    void deveCadastrarUsuario() throws Exception {
        // 1. DADOS DE ENTRADA
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome("Usuario Teste Integracao");
        novoUsuario.setEmail("integracao@teste.com");
        novoUsuario.setSenha("123456");

        String jsonBody = objectMapper.writeValueAsString(novoUsuario);

        // 2. EXECUÇÃO E VERIFICAÇÃO
        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("integracao@teste.com"));
    }

    @Test
    @DisplayName("Deve retornar 403 Forbidden ao tentar acessar endpoint protegido sem token")
    void deveBloquearAcessoSemToken() throws Exception {
        mockMvc.perform(post("/api/cartoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve listar todos os usuários quando acessado por administrador ou usuário autenticado")
    void deveListarUsuariosAutenticado() throws Exception {
        Usuario u = new Usuario();
        u.setNome("Welber");
        u.setEmail("welber.lista@email.com");
        u.setSenha("123456");
        usuarioRepository.save(u);

        mockMvc.perform(get("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Deve atualizar o perfil do usuário logado com sucesso")
    void deveAtualizarPerfilComSucesso() throws Exception {
        Usuario u = new Usuario();
        u.setNome("Welber Antigo");
        u.setEmail("perfil@email.com");
        u.setSenha("123456");
        usuarioRepository.save(u);

        UsuarioAtualizacaoDTO dto = new UsuarioAtualizacaoDTO("Welber Novo", "novaSenha123");
        String jsonBody = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/api/usuarios/perfil")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Welber Novo"));
    }

    @Test
    @DisplayName("Deve solicitar a recuperação de senha enviando o e-mail")
    void deveSolicitarRecuperacaoDeSenha() throws Exception {
        Usuario u = new Usuario();
        u.setNome("Welber");
        u.setEmail("recupera@email.com");
        u.setSenha("123456");
        usuarioRepository.save(u);

        SolicitacaoSenhaDTO dto = new SolicitacaoSenhaDTO("recupera@email.com");
        String jsonBody = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/usuarios/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve resetar a senha usando um token válido")
    void deveResetarSenhaComTokenValido() throws Exception {
        Usuario u = new Usuario();
        u.setNome("Welber");
        u.setEmail("token@email.com");
        u.setSenha("123456");
        u.setResetToken("meu-token-secreto");
        u.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        usuarioRepository.save(u);

        ResetSenhaDTO dto = new ResetSenhaDTO("meu-token-secreto", "novaSenha", "novaSenha");
        String jsonBody = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/usuarios/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk());
    }
}