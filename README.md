# SIMOV ANAC — Grupo 01

**FATEC Ipiranga — Análise e Desenvolvimento de Sistemas (noturno) — Programação Avançada Orientada a Objetos (POO II) — Projeto P2**

Integrantes:
- Giovanne Crispim de Lima
- Gustavo Silva Paulino
- Vinicius Carvalho Sanchez
- Vinicius Guedes Soriano Pereira

**Entrega:** 31/10/2026, até 23h59, pelo Teams (PDF técnico com link do GitHub e commit final).
**Apresentação:** 07/11/2026.
**Docente:** Profa. Me. Sirley Ambrosia Vitorio Addão.

> **Estado verificado em 26/10/2026:** aplicação iniciada com Spring Boot e Neon, importação do VRA de setembro/2026 executada, interface e API REST testadas via navegador e Insomnia. O comando `mvn clean test` registrou **6 testes, 0 falhas, 0 erros e BUILD SUCCESS**. As capturas reais estão em `documentacao/`. O número de registros pode variar com cadastros e exclusões de teste.

## Objetivo

Aplicação web acadêmica para importar, armazenar, consultar, cadastrar, editar, excluir, filtrar e exibir graficamente voos do conjunto oficial VRA da Agência Nacional de Aviação Civil (ANAC). Não é produto oficial da ANAC.

## Dados oficiais

- Órgão: Agência Nacional de Aviação Civil (ANAC)
- Dataset: Voo Regular Ativo (VRA), atualização mensal segundo metadados.
- Diretório público: https://siros.anac.gov.br/siros/registros/diversos/vra/2026/
- Arquivo de referência recomendado: https://siros.anac.gov.br/siros/registros/diversos/vra/2026/VRA_2026_09.csv
- Metadados: https://www.anac.gov.br/acesso-a-informacao/dados-abertos/areas-de-atuacao/voos-e-operacoes-aereas/voo-regular-ativo-vra/62-voo-regular-ativo-vra
- Download do CSV e execução da importação pelo grupo: **26/10/2026**.
- **Arquivo bruto não versionado:** obter diretamente no portal ANAC/SIROS. O arquivo de setembro/2026 foi importado pelo grupo no ambiente local. A licença individual deve ser verificada na publicação oficial; a fonte é creditada no relatório.
- **Resultado observado em 26/10/2026:** 85.845 linhas lidas, 85.839 válidas únicas, 6 duplicadas, 0 rejeitadas, 1.200 importadas, 84.639 válidas fora da amostra. Os registros fora da amostra não são inválidos.

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

Os testes cobrem normalização, validação, ID inexistente, importação com rejeições e duplicidade, e cabeçalho inválido. Em **26/10/2026**, o grupo executou `mvn clean test`: **6 testes executados, 0 falhas, 0 erros, 0 ignorados, BUILD SUCCESS**. A validação integrada do Neon e do frontend foi realizada manualmente, conforme capturas, não como suíte automatizada end-to-end.

## Evidências reais da execução (26/10/2026)

As capturas foram feitas pelo grupo durante os testes no navegador, no SQL Editor do Neon e no Insomnia. Consulte a pasta `documentacao/` e o relatório técnico para as legendas.

| Teste observado | Resultado apresentado |
|---|---|
| Interface e persistência | Formulário cadastra voo; o registro permanece após atualização F5 e é consultável no Neon |
| Importação ANAC VRA | 85.845 linhas lidas; 1.200 voos gravados; 6 duplicatas; 0 registros rejeitados |
| Visualização por situação | Consulta SQL no Neon exibiu 1.159 REALIZADO e 43 CANCELADO na captura anterior à exclusão de um registro; o gráfico posterior mostra 1.158 e 43 |
| GET listar e GET por ID | HTTP 200; JSON de voos |
| GET ID inexistente | HTTP 404 |
| POST cadastro | HTTP 201; voo de teste ID 1203 |
| PUT atualização | HTTP 200; voo de teste atualizado de T9001 para T9002 |
| DELETE exclusão | HTTP 204; GET posterior do ID 1203 retorna 404 |
| Filtros de empresa e origem | HTTP 200; empresa AZU e aeroporto SBGR |
| Importação de CSV inválido | HTTP 400; rejeição de cabeçalho incompatível |

> **Amostragem:** o universo completo de 85.845 linhas não foi persistido. A taxa e as quantidades do gráfico são relativas à amostra no banco no instante de cada captura. Os voos criados para demonstração são fictícios, distintos dos dados oficiais importados.

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

Os endpoints foram testados pelo grupo no Insomnia em 26/10/2026; as respostas HTTP estão documentadas nas evidências da pasta `documentacao/`. `TESTES_API.http` também contém exemplos para REST Client do VS Code.

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

**Justificativa:** permitir demonstração confiável com centenas de operações reais sem gravar centenas de milhares de linhas em banco gratuito. Amostragem por reservatório reduz viés de escolher só o início do arquivo e pode ser reproduzida. Na execução documentada, **1.200 voos válidos** foram importados e consultados via PostgreSQL no Neon, superando o mínimo de 500 registros da atividade.

## Regras e modelagem

A entidade `Voo` tem identificador `id` (Long autogerado) e 11 atributos do domínio (empresaAerea, numeroVoo, codigoDi, codigoTipoLinha, aeroportoOrigem, aeroportoDestino, partidaPrevista, partidaReal, chegadaPrevista, chegadaReal, situacaoVoo), além de `chaveOrigem` técnica para duplicidades.

A modelagem persiste os onze campos operacionais relevantes selecionados do CSV VRA e uma chave técnica de deduplicação. Nomes completos de companhias e aeroportos não são armazenados separadamente: os códigos ICAO são usados nas consultas e nos filtros.

Os quatro pacotes exigidos estão presentes. O Controller só recebe/repassa requisições; Service valida/importa/normaliza; Repository conversa com banco via `JpaRepository` e `JpaSpecificationExecutor`; Model mapeia dados com JPA. As buscas usam `Optional`; os endpoints devolvem `ResponseEntity`.

## GitHub e versão do grupo

Repositório público: **https://github.com/viniciusbr1/simov-anac-grupo01**
Branch: **main**
Primeiro commit registrado em 26/10/2026: **`fd854bc`**. Este é o commit inicial, não necessariamente o último commit de entrega.

O projeto já foi inicializado e publicado. Para atualizar o README e acrescentar evidências, **não execute outro `git init`**. No terminal aberto dentro de `SIMOV_ANAC_Grupo01`, use:

```powershell
git status
git add README.md documentacao/Evidencia_00_Pagina_Inicial_Tabela.png
git commit -m "docs: atualiza README e evidencia complementar"
git push origin main
git rev-parse --short HEAD
```

O código retornado pelo último comando identifica o **novo commit**, que deve ser mencionado no PDF entregue pelo Teams. Não adicionar arquivos secretos: `.env` e `target/` estão ignorados no `.gitignore`; nunca utilizar `git add -f .env`.

## Checklist final da entrega

- [x] Pesquisar o conjunto oficial VRA, documentação e arquivo de setembro de 2026.
- [x] Importar arquivo oficial com contadores e amostra programática de 1.200 registros.
- [x] Persistir no PostgreSQL Neon e comprovar via SQL Editor.
- [x] Executar 6 testes Maven, com BUILD SUCCESS, e testar API pelo Insomnia.
- [x] Testar CRUD, filtros, erro 404, importação válida e inválida, gráfico e atualização da página.
- [x] Guardar as capturas reais na pasta `documentacao/`.
- [x] Publicar o repositório no GitHub sem o arquivo `.env`.
- [ ] Atualizar e publicar este README e a imagem complementar no GitHub.
- [ ] Confirmar o hash do último commit e registrá-lo no relatório PDF.
- [ ] Revisar o PDF com as 13 seções, figuras legíveis, link clicável do GitHub e título da FATEC Ipiranga.
- [ ] Enviar **um único PDF** no Teams, atividade `Entrega Relatório dos Projetos-P2`, até **31/10/2026 às 23h59**.
- [ ] Preparar os quatro integrantes para a demonstração oral em 07/11/2026.
