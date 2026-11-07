/**
 * Arquivo Digital — Console Web da Secretaria da ONG
 * Consome a API REST Spring Boot (v1)
 */

const API_BASE_URL = window.API_BASE_URL || 
    (window.location.origin && window.location.origin.includes(':8080')
        ? `${window.location.origin}/api/v1`
        : 'http://localhost:8080/api/v1');

// Estado global da aplicação
const state = {
    currentPage: 0,
    pageSize: 10,
    totalPages: 1,
    totalElements: 0,
    filters: {
        tipo: '',
        dataInicio: '',
        dataFim: '',
        titulo: ''
    }
};

// Elementos DOM
const elements = {
    // Status
    connectionStatus: document.getElementById('connectionStatus'),
    apiStatusText: document.getElementById('apiStatusText'),

    // Métricas
    metricTotal: document.getElementById('metricTotal'),
    metricVigentes: document.getElementById('metricVigentes'),
    metricArquivados: document.getElementById('metricArquivados'),
    tipoAta: document.getElementById('tipoAta'),
    tipoContrato: document.getElementById('tipoContrato'),
    tipoRelatorio: document.getElementById('tipoRelatorio'),
    tipoOficio: document.getElementById('tipoOficio'),

    // Filtros
    filterForm: document.getElementById('filterForm'),
    filterTipo: document.getElementById('filterTipo'),
    filterDataInicio: document.getElementById('filterDataInicio'),
    filterDataFim: document.getElementById('filterDataFim'),
    filterTitulo: document.getElementById('filterTitulo'),
    btnLimparFiltros: document.getElementById('btnLimparFiltros'),

    // Estados e Tabela
    resultsCount: document.getElementById('resultsCount'),
    tableContainer: document.getElementById('tableContainer'),
    documentsTableBody: document.getElementById('documentsTableBody'),
    loadingState: document.getElementById('loadingState'),
    emptyState: document.getElementById('emptyState'),
    errorState: document.getElementById('errorState'),
    errorMessage: document.getElementById('errorMessage'),
    errorDetail: document.getElementById('errorDetail'),
    btnRetry: document.getElementById('btnRetry'),

    // Paginação
    paginationBar: document.getElementById('paginationBar'),
    paginationInfo: document.getElementById('paginationInfo'),
    currentPageLabel: document.getElementById('currentPageLabel'),
    btnPrevPage: document.getElementById('btnPrevPage'),
    btnNextPage: document.getElementById('btnNextPage'),

    // Modais
    modalNovoDocumento: document.getElementById('modalNovoDocumento'),
    btnNovoDocumento: document.getElementById('btnNovoDocumento'),
    formNovoDocumento: document.getElementById('formNovoDocumento'),
    novoDocError: document.getElementById('novoDocError'),

    modalNovaVersao: document.getElementById('modalNovaVersao'),
    formNovaVersao: document.getElementById('formNovaVersao'),
    versaoDocId: document.getElementById('versaoDocId'),
    versaoDocTitulo: document.getElementById('versaoDocTitulo'),
    versaoDocAtual: document.getElementById('versaoDocAtual'),
    versaoDocProxima: document.getElementById('versaoDocProxima'),
    novaVersaoError: document.getElementById('novaVersaoError'),

    modalHistorico: document.getElementById('modalHistorico'),
    histDocTitulo: document.getElementById('histDocTitulo'),
    histLoading: document.getElementById('histLoading'),
    histContent: document.getElementById('histContent'),

    modalArquivar: document.getElementById('modalArquivar'),
    arquivarDocId: document.getElementById('arquivarDocId'),
    arquivarDocTitulo: document.getElementById('arquivarDocTitulo'),
    btnConfirmarArquivar: document.getElementById('btnConfirmarArquivar'),

    toastContainer: document.getElementById('toastContainer')
};

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    carregarTudo();
});

function setupEventListeners() {
    // Filtros
    elements.filterForm.addEventListener('submit', (e) => {
        e.preventDefault();
        state.currentPage = 0;
        state.filters.tipo = elements.filterTipo.value.trim();
        state.filters.dataInicio = elements.filterDataInicio.value;
        state.filters.dataFim = elements.filterDataFim.value;
        state.filters.titulo = elements.filterTitulo.value.trim();
        carregarDocumentos();
    });

    elements.btnLimparFiltros.addEventListener('click', () => {
        elements.filterForm.reset();
        state.currentPage = 0;
        state.filters = { tipo: '', dataInicio: '', dataFim: '', titulo: '' };
        carregarDocumentos();
    });

    elements.btnRetry.addEventListener('click', carregarTudo);

    // Paginação
    elements.btnPrevPage.addEventListener('click', () => {
        if (state.currentPage > 0) {
            state.currentPage--;
            carregarDocumentos();
        }
    });

    elements.btnNextPage.addEventListener('click', () => {
        if (state.currentPage < state.totalPages - 1) {
            state.currentPage++;
            carregarDocumentos();
        }
    });

    // Abrir Modal Novo Documento
    elements.btnNovoDocumento.addEventListener('click', () => {
        elements.formNovoDocumento.reset();
        elements.novoDocError.classList.add('hidden');
        elements.novoDocError.textContent = '';
        // Sugere a data de hoje por padrão
        document.getElementById('novoDataDocumento').value = new Date().toISOString().split('T')[0];
        abrirModal(elements.modalNovoDocumento);
    });

    // Submissão Novo Documento (RF01 / CA1)
    elements.formNovoDocumento.addEventListener('submit', async (e) => {
        e.preventDefault();
        elements.novoDocError.classList.add('hidden');
        const formData = new FormData(elements.formNovoDocumento);
        const payload = {
            titulo: formData.get('titulo').trim(),
            tipo: formData.get('tipo'),
            data_documento: formData.get('dataDocumento'),
            responsavel: formData.get('responsavel').trim(),
            observacao_inicial: formData.get('observacaoInicial') ? formData.get('observacaoInicial').trim() : null
        };

        try {
            const resp = await fetch(`${API_BASE_URL}/documentos`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (!resp.ok) {
                const err = await resp.json().catch(() => ({ detail: 'Erro ao cadastrar documento.' }));
                throw new Error(err.detail || 'Falha ao salvar documento.');
            }

            const docCriado = await resp.json();
            fecharModal(elements.modalNovoDocumento);
            mostrarToast(`Documento "${docCriado.titulo}" cadastrado com sucesso (v1)!`, 'success');
            carregarTudo();
        } catch (err) {
            elements.novoDocError.textContent = err.message;
            elements.novoDocError.classList.remove('hidden');
        }
    });

    // Submissão Nova Versão (RF03 / CA2)
    elements.formNovaVersao.addEventListener('submit', async (e) => {
        e.preventDefault();
        elements.novaVersaoError.classList.add('hidden');
        const docId = elements.versaoDocId.value;
        const observacao = document.getElementById('versaoObservacao').value.trim();

        try {
            const resp = await fetch(`${API_BASE_URL}/documentos/${docId}/versoes`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ observacao })
            });

            if (!resp.ok) {
                const err = await resp.json().catch(() => ({ detail: 'Erro ao registrar versão.' }));
                throw new Error(err.detail || 'Falha ao registrar versão.');
            }

            const versaoCriada = await resp.json();
            fecharModal(elements.modalNovaVersao);
            mostrarToast(`Nova versão v${versaoCriada.numero} registrada com sucesso!`, 'success');
            carregarTudo();
        } catch (err) {
            elements.novaVersaoError.textContent = err.message;
            elements.novaVersaoError.classList.remove('hidden');
        }
    });

    // Confirmação de Arquivamento (RF04 / CA3 / RNF06)
    elements.btnConfirmarArquivar.addEventListener('click', async () => {
        const docId = elements.arquivarDocId.value;
        try {
            const resp = await fetch(`${API_BASE_URL}/documentos/${docId}/arquivar`, {
                method: 'PATCH'
            });

            if (!resp.ok) {
                const err = await resp.json().catch(() => ({ detail: 'Erro ao arquivar documento.' }));
                throw new Error(err.detail || 'Falha ao arquivar.');
            }

            fecharModal(elements.modalArquivar);
            mostrarToast('Documento arquivado definitivamente. Alterações bloqueadas.', 'warning');
            carregarTudo();
        } catch (err) {
            fecharModal(elements.modalArquivar);
            mostrarToast(`Erro ao arquivar: ${err.message}`, 'error');
        }
    });

    // Fechar modais ao clicar no botão fechar ou fora do card
    document.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.modal-overlay').forEach(modal => modal.classList.add('hidden'));
        });
    });

    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                overlay.classList.add('hidden');
            }
        });
    });

    // Tecla ESC para fechar modais
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal-overlay').forEach(modal => modal.classList.add('hidden'));
        }
    });
}

// Carregamento Integrado
async function carregarTudo() {
    await Promise.all([carregarMetricas(), carregarDocumentos()]);
}

// Consulta de Métricas (RF06 / US06)
async function carregarMetricas() {
    try {
        const resp = await fetch(`${API_BASE_URL}/metricas`);
        if (!resp.ok) throw new Error('Falha ao obter métricas.');
        const data = await resp.json();

        setBackendOnline(true);
        elements.metricTotal.textContent = data.total ?? 0;
        elements.metricVigentes.textContent = data.vigentes ?? 0;
        elements.metricArquivados.textContent = data.arquivados ?? 0;

        const porTipo = data.por_tipo || {};
        elements.tipoAta.textContent = porTipo.ata ?? 0;
        elements.tipoContrato.textContent = porTipo.contrato ?? 0;
        elements.tipoRelatorio.textContent = porTipo.relatorio ?? 0;
        elements.tipoOficio.textContent = porTipo.oficio ?? 0;
    } catch (err) {
        setBackendOnline(false);
        console.error('Erro ao buscar métricas:', err);
    }
}

// Consulta de Documentos com Filtros e Paginação (RF02 / CA4)
async function carregarDocumentos() {
    mostrarEstado('loading');

    const params = new URLSearchParams({
        page: state.currentPage,
        size: state.pageSize
    });

    if (state.filters.tipo) params.append('tipo', state.filters.tipo);
    if (state.filters.dataInicio) params.append('data_inicio', state.filters.dataInicio);
    if (state.filters.dataFim) params.append('data_fim', state.filters.dataFim);
    if (state.filters.titulo) params.append('titulo', state.filters.titulo);

    try {
        const resp = await fetch(`${API_BASE_URL}/documentos?${params.toString()}`);
        if (!resp.ok) {
            const err = await resp.json().catch(() => ({ detail: 'Erro HTTP ' + resp.status }));
            throw new Error(err.detail || 'Falha ao recuperar documentos do servidor.');
        }

        const data = await resp.json();
        setBackendOnline(true);

        state.totalPages = data.total_pages || 1;
        state.totalElements = data.total_elements || 0;

        const docs = data.items || data.content || [];
        state.totalPages = data.total_pages || data.totalPages || 1;
        state.totalElements = data.total_elements ?? data.totalElements ?? docs.length;
        renderizarTabela(docs);
    } catch (err) {
        setBackendOnline(false);
        mostrarEstado('error', err.message);
    }
}

// Renderização da Tabela de Documentos
function renderizarTabela(documentos) {
    if (!documentos || documentos.length === 0) {
        mostrarEstado('empty');
        elements.resultsCount.textContent = '0 documentos encontrados';
        elements.paginationBar.classList.add('hidden');
        return;
    }

    elements.documentsTableBody.innerHTML = '';
    documentos.forEach(doc => {
        const tr = document.createElement('tr');
        const isArquivado = doc.status === 'arquivado';

        tr.innerHTML = `
            <td><code>#${doc.id}</code></td>
            <td>
                <span class="doc-title">${escapeHtml(doc.titulo)}</span>
            </td>
            <td>
                <span class="badge badge-tipo-${doc.tipo}">${doc.tipo}</span>
            </td>
            <td>${formatarData(doc.data_documento)}</td>
            <td>${escapeHtml(doc.responsavel)}</td>
            <td style="text-align: center;">
                <span class="badge badge-version">v${doc.versao_atual}</span>
            </td>
            <td style="text-align: center;">
                <span class="badge ${isArquivado ? 'badge-arquivado' : 'badge-vigente'}">
                    ${doc.status}
                </span>
            </td>
            <td>
                <div class="actions-cell">
                    <button type="button" class="btn btn-secondary btn-sm" onclick="abrirHistorico(${doc.id}, '${escapeForAttr(doc.titulo)}')">
                        📜 Histórico
                    </button>
                    <button type="button" class="btn btn-primary btn-sm" 
                            onclick="abrirNovaVersao(${doc.id}, '${escapeForAttr(doc.titulo)}', ${doc.versao_atual})"
                            ${isArquivado ? 'disabled title="Documento arquivado é imutável (RF04 / CA3)"' : ''}>
                        ➕ Versão
                    </button>
                    <button type="button" class="btn btn-danger btn-sm" 
                            onclick="abrirConfirmarArquivar(${doc.id}, '${escapeForAttr(doc.titulo)}')"
                            ${isArquivado ? 'disabled title="Documento já está arquivado"' : ''}>
                        🔒 Arquivar
                    </button>
                </div>
            </td>
        `;
        elements.documentsTableBody.appendChild(tr);
    });

    mostrarEstado('table');
    atualizarPaginacao();
}

// Histórico de Versões (RF05)
window.abrirHistorico = async function(docId, docTitulo) {
    elements.histDocTitulo.textContent = docTitulo;
    elements.histLoading.classList.remove('hidden');
    elements.histContent.classList.add('hidden');
    abrirModal(elements.modalHistorico);

    try {
        const resp = await fetch(`${API_BASE_URL}/documentos/${docId}/versoes`);
        if (!resp.ok) throw new Error('Não foi possível obter o histórico de versões.');
        const versoes = await resp.json();

        elements.histContent.innerHTML = '';
        if (versoes.length === 0) {
            elements.histContent.innerHTML = '<p class="text-muted">Nenhuma versão registrada.</p>';
        } else {
            versoes.forEach(v => {
                const item = document.createElement('div');
                item.className = 'timeline-item';
                item.innerHTML = `
                    <div class="timeline-dot">${v.numero}</div>
                    <div class="timeline-content">
                        <div class="timeline-meta">
                            <strong>Versão ${v.numero}</strong>
                            <span>${formatarDataHora(v.criado_em)}</span>
                        </div>
                        <div class="timeline-obs">
                            ${v.observacao ? escapeHtml(v.observacao) : '<em>Sem observação registrada.</em>'}
                        </div>
                    </div>
                `;
                elements.histContent.appendChild(item);
            });
        }

        elements.histLoading.classList.add('hidden');
        elements.histContent.classList.remove('hidden');
    } catch (err) {
        elements.histLoading.classList.add('hidden');
        elements.histContent.innerHTML = `<div class="alert alert-error">${escapeHtml(err.message)}</div>`;
        elements.histContent.classList.remove('hidden');
    }
};

// Modal Nova Versão (RF03 / CA2)
window.abrirNovaVersao = function(docId, docTitulo, versaoAtual) {
    elements.versaoDocId.value = docId;
    elements.versaoDocTitulo.textContent = docTitulo;
    elements.versaoDocAtual.textContent = `v${versaoAtual}`;
    elements.versaoDocProxima.textContent = `v${versaoAtual + 1}`;
    elements.formNovaVersao.reset();
    elements.novaVersaoError.classList.add('hidden');
    abrirModal(elements.modalNovaVersao);
};

// Modal Confirmar Arquivar (RF04 / CA3)
window.abrirConfirmarArquivar = function(docId, docTitulo) {
    elements.arquivarDocId.value = docId;
    elements.arquivarDocTitulo.textContent = `"${docTitulo}"`;
    abrirModal(elements.modalArquivar);
};

// Atualização da Barra de Paginação
function atualizarPaginacao() {
    elements.paginationBar.classList.remove('hidden');
    const total = state.totalElements;
    const pagAtual = state.currentPage + 1;
    const totalPags = Math.max(state.totalPages, 1);

    elements.resultsCount.textContent = `Exibindo ${total} documento${total === 1 ? '' : 's'}`;
    elements.paginationInfo.textContent = `Página ${pagAtual} de ${totalPags} (${total} registro${total === 1 ? '' : 's'})`;
    elements.currentPageLabel.textContent = pagAtual;

    elements.btnPrevPage.disabled = (state.currentPage === 0);
    elements.btnNextPage.disabled = (state.currentPage >= state.totalPages - 1);
}

// Gerenciamento de Estados Visuais (RNF06)
function mostrarEstado(nomeEstado, mensagemErro = '') {
    elements.loadingState.classList.add('hidden');
    elements.emptyState.classList.add('hidden');
    elements.errorState.classList.add('hidden');
    elements.tableContainer.classList.add('hidden');

    if (nomeEstado === 'loading') {
        elements.loadingState.classList.remove('hidden');
        elements.resultsCount.textContent = 'Carregando acervo...';
    } else if (nomeEstado === 'empty') {
        elements.emptyState.classList.remove('hidden');
    } else if (nomeEstado === 'error') {
        elements.errorState.classList.remove('hidden');
        if (mensagemErro) {
            elements.errorDetail.textContent = mensagemErro;
        }
        elements.resultsCount.textContent = 'Erro de carregamento';
        elements.paginationBar.classList.add('hidden');
    } else if (nomeEstado === 'table') {
        elements.tableContainer.classList.remove('hidden');
    }
}

function abrirModal(modal) {
    modal.classList.remove('hidden');
}

function fecharModal(modal) {
    modal.classList.add('hidden');
}

// Status do Backend
function setBackendOnline(isOnline) {
    const dot = elements.connectionStatus.querySelector('.status-dot');
    if (isOnline) {
        dot.className = 'status-dot online';
        elements.apiStatusText.textContent = 'API REST Online';
    } else {
        dot.className = 'status-dot offline';
        elements.apiStatusText.textContent = 'API Offline';
    }
}

// Toasts de Notificação
function mostrarToast(mensagem, tipo = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${tipo}`;
    const icon = tipo === 'success' ? '✅' : tipo === 'warning' ? '⚠️' : 'ℹ️';

    toast.innerHTML = `
        <div style="display: flex; align-items: center; gap: 0.5rem;">
            <span>${icon}</span>
            <span>${escapeHtml(mensagem)}</span>
        </div>
        <button type="button" class="btn-close" style="font-size: 1.1rem;">&times;</button>
    `;

    toast.querySelector('.btn-close').addEventListener('click', () => toast.remove());
    elements.toastContainer.appendChild(toast);

    setTimeout(() => {
        if (toast.parentElement) toast.remove();
    }, 4500);
}

// Formatadores utilitários
function formatarData(dataIso) {
    if (!dataIso) return '—';
    const partes = dataIso.split('-');
    if (partes.length === 3) {
        return `${partes[2]}/${partes[1]}/${partes[0]}`;
    }
    return dataIso;
}

function formatarDataHora(dataHoraIso) {
    if (!dataHoraIso) return '—';
    try {
        const d = new Date(dataHoraIso);
        return d.toLocaleString('pt-BR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    } catch {
        return dataHoraIso;
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function escapeForAttr(str) {
    if (!str) return '';
    return String(str).replace(/'/g, "\\'").replace(/"/g, '&quot;');
}
