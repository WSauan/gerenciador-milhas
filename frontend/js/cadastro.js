const API_URL = 'http://localhost:8080/api';

async function realizarCadastro() {
    const btn = document.getElementById('btnCadastrar');
    const textoOriginal = btn.textContent;
    
    // Bloqueia o botão
    btn.disabled = true;
    btn.textContent = 'Cadastrando...';

    try {
        // 1. Captura os dados
        const nome = document.getElementById('nome').value;
        const email = document.getElementById('email').value;
        const senha = document.getElementById('senha').value;

        if (!nome || !email || !senha) {
            throw new Error("Preencha todos os campos.");
        }

        // 2. Envia para o Backend
        const response = await fetch(`${API_URL}/usuarios`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                nome: nome,
                email: email,
                senha: senha
            })
        });

        // 3. Trata a resposta
        if (response.ok) {
            alert('Conta criada com sucesso! Faça login para continuar.');
            window.location.href = 'index.html';
        } else {
            // Tenta ler mensagem de erro do backend (ex: "Email já cadastrado")
            // Se não tiver corpo, usa mensagem genérica
            try {
                const erroJson = await response.json();
                throw new Error(erroJson.message || 'Erro ao criar conta.');
            } catch (e) {
                throw new Error('Erro ao criar conta. Verifique os dados.');
            }
        }

    } catch (error) {
        console.error(error);
        alert(error.message);
    } finally {
        btn.disabled = false;
        btn.textContent = textoOriginal;
    }
}