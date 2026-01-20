const API_URL = 'http://localhost:8080/api';
const token = localStorage.getItem('token');

// Proteção de Rota
if (!token) {
    window.location.href = 'index.html';
}

// Carregar dados ao abrir a página
document.addEventListener('DOMContentLoaded', () => {
    carregarCartoes();
});

// --- Função 1: Carregar Cartões ---
async function carregarCartoes() {
    try {
        const response = await fetch(`${API_URL}/cartoes`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        if (!response.ok) throw new Error('Falha ao buscar cartões');

        const cartoes = await response.json();
        const select = document.getElementById('cartaoId');
        select.innerHTML = '<option value="">Selecione o cartão...</option>';
        
        cartoes.forEach(c => {
            select.innerHTML += `<option value="${c.id}">${c.nome} (Fator: ${c.fatorConversao})</option>`;
        });
    } catch (error) {
        console.error(error);
        alert('Erro ao carregar cartões.');
    }
}

// --- Função 2: Enviar Compra (Chamada pelo onclick do HTML) ---
async function enviarCompra() {
    console.log("Função enviarCompra iniciada!");

    const btn = document.getElementById('btnSalvar');
    const textoOriginal = btn.textContent;
    
    // 1. Bloqueia o botão para evitar clique duplo
    btn.disabled = true;
    btn.textContent = 'Enviando...';

    try {
        // 2. Validações Manuais
        const cartaoId = document.getElementById('cartaoId').value;
        const descricao = document.getElementById('descricao').value;
        const valor = document.getElementById('valorGasto').value;
        const dataCompra = document.getElementById('dataCompra').value;
        const dataPrevista = document.getElementById('dataPrevista').value;
        const arquivoInput = document.getElementById('comprovante');

        if (!cartaoId) throw new Error("Selecione um cartão.");
        if (!descricao) throw new Error("Preencha a descrição.");
        if (!valor) throw new Error("Preencha o valor.");
        if (!dataCompra) throw new Error("Informe a data da compra.");
        if (!dataPrevista) throw new Error("Informe a previsão de crédito.");
        if (arquivoInput.files.length === 0) throw new Error("É obrigatório anexar um comprovante.");

        // 3. Monta os dados
        const dadosAquisicao = {
            descricao: descricao,
            valorGasto: parseFloat(valor),
            dataCompra: dataCompra,
            dataPrevistaCredito: dataPrevista,
            cartaoId: parseInt(cartaoId)
        };

        const arquivo = arquivoInput.files[0];
        const formData = new FormData();
        formData.append('aquisicao', JSON.stringify(dadosAquisicao)); 
        formData.append('comprovante', arquivo);

        // 4. Envia
        console.log("Enviando requisição...");
        const response = await fetch(`${API_URL}/aquisicoes`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            body: formData
        });

        console.log("Resposta recebida:", response.status);

        if (response.ok) {
            const jsonResposta = await response.json();
            abrirModalSucesso(jsonResposta.pontosCalculados);
        } else {
            const erroTexto = await response.text(); // Tenta ler msg de erro do backend
            throw new Error(`Erro do servidor (${response.status}): ${erroTexto}`);
        }

    } catch (error) {
        console.error(error);
        alert(error.message);
        // Só reabilita o botão se der erro. Se der sucesso, o modal aparece.
        btn.disabled = false;
        btn.textContent = textoOriginal;
    }
}

// --- Função 3: Modal ---
function abrirModalSucesso(pontos) {
    console.log("Abrindo modal de sucesso..."); // Log para debug
    
    const pontosFormatados = pontos.toLocaleString('pt-BR');
    const msg = document.getElementById('mensagemSucesso');
    
    if (msg) {
        msg.innerHTML = `Compra registrada!<br>Você acumulou <strong>${pontosFormatados} pontos</strong>.`;
    }
    
    const modal = document.getElementById('modalSucesso');
    if (modal) {
        modal.style.display = 'flex'; // Mostra o modal
    } else {
        console.error("Erro: Modal não encontrado no HTML");
    }

    // IMPORTANTE: Removi o redirecionamento automático.
    // O usuário SÓ sai daqui se clicar no botão.
    const btnFechar = document.getElementById('btnFecharModal');
    if (btnFechar) {
        // Removemos listeners antigos para evitar duplo clique
        const novoBtn = btnFechar.cloneNode(true);
        btnFechar.parentNode.replaceChild(novoBtn, btnFechar);
        
        novoBtn.onclick = function(e) {
            e.preventDefault(); // Garante que o botão do modal não recarregue
            window.location.href = 'dashboard.html';
        };
    }
}