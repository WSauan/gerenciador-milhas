const API_URL = 'http://localhost:8080/api';
const token = localStorage.getItem('token');

// --- 1. VERIFICAÇÃO DE SEGURANÇA ---
if (!token) {
    alert('Sessão expirada. Faça login novamente.');
    window.location.href = 'index.html';
}

// Variáveis globais
let meusCartoes = [];
let minhasCompras = [];

// --- 2. INICIALIZAÇÃO ---
document.addEventListener('DOMContentLoaded', () => {
    const nomeSalvo = localStorage.getItem('usuarioNome');
    const emailSalvo = localStorage.getItem('usuarioEmail');
    const displayNome = nomeSalvo || emailSalvo || 'Bem-vindo';

    const elementoUsuario = document.getElementById('userEmail');
    if (elementoUsuario) {
        elementoUsuario.textContent = displayNome;
    }

    document.getElementById('btnLogout').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = 'index.html';
    });

    carregarDados();
});

// --- FUNÇÃO AUXILIAR PARA CORRIGIR DATA (TIMEZONE BUG) ---
function formatarDataSegura(dataString) {
    if (!dataString) return '---';
    
    // Se a data vier no formato YYYY-MM-DD (padrão do Java/Banco)
    // Nós quebramos a string e remontamos manualmente.
    const partes = dataString.split('-'); 
    if (partes.length === 3) {
        // partes[0] = ano, partes[1] = mes, partes[2] = dia
        return `${partes[2]}/${partes[1]}/${partes[0]}`;
    }
    
    // Fallback: Se não for esse formato, tenta o padrão
    return new Date(dataString).toLocaleDateString('pt-BR');
}

// --- 3. CARREGAMENTO DE DADOS ---
async function carregarDados() {
    try {
        const [resCartoes, resCompras] = await Promise.all([
            fetch(`${API_URL}/cartoes`, { headers: { 'Authorization': `Bearer ${token}` } }),
            fetch(`${API_URL}/aquisicoes`, { headers: { 'Authorization': `Bearer ${token}` } })
        ]);

        if (resCartoes.status === 403 || resCompras.status === 403) {
            localStorage.clear();
            alert("Sessão inválida. Faça login novamente.");
            window.location.href = 'index.html';
            return;
        }

        if (resCartoes.ok) meusCartoes = await resCartoes.json();
        
        if (resCompras.ok) {
            minhasCompras = await resCompras.json();
        } else {
            minhasCompras = [];
        }

        renderizarCartoes();
        renderizarTabela();
        atualizarKPIs();

    } catch (error) {
        console.error('Erro de conexão:', error);
    }
}

// --- 4. RENDERIZAÇÃO DOS CARTÕES ---
function renderizarCartoes() {
    const grid = document.getElementById('cardsGrid');
    grid.innerHTML = '';

    if (meusCartoes.length === 0) {
        grid.innerHTML = '<p style="color: #666;">Nenhum cartão cadastrado.</p>';
        return;
    }

    meusCartoes.forEach(cartao => {
        const pontosDoCartao = minhasCompras
            .filter(compra => compra.nomeCartao === cartao.nome) 
            .reduce((acc, compra) => acc + (compra.pontosCalculados || 0), 0);

        const div = document.createElement('div');
        div.className = 'card';
        div.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: start;">
                <h3 style="margin: 0; color: #555; font-size: 1.1em;">${cartao.nome}</h3>
                <button onclick="excluirCartao(${cartao.id})" title="Excluir Cartão"
                        style="background: none; border: none; cursor: pointer; font-size: 1.2em;">
                    🗑️
                </button>
            </div>
            
            <p style="font-size: 13px; color: #888; margin-top: 5px;">
                ${cartao.bandeira || 'Cartão de Crédito'}
            </p>
            
            <div style="margin-top: 15px;">
                <span style="font-size: 22px; font-weight: bold; color: #007bff;">
                    ${pontosDoCartao.toLocaleString('pt-BR')} Pontos
                </span>
            </div>
        `;
        grid.appendChild(div);
    });
}

// --- 5. RENDERIZAÇÃO DA TABELA (ATUALIZADA) ---
// --- 5. RENDERIZAÇÃO DA TABELA ---
function renderizarTabela() {
    const tbody = document.getElementById('tabelaHistorico');
    tbody.innerHTML = '';

    if (minhasCompras.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; padding: 20px; color: #888;">Nenhuma compra registrada.</td></tr>';
        return;
    }

    const listaOrdenada = [...minhasCompras].sort((a, b) => new Date(b.dataCompra) - new Date(a.dataCompra));

    listaOrdenada.forEach(compra => {
        const valorFormatado = (compra.valorGasto || 0).toFixed(2);
        const dataCompra = formatarDataSegura(compra.dataCompra);
        const dataCredito = compra.dataCredito ? formatarDataSegura(compra.dataCredito) : '---';
        
        // --- LÓGICA DE CORES DA BADGE ---
        let badgeClass = '';
        let badgeStyle = '';
        
        if (compra.status === 'APROVADO') {
            // Verde
            badgeStyle = 'background:#e8f5e9; color:#2e7d32;';
        } else if (compra.status === 'PENDENTE') {
            // Amarelo/Laranja
            badgeStyle = 'background:#fff3cd; color:#856404;';
        } else {
            // Cinza ou Vermelho (Cancelado)
            badgeStyle = 'background:#f8d7da; color:#721c24;';
        }

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${dataCompra}</td>
            <td style="color: #666; font-size: 0.95em;">${dataCredito}</td>
            <td>${compra.descricao}</td>
            <td>${compra.nomeCartao || '---'}</td>
            <td>R$ ${valorFormatado}</td>
            <td style="color: #28a745; font-weight: bold;">+${compra.pontosCalculados || 0}</td>
            
            <td><span class="badge" style="padding:4px 8px; border-radius:4px; font-size:12px; ${badgeStyle}">${compra.status}</span></td>
            
            <td style="text-align: center;">
                <button onclick="excluirCompra(${compra.id})" title="Excluir Compra"
                        style="background: none; border: none; cursor: pointer; color: #dc3545; font-size: 1.1em;">
                    ❌
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// --- 6. KPIs ---
function atualizarKPIs() {
    const kpiElement = document.getElementById('kpiPrazo');
    if (minhasCompras.length < 2) {
        kpiElement.textContent = "---";
        return;
    }
    const sorted = [...minhasCompras].sort((a, b) => new Date(a.dataCompra) - new Date(b.dataCompra));
    const primeiraData = new Date(sorted[0].dataCompra);
    const ultimaData = new Date(sorted[sorted.length - 1].dataCompra);
    const diferencaTempo = Math.abs(ultimaData - primeiraData);
    const diferencaDias = Math.ceil(diferencaTempo / (1000 * 60 * 60 * 24));
    const media = diferencaDias / (minhasCompras.length - 1);
    kpiElement.textContent = `${media.toFixed(1)} dias`;
}

// --- 7. AÇÕES DE EXCLUSÃO ---
async function excluirCartao(id) {
    if (confirm('⚠️ Tem certeza? Ao excluir o cartão, o histórico de pontos dele também será afetado.')) {
        try {
            const response = await fetch(`${API_URL}/cartoes/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (response.ok) { alert('Cartão excluído!'); carregarDados(); } 
            else { alert('Erro ao excluir cartão.'); }
        } catch (error) { console.error(error); alert('Erro de conexão.'); }
    }
}

async function excluirCompra(id) {
    if (confirm('Deseja realmente excluir esta compra? Os pontos serão estornados do cartão.')) {
        try {
            const response = await fetch(`${API_URL}/aquisicoes/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (response.ok) { carregarDados(); } 
            else { alert('Erro ao excluir compra.'); }
        } catch (error) { console.error(error); alert('Erro de conexão.'); }
    }
}

// --- 8. EXPORTAÇÃO ---

function baixarPDF() {
    if (!window.jspdf) {
        alert("Erro: Biblioteca de PDF não carregada.");
        return;
    }
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF('landscape');

    doc.setFontSize(18);
    doc.text("Relatório Detalhado de Milhas", 14, 20);
    doc.setFontSize(10);
    doc.text(`Gerado em: ${new Date().toLocaleDateString()} às ${new Date().toLocaleTimeString()}`, 14, 28);

    const colunas = ["Data Compra", "Data Crédito", "Descrição", "Cartão", "Valor (R$)", "Pontos", "Status"];
    const linhas = [];

    minhasCompras.forEach(compra => {
        const dCompra = formatarDataSegura(compra.dataCompra);
        const dCredito = compra.dataCredito ? formatarDataSegura(compra.dataCredito) : '---';
        
        const valor = (compra.valorGasto || 0).toFixed(2).replace('.', ',');
        const cartao = compra.nomeCartao || '---';
        const pontos = compra.pontosCalculados || 0;
        const status = compra.status || '---';
        
        linhas.push([dCompra, dCredito, compra.descricao, cartao, valor, pontos, status]);
    });

    doc.autoTable({
        head: [colunas],
        body: linhas,
        startY: 35,
        theme: 'grid',
        styles: { fontSize: 9 },
        headStyles: { fillColor: [23, 162, 184] }
    });

    doc.save("relatorio_milhas_completo.pdf");
}

function baixarCSV() {
    if (minhasCompras.length === 0) {
        alert("Sem dados para exportar.");
        return;
    }
    let csvContent = "data:text/csv;charset=utf-8,";
    csvContent += "Data Compra;Data Credito;Descricao;Cartao;Valor;Pontos;Status\n";

    minhasCompras.forEach(compra => {
        const dCompra = formatarDataSegura(compra.dataCompra);
        const dCredito = compra.dataCredito ? formatarDataSegura(compra.dataCredito) : '';
        
        const val = (compra.valorGasto || 0).toString().replace('.',',');
        const pts = compra.pontosCalculados || 0;
        const status = compra.status || '';
        
        const row = `${dCompra};${dCredito};${compra.descricao};${compra.nomeCartao};${val};${pts};${status}`;
        csvContent += row + "\n";
    });

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "meus_pontos.csv");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}