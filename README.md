# SIMOV ANAC — Grupo 01

**FATEC — Programação Avançada Orientada a Objetos (POO II) — Projeto P2**

Integrantes:
- Giovanne Crispim de Lima
- Gustavo Silva Paulino
- Vinicius Carvalho Sanchez
- Vinicius Guedes Soriano Pereira

**Entrega:** 31/10/2026 (PDF técnico + link GitHub + commit final).

**Apresentação:** 07/11/2026.

**Docente:** Profa. Me. Sirley Ambrosia Vitorio Addão.

> **Situação deste pacote:** implementação inicial completa em arquivos; a compilação Java, a execução integrada contra o Neon, a importação do CSV oficial e a captura das evidências ainda precisam ser realizadas no computador do grupo. Não confundir código entregue com testes executados.

## Objetivo

Aplicação web acadêmica para importar, armazenar, consultar, cadastrar, editar, excluir, filtrar e exibir graficamente voos do conjunto oficial VRA da Agência Nacional de Aviação Civil (ANAC). Não é produto oficial da ANAC.

## Dados oficiais

- Órgão: Agência Nacional de Aviação Civil (ANAC)
- Dataset: Voo Regular Ativo (VRA), atualização mensal segundo metadados.
- Diretório público: https://siros.anac.gov.br/siros/registros/diversos/vra/2026/
- Arquivo de referência recomendado: https://siros.anac.gov.br/siros/registros/diversos/vra/2026/VRA_2026_09.csv
- Metadados: https://www.anac.gov.br/acesso-a-informacao/dados-abertos/areas-de-atuacao/voos-e-operacoes-aereas/voo-regular-ativo-vra/62-voo-regular-ativo-vra
- Data de consulta aos links: 24/10/2026.
- **Atenção:** o arquivo bruto não acompanha o pacote; o servidor de dados não permitiu download direto neste ambiente. Antes da entrega, verificar codificação, separador, cabeçalhos reais, quantidades, tamanho, licença e resultados de importação. Não declarar números ainda não medidos.

## Tecnologias

Java 21, Spring Boot 3.5.16, Spring Web, Spring Data JPA, Bean Validation, Apache Commons CSV 1.14.1, PostgreSQL (Neon), HTML, CSS, JavaScript nativo (fetch). Maven para build.

## Estrutura

```text
src/main/java/br/edu/fatec/simov/
  SimovApplication.java
  model/Voo.java
  repository/VooRepository.java
  service/VooService.java
  service/VooImportacaoService.java
  controller/VooController.java
  dto/ImportacaoResultado.java
  exception/ApiExceptionHandler.java
src/main/resources/
  application.properties
  static/index.html
  static/css/style.css
  static/js/app.js
src/test/.../service/
  VooServiceTest.java
  VooImportacaoServiceTest.java
```

## Pré-requisitos

1. JDK 21, `java -version` e `javac -version` funcionando.
2. Apache Maven 3.6.3+ disponível em `mvn -version` (recomendado Maven 3.9.x).
3. VS Code com *Extension Pack for Java* e *Spring Boot Extension Pack*.
4. Conta gratuita no Neon com projeto PostgreSQL e string JDBC de conexão, usuário e senha.
5. Conexão com internet na primeira execução para Maven baixar dependências e para acessar Neon.

## Configurar PostgreSQL no Neon

1. Criar projeto em https://neon.tech e copiar os dados de conexão em **Connect**.
2. Converter a URL PostgreSQL para JDBC: `jdbc:postgresql://HOST/neondb?sslmode=require`. Caso o Neon exiba outra base, usar aquela base, host e porta informados. **Não colar a senha na URL**.
3. Copiar `.env.example` para `.env` e substituir `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
4. **Nunca colocar credenciais em GitHub, print, relatório nem README.**
5. Ao iniciar, o Hibernate criará/atualizará a tabela `voos` (recurso acadêmico `ddl-auto=update`). Em produção, usar migrações.

### Executar no Windows / VS Code

Na pasta do projeto, abrir terminal PowerShell:

```powershell
Copy-Item .env.example .env
notepad .env
mvn -version
powershell -ExecutionPolicy Bypass -File .\iniciar.ps1
```

O script `iniciar.ps1` lê localmente as variáveis do `.env` para o processo e executa `mvn spring-boot:run`. O próprio Spring Boot serve a interface em **http://localhost:8080**. Não abra o HTML por `file://` e não use Live Server: ele deve consumir a API no mesmo servidor.

Como alternativa, configurar manualmente `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` como variáveis de ambiente e executar `mvn spring-boot:run`.

### Testes automatizados

```powershell
mvn test
mvn clean package
```

Os testes cobrem normalização, validação, ID inexistente, importação com rejeições e duplicidade, e cabeçalho inválido. **A execução desses comandos é tarefa pendente:** este pacote não afirma que já passaram.

## Interface e funcionalidades

- Indicadores gerais e gráfico de contagem de voos por situação.
- Formulário de cadastro e edição.
- Tabela paginada e exclusão com confirmação.
- Busca por ID (incluindo retorno 404 quando inexistente).
- Filtros por empresa aérea, origem, destino e situação.
- Importação por `multipart/form-data` a partir do navegador.
- Feedback sobre linhas lidas, válidas únicas, importadas, rejeitadas, duplicadas e fora da amostra.
- Persistência em PostgreSQL Neon.

## API REST

| Método | Endpoint | Resultado esperado |
|---|---|---|
| GET | `/api/voos?page=0&size=20` | 200 — página de voos |
| GET | `/api/voos/{id}` | 200 ou 404 |
| POST | `/api/voos` | 201 ou 400; 409 se condição de unicidade no banco |
| PUT | `/api/voos/{id}` | 200, 400 ou 404 |
| DELETE | `/api/voos/{id}` | 204 ou 404 |
| GET | `/api/voos/filtro?empresa=AZU&origem=SBGR` | 200 |
| POST | `/api/voos/importar` | 200 ou 400 — multipart campo `arquivo` |
| GET | `/api/voos/estatisticas` | 200 — `{ "REALIZADO": 10, "CANCELADO": 2 }` |

Exemplo de JSON **de teste**, não extraído da ANAC:

```json
{
  "empresaAerea": "AZU",
  "numeroVoo": "9999",
  "codigoDi": "0",
  "codigoTipoLinha": "N",
  "aeroportoOrigem": "SBGR",
  "aeroportoDestino": "SBRJ",
  "partidaPrevista": "2026-09-10T18:00:00",
  "partidaReal": null,
  "chegadaPrevista": "2026-09-10T19:00:00",
  "chegadaReal": null,
  "situacaoVoo": "CANCELADO"
}
```

Para testar no Insomnia, importar manualmente estes endpoints e capturar telas para o relatório. `TESTES_API.http` também contém exemplos para REST Client do VS Code.

## Tratamentos da importação

1. Exige CSV não vazio e cabeçalho reconhecido; separador `;`.
2. Inspeciona o começo do arquivo para estimar UTF-8 ou Windows-1252, padrão comum nos dados legados.
3. Reconhece títulos das colunas independentemente de maiúsculas, acentos e diferenças comuns (Código DI/CI; Situação Voo/do voo).
4. Elimina espaços extras nas extremidades, transforma códigos ICAO e situações em maiúsculas, remove acentos do campo de situação e converte datas em `LocalDateTime`.
5. Valida empresa, número do voo, aeroportos de origem/destino, situação e necessidade de partida prevista ou real.
6. Descarta registro individual inválido e contabiliza sua rejeição.
7. Gera chave SHA-256 baseada nos atributos de identificação; repetidos no arquivo e no banco não entram novamente.
8. Percorre **todo o arquivo** e seleciona uma amostra reproduzível de até 1200 operações válidas únicas usando *reservoir sampling* e semente fixa; a amostra não é manual. Pode ajustar `LIMITE_AMOSTRA` (mínimo 500, máximo 10000).
9. Diferencia rejeitados, duplicados e válidos fora da amostra. Relata os quantitativos no resultado da importação.
10. Um erro estrutural de cabeçalho/codificação/formato aborta a operação com mensagem; se todas as linhas forem inválidas, não grava nada.

**Justificativa:** permitir demonstração confiável com centenas de operações reais sem gravar centenas de milhares de linhas em banco gratuito. Amostragem por reservatório reduz viés de escolher só o início do arquivo e pode ser reproduzida. Antes de afirmar conformidade total, confirmar que há **pelo menos 500 registros válidos efetivamente importados** no Neon após o teste real.

## Regras e modelagem

A entidade `Voo` tem identificador `id` (Long autogerado) e 11 atributos do domínio (empresaAerea, numeroVoo, codigoDi, codigoTipoLinha, aeroportoOrigem, aeroportoDestino, partidaPrevista, partidaReal, chegadaPrevista, chegadaReal, situacaoVoo), além de `chaveOrigem` técnica para duplicidades.

Colunas descritivas não usadas (como nome da empresa e descrição completa do aeroporto, se presentes no arquivo) são descartadas porque esta primeira versão foca identificação, cronologia e situação de voo. Essa decisão deve ser validada com o arquivo selecionado.

Os quatro pacotes exigidos estão presentes. O Controller só recebe/repassa requisições; Service valida/importa/normaliza; Repository conversa com banco via `JpaRepository` e `JpaSpecificationExecutor`; Model mapeia dados com JPA. As buscas usam `Optional`; os endpoints devolvem `ResponseEntity`.

## GitHub

No VS Code, terminal na pasta do projeto (troque `<URL_DO_REPOSITORIO>`):

```bash
git init
git branch -M main
git add .
git commit -m "feat: estrutura inicial SIMOV ANAC e CRUD"
git remote add origin <URL_DO_REPOSITORIO>
git push -u origin main
```

Se o repositório remoto tiver commits prévios, alinhar o histórico antes de dar `push`. Cada integrante deve fazer commits reais e saber explicar sua contribuição. **Nunca rode `git add -f .env`**.

## Pendências para entrega

- [ ] Baixar arquivo VRA_2026_09.csv do portal oficial no computador do grupo.
- [ ] Conferir nome exato das colunas, codificação, separador, datas e valores, adaptando o importador se necessário.
- [ ] Confirmar licença de reutilização e volume/linhas reais do arquivo.
- [ ] Criar Neon, configurar variáveis de ambiente e rodar a aplicação.
- [ ] Executar `mvn test` e corrigir qualquer incompatibilidade encontrada.
- [ ] Testar CRUD, 404, 4 filtros, CSV válido, CSV inválido, atualização e persistência após refresh.
- [ ] Confirmar 500+ registros oficiais válidos persistidos no Neon.
- [ ] Tirar evidências legíveis do site, do Insomnia, do Neon e do gráfico.
- [ ] Completar as 13 seções do relatório com resultados obtidos, números reais, prints, licença e justificativas.
- [ ] Inserir URL pública do GitHub e hash do commit final no PDF.
- [ ] Todos os quatro integrantes ensaiarem as explicações.
