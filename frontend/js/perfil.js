const API_URL = 'http://localhost:8080/api';
const token = localStorage.getItem('token');

// 1. Proteção de Rota: Se não estiver logado, manda pro login
if (!token) {
    window.location.href = 'index.html';
}

// 2. Ao carregar a tela, preenche o e-mail (que não pode mudar)
document.addEventListener('DOMContentLoaded', () => {
    const emailSalvo = localStorage.getItem('usuarioEmail');
    
    if (emailSalvo) {
        document.getElementById('email').value = emailSalvo;
    }
});

// 3. Lógica de Salvar Alterações
document.getElementById('formPerfil').addEventListener('submit', async (e) => {
    e.preventDefault();

    const btn = document.getElementById('btnSalvar');
    const textoOriginal = btn.textContent;
    
    // Bloqueia o botão para evitar cliques duplos
    btn.disabled = true;
    btn.textContent = 'Salvando...';

    const novoNome = document.getElementById('nome').value;
    const novaSenha = document.getElementById('senha').value;

    // --- MONTAGEM INTELIGENTE DO PAYLOAD ---
    const payload = {};

    // Só envia o nome se o usuário digitou algo (ignora espaços vazios)
    if (novoNome && novoNome.trim() !== "") {
        payload.nome = novoNome.trim();
    }

    // Só envia a senha se o usuário digitou algo
    if (novaSenha && novaSenha.trim() !== "") {
        payload.senha = novaSenha.trim();
    }

    // Se os dois estiverem vazios, não tem o que salvar
    if (Object.keys(payload).length === 0) {
        alert("Preencha o nome ou a senha para salvar alterações.");
        btn.disabled = false;
        btn.textContent = textoOriginal;
        return;
    }

    try {
        const response = await fetch(`${API_URL}/perfil`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert('Perfil atualizado com sucesso!');

            // Se a senha foi alterada, forçamos um novo login por segurança
            if (payload.senha) {
                alert('Como você alterou sua senha, faça login novamente.');
                localStorage.removeItem('token');
                localStorage.removeItem('usuarioEmail');
                window.location.href = 'index.html';
            } else {
                // Se mudou só o nome, volta pro Dashboard
                window.location.href = 'dashboard.html';
            }
        } else {
            // Tenta ler mensagem de erro do backend
            const erroTexto = await response.text(); 
            alert('Erro ao atualizar: ' + (erroTexto || 'Tente novamente.'));
        }

    } catch (error) {
        console.error("Erro na requisição:", error);
        alert('Erro de conexão com o servidor.');
    } finally {
        // Libera o botão novamente
        btn.disabled = false;
        btn.textContent = textoOriginal;
    }
});