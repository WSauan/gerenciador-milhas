package br.com.milhas.gerenciador.controller;

import br.com.milhas.gerenciador.model.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // CORREÇÃO: Removemos o (addFilters = false) para ATIVAR a segurança
@Transactional
class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        // Como configuramos .permitAll() no SecurityConfig para este endpoint,
        // o teste deve passar mesmo com a segurança ligada.
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
        // Tenta acessar /api/cartoes (que é protegido) sem enviar token
        // Agora que a segurança está ligada, o Spring Security vai barrar e retornar 403
        mockMvc.perform(post("/api/cartoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden()); // Agora sim, esperamos 403 e receberemos 403
    }
}