const API_URL = 'http://localhost:8080/api';

document.getElementById('loginForm').addEventListener('submit', async function(event) {
    event.preventDefault(); // Impede que a página recarregue

    // 1. Capturar os dados dos inputs
    const email = document.getElementById('email').value;
    const senha = document.getElementById('senha').value;
    const btn = document.getElementById('btnEntrar');

    // Feedback visual (desabilita botão)
    btn.textContent = 'Entrando...';
    btn.disabled = true;

    try {
        // 2. Enviar requisição POST para o Backend
        const response = await fetch(`${API_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: email,
                senha: senha
            })
        });

        // 3. Verificar a resposta
        if (response.ok) {
            const data = await response.json();
            
            // SUCESSO: Salvar o token no navegador
            localStorage.setItem('token', data.token);
            localStorage.setItem('usuarioEmail', email); // Útil para o dashboard

            alert('Login realizado com sucesso!');
            
            // Redirecionar para o Dashboard (vamos criar depois)
            window.location.href = 'dashboard.html';
        } else {
            // ERRO (403 Forbidden, etc)
            alert('E-mail ou senha inválidos!');
        }

    } catch (error) {
        console.error('Erro:', error);
        alert('Erro ao conectar com o servidor. O backend está rodando?');
    } finally {
        // Reabilita o botão
        btn.textContent = 'Entrar';
        btn.disabled = false;
    }
});