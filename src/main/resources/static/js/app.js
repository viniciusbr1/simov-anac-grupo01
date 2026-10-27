'use strict';
const $ = id => document.getElementById(id);
const estado = { page: 0, totalPages: 0, filter: false };
const num = n => Number(n || 0).toLocaleString('pt-BR');
let toastTimeout;
function aviso(mensagem, erro = false) {
  const toast = $('toast'); toast.textContent = mensagem; toast.className = `toast show${erro ? ' error' : ''}`;
  clearTimeout(toastTimeout); toastTimeout = setTimeout(() => toast.className = 'toast', 7000);
}
async function requisicao(url, options = {}) {
  const resposta = await fetch(url, options);
  if (!resposta.ok) {
    let msg = `Erro HTTP ${resposta.status}`;
    try { const corpo = await resposta.json(); msg = corpo.erro || msg; } catch (_) { /* Sem corpo */ }
    throw new Error(msg);
  }
  if (resposta.status === 204) return null;
  return resposta.json();
}
function hora(valor) {
  if (!valor) return '—';
  const data = new Date(valor);
  if (Number.isNaN(data.getTime())) return valor;
  return valor.slice(8, 10) + '/' + valor.slice(5, 7) + '/' + valor.slice(0, 4) + ' ' + valor.slice(11, 16);
}
function celula(linha, texto, css) {
  const td = document.createElement('td'); td.textContent = texto;
  if (css) td.className = css; linha.appendChild(td); return td;
}
function montarTabela(voos) {
  const tbody = $('voos-tbody'); tbody.replaceChildren();
  if (!voos.length) {
    const tr = document.createElement('tr'); const td = celula(tr, 'Nenhum registro encontrado.', 'empty'); td.colSpan = 7; tbody.appendChild(tr); return;
  }
  voos.forEach(voo => {
    const tr = document.createElement('tr');
    celula(tr, String(voo.id)); celula(tr, voo.empresaAerea);
    celula(tr, voo.numeroVoo);
    celula(tr, `${voo.aeroportoOrigem}  →  ${voo.aeroportoDestino}`, 'route');
    celula(tr, hora(voo.partidaPrevista));
    const stat = document.createElement('span'); stat.className = 'pill ' +
      (voo.situacaoVoo === 'REALIZADO' ? 'realizado' : voo.situacaoVoo === 'CANCELADO' ? 'cancelado' : '');
    stat.textContent = voo.situacaoVoo; const coluna = document.createElement('td'); coluna.appendChild(stat); tr.appendChild(coluna);
    const acoes = document.createElement('td');
    const editar = document.createElement('button'); editar.className = 'btn btn-mini btn-secondary'; editar.textContent = 'Editar';
    editar.addEventListener('click', () => editarVoo(voo));
    const excluir = document.createElement('button'); excluir.className = 'btn btn-mini btn-danger'; excluir.textContent = 'Excluir';
    excluir.addEventListener('click', () => excluirVoo(voo));
    acoes.append(editar, excluir); tr.appendChild(acoes); tbody.appendChild(tr);
  });
}
async function carregarTabela() {
  try {
    const query = new URLSearchParams({ page: String(estado.page), size: '20' });
    const isFiltered = estado.filter;
    if (isFiltered) {
      for (const [chave, id] of [['empresa', 'f-empresa'], ['origem', 'f-origem'], ['destino', 'f-destino'], ['situacao', 'f-situacao']]) {
        if ($(id).value.trim()) query.set(chave, $(id).value.trim().toUpperCase());
      }
    }
    const resposta = await requisicao('/api/voos' + (isFiltered ? '/filtro' : '') + '?' + query);
    estado.totalPages = resposta.totalPages;
    montarTabela(resposta.content);
    $('page-desc').textContent = `${num(resposta.totalElements)} registro(s) · Página ${resposta.totalPages ? resposta.number + 1 : 0} de ${resposta.totalPages}`;
    $('prev-page').disabled = estado.page === 0;
    $('next-page').disabled = estado.page + 1 >= estado.totalPages;
  } catch (err) {
    montarTabela([]); $('page-desc').textContent = 'Falha na conexão'; aviso(err.message, true);
  }
}
function desenharGrafico(agrupamento) {
  const corpo = $('chart-bars'); corpo.replaceChildren();
  const pares = Object.entries(agrupamento).sort((a, b) => b[1] - a[1]);
  const maximo = Math.max(1, ...pares.map(x => x[1]));
  if (!pares.length) { corpo.textContent = 'Importe ou cadastre voos para visualizar o gráfico.'; return; }
  for (const [situacao, qtd] of pares) {
    const grupo = document.createElement('div');
    const etiqueta = document.createElement('div'); etiqueta.className = 'chart-label';
    const nome = document.createElement('span'); nome.textContent = situacao;
    const valor = document.createElement('strong'); valor.textContent = num(qtd);
    etiqueta.append(nome, valor);
    const trilho = document.createElement('div'); trilho.className = 'chart-track';
    const barra = document.createElement('div'); barra.className = 'chart-fill ' + (situacao === 'CANCELADO' ? 'cancelado' : situacao === 'NAO INFORMADO' ? 'nao' : '');
    barra.style.width = `${(qtd / maximo * 100).toFixed(1)}%`; barra.title = `${num(qtd)} operações`;
    trilho.appendChild(barra); grupo.append(etiqueta, trilho); corpo.appendChild(grupo);
  }
}
async function carregarEstatisticas() {
  try {
    const dados = await requisicao('/api/voos/estatisticas');
    const total = Object.values(dados).reduce((s, v) => s + v, 0);
    $('metric-total').textContent = num(total);
    $('metric-realizados').textContent = num(dados.REALIZADO);
    $('metric-cancelados').textContent = num(dados.CANCELADO);
    $('metric-taxa').textContent = total ? ((dados.CANCELADO || 0) / total * 100).toFixed(1).replace('.', ',') + '%' : '0%';
    desenharGrafico(dados);
  } catch (err) { aviso('Indicadores: ' + err.message, true); }
}
async function atualizarTela() { await Promise.all([carregarTabela(), carregarEstatisticas()]); }
function limparForm() {
  $('voo-form').reset(); $('edit-id').value = ''; $('form-title').textContent = 'Cadastrar voo';
  $('submit-voo').textContent = 'Salvar voo';
}
function editarVoo(voo) {
  $('edit-id').value = voo.id; $('form-title').textContent = 'Editar voo #' + voo.id;
  const map = { empresa: 'empresaAerea', numero: 'numeroVoo', origem: 'aeroportoOrigem', destino: 'aeroportoDestino', di: 'codigoDi', tipo: 'codigoTipoLinha', situacao: 'situacaoVoo', 'partida-prevista': 'partidaPrevista', 'partida-real': 'partidaReal', 'chegada-prevista': 'chegadaPrevista', 'chegada-real': 'chegadaReal' };
  Object.entries(map).forEach(([id, chave]) => $(id).value = voo[chave] || '');
  $('submit-voo').textContent = 'Salvar alterações'; $('voo-form').scrollIntoView({ behavior: 'smooth', block: 'start' });
}
async function excluirVoo(voo) {
  if (!confirm(`Excluir o voo #${voo.id} (${voo.numeroVoo})?`)) return;
  try {
    await requisicao(`/api/voos/${voo.id}`, { method: 'DELETE' });
    aviso('Voo excluído.'); await atualizarTela();
  } catch (err) { aviso(err.message, true); }
}
$('voo-form').addEventListener('submit', async evento => {
  evento.preventDefault();
  const payload = {
    empresaAerea: $('empresa').value.trim().toUpperCase(), numeroVoo: $('numero').value.trim().toUpperCase(),
    aeroportoOrigem: $('origem').value.trim().toUpperCase(), aeroportoDestino: $('destino').value.trim().toUpperCase(),
    codigoDi: $('di').value.trim().toUpperCase(), codigoTipoLinha: $('tipo').value.trim().toUpperCase(),
    partidaPrevista: $('partida-prevista').value || null, partidaReal: $('partida-real').value || null,
    chegadaPrevista: $('chegada-prevista').value || null, chegadaReal: $('chegada-real').value || null,
    situacaoVoo: $('situacao').value
  };
  if (!payload.partidaPrevista && !payload.partidaReal) return aviso('Informe uma partida prevista ou real.', true);
  const id = $('edit-id').value;
  try {
    await requisicao(id ? `/api/voos/${id}` : '/api/voos', {
      method: id ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
    });
    aviso(id ? 'Voo atualizado com sucesso.' : 'Voo cadastrado com sucesso.');
    limparForm(); estado.page = 0; await atualizarTela();
  } catch (err) { aviso(err.message, true); }
});
$('cancel-edit').addEventListener('click', limparForm);
$('filter-form').addEventListener('submit', evento => { evento.preventDefault(); estado.filter = true; estado.page = 0; carregarTabela(); });
$('clear-filters').addEventListener('click', () => { $('filter-form').reset(); estado.filter = false; estado.page = 0; carregarTabela(); });
$('buscar-id').addEventListener('click', async () => {
  const id = $('busca-id').value;
  if (!/^\d+$/.test(id) || Number(id) < 1) return aviso('Informe um ID válido.', true);
  try {
    const voo = await requisicao(`/api/voos/${id}`);
    montarTabela([voo]); $('page-desc').textContent = 'Busca individual por ID #' + id;
    aviso('Registro encontrado.');
  } catch (err) { montarTabela([]); $('page-desc').textContent = 'Busca individual'; aviso(err.message, true); }
});
$('import-form').addEventListener('submit', async evento => {
  evento.preventDefault();
  const arquivo = $('arquivo').files[0]; if (!arquivo) return aviso('Selecione um arquivo CSV.', true);
  const corpo = new FormData(); corpo.append('arquivo', arquivo);
  $('import-button').disabled = true; $('import-result').textContent = 'Lendo e validando o arquivo. Não feche esta página...';
  try {
    const r = await requisicao('/api/voos/importar', { method: 'POST', body: corpo });
    $('import-result').textContent = `Lidas: ${num(r.linhasLidas)} | Válidas únicas: ${num(r.validas)}\nImportadas: ${num(r.importadas)} | Rejeitadas: ${num(r.rejeitadas)}\nDuplicadas: ${num(r.duplicadas)} | Fora da amostra: ${num(r.ignoradasPorAmostragem)}\n${r.politicaAmostragem}`;
    aviso(r.mensagem); estado.page = 0; await atualizarTela();
  } catch (err) { $('import-result').textContent = err.message; aviso(err.message, true); }
  finally { $('import-button').disabled = false; }
});
$('reload').addEventListener('click', atualizarTela);
$('prev-page').addEventListener('click', () => { if (estado.page > 0) { estado.page--; carregarTabela(); } });
$('next-page').addEventListener('click', () => { if (estado.page + 1 < estado.totalPages) { estado.page++; carregarTabela(); } });
atualizarTela();
