const API_URL = 'http://localhost:8080/api';
const token = localStorage.getItem('token');

// Proteção de Rota
if (!token) {
    window.location.href = 'index.html';
}

// --- 1. Função para carregar os Selects (Bandeiras e Programas) ---
async function carregarOpcoes() {
    try {
        // Busca Bandeiras
        const resBandeiras = await fetch(`${API_URL}/bandeiras`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const bandeiras = await resBandeiras.json();

        // Busca Programas
        const resProgramas = await fetch(`${API_URL}/programas`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const programas = await resProgramas.json();

        // Preenche o Select de Bandeiras
        const selectBandeira = document.getElementById('bandeiraId');
        selectBandeira.innerHTML = '<option value="">Selecione uma bandeira...</option>';
        bandeiras.forEach(b => {
            selectBandeira.innerHTML += `<option value="${b.id}">${b.nome}</option>`;
        });

        // Preenche o Select de Programas
        const selectPrograma = document.getElementById('programaId');
        selectPrograma.innerHTML = '<option value="">Selecione um programa...</option>';
        programas.forEach(p => {
            selectPrograma.innerHTML += `<option value="${p.id}">${p.nome}</option>`;
        });

    } catch (error) {
        console.error("Erro ao carregar opções:", error);
        alert("Erro ao carregar bandeiras ou programas. Verifique se o backend está rodando.");
    }
}

// Executa ao carregar a página
carregarOpcoes();

// --- 2. Função de Salvar (Submit do Formulário) ---
document.getElementById('formCartao').addEventListener('submit', async function(event) {
    event.preventDefault();

    const btn = document.getElementById('btnSalvar');
    btn.disabled = true;
    btn.textContent = 'Salvando...';

    // Monta o JSON
    const payload = {
        nome: document.getElementById('nome').value,
        saldoDePontos: parseFloat(document.getElementById('saldoDePontos').value),
        fatorConversao: parseFloat(document.getElementById('fatorConversao').value),
        bandeiraId: parseInt(document.getElementById('bandeiraId').value),
        programaId: parseInt(document.getElementById('programaId').value)
    };

    try {
        const response = await fetch(`${API_URL}/cartoes`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            alert('Cartão cadastrado com sucesso!');
            window.location.href = 'dashboard.html'; // Volta para a home
        } else {
            alert('Erro ao cadastrar. Verifique os dados.');
        }
    } catch (error) {
        console.error(error);
        alert('Erro de conexão.');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Salvar Cartão';
    }
});