const API_URL = 'http://localhost:8080/api';
const token = localStorage.getItem('token');
const email = localStorage.getItem('usuarioEmail');

// Proteção de rota: se não tiver token, joga pro login
if (!token) {
    window.location.href = 'index.html';
}

// Inicialização da tela
document.getElementById('userEmail').textContent = email || 'Usuário';
document.getElementById('btnLogout').addEventListener('click', logout);

// Carrega os dados assim que a página abre
document.addEventListener('DOMContentLoaded', () => {
    carregarCartoes();
    carregarKPIs();
    carregarHistorico();
});

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('usuarioEmail');
    window.location.href = 'index.html';
}

// --- 1. Carregar Cartões (Com botão de excluir) ---
async function carregarCartoes() {
    const grid = document.getElementById('cardsGrid');
    try {
        const response = await fetch(`${API_URL}/dashboard/pontos-por-cartao`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        // Se o token expirou (erro 403), faz logout
        if (response.status === 403) return logout();
        
        const dados = await response.json();
        grid.innerHTML = '';

        if (dados.length === 0) {
            grid.innerHTML = '<p>Nenhum cartão cadastrado.</p>';
            return;
        }

        dados.forEach(item => {
            // Renderiza o cartão com o ícone de lixeira no canto superior direito
            grid.innerHTML += `
                <div class="card-item" style="position: relative;">
                    <button onclick="excluirCartao(${item.id})" 
                            title="Excluir Cartão"
                            style="position: absolute; top: 10px; right: 10px; background: none; border: none; color: #dc3545; cursor: pointer; font-size: 18px; width: auto; padding: 0;">
                        🗑️
                    </button>
                    <h3>${item.nomeCartao}</h3>
                    <div class="pontos">${item.totalPontos.toLocaleString('pt-BR')} pts</div>
                </div>
            `;
        });
    } catch (error) {
        console.error("Erro ao carregar cartões:", error);
    }
}

// --- 2. Função de Excluir (Nova Lógica) ---
async function excluirCartao(id) {
    // Alerta de segurança para o usuário
    const confirmacao = confirm(
        "⚠️ ATENÇÃO!\n\n" +
        "Ao excluir este cartão, TODAS as compras registradas nele também serão apagadas permanentemente do histórico.\n\n" +
        "Deseja realmente continuar?"
    );

    if (!confirmacao) {
        return; // Usuário cancelou
    }

    try {
        const response = await fetch(`${API_URL}/cartoes/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.status === 204) { // 204 = Sucesso
            alert("Cartão removido com sucesso!");
            
            // Recarrega todas as partes da tela para atualizar os dados
            carregarCartoes(); 
            carregarKPIs();
            carregarHistorico();
        } else {
            const erroTexto = await response.text();
            alert("Erro ao excluir: " + erroTexto);
        }

    } catch (error) {
        console.error("Erro na exclusão:", error);
        alert("Erro de conexão ao tentar excluir.");
    }
}

// --- 3. Carregar KPI (Prazo Médio) ---
async function carregarKPIs() {
    try {
        const response = await fetch(`${API_URL}/dashboard/prazo-medio-recebimento`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();
        
        const dias = data.mediaEmDias ? data.mediaEmDias.toFixed(1) : '0';
        document.getElementById('kpiPrazo').textContent = `${dias} dias`;
        
    } catch (error) {
        console.error("Erro KPI:", error);
    }
}

// --- 4. Carregar Histórico (Tabela) ---
async function carregarHistorico() {
    const tbody = document.getElementById('tabelaHistorico');
    try {
        const response = await fetch(`${API_URL}/aquisicoes`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const lista = await response.json();
        
        tbody.innerHTML = '';

        if (lista.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center">Nenhuma compra registrada.</td></tr>';
            return;
        }

        lista.forEach(compra => {
            const dataFormatada = new Date(compra.dataCompra).toLocaleDateString('pt-BR');
            const valorFormatado = compra.valorGasto.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
            
            tbody.innerHTML += `
                <tr>
                    <td>${dataFormatada}</td>
                    <td>${compra.descricao}</td>
                    <td>${compra.nomeCartao}</td>
                    <td>${valorFormatado}</td>
                    <td style="color: green; font-weight: bold;">+${compra.pontosCalculados}</td>
                    <td><span class="status-badge">${compra.status}</span></td>
                </tr>
            `;
        });

    } catch (error) {
        console.error("Erro Tabela:", error);
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:red">Erro ao carregar dados.</td></tr>';
    }
}

// --- 5. Funções de Exportação (CSV e PDF) ---
async function baixarArquivo(endpoint, nomePadrao) {
    try {
        const response = await fetch(`${API_URL}/dashboard/${endpoint}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        if (!response.ok) throw new Error('Erro ao baixar');

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        
        const a = document.createElement('a');
        a.href = url;
        a.download = nomePadrao;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);

    } catch (error) {
        console.error(error);
        alert('Erro ao gerar relatório.');
    }
}

function baixarPDF() {
    baixarArquivo('exportar-historico-pdf', 'relatorio.pdf');
}

function baixarCSV() {
    baixarArquivo('exportar-historico-csv', 'relatorio.csv');
}