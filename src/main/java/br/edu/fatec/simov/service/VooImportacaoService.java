package br.edu.fatec.simov.service;

import br.edu.fatec.simov.dto.ImportacaoResultado;
import br.edu.fatec.simov.model.Voo;
import br.edu.fatec.simov.repository.VooRepository;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.CodingErrorAction;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VooImportacaoService {
    private static final List<DateTimeFormatter> FORMATOS = Arrays.asList(
        DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT),
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
    );
    private final VooRepository repository;
    private final VooService voos;
    private final int limite;

    public VooImportacaoService(VooRepository repository, VooService voos,
            @Value("${simov.importacao.limite:1200}") int limite) {
        this.repository = repository;
        this.voos = voos;
        this.limite = Math.max(500, Math.min(10000, limite));
    }

    public ImportacaoResultado importar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Selecione um arquivo CSV nao vazio.");
        }
        if (arquivo.getOriginalFilename() == null ||
                !arquivo.getOriginalFilename().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new IllegalArgumentException("Extensao invalida: envie um arquivo .csv.");
        }
        long lidas = 0, validas = 0, rejeitadas = 0, duplicadas = 0;
        Set<String> chavesLidas = new HashSet<>();
        List<Voo> amostra = new ArrayList<>();
        Random sorteio = new Random(20261024L); // amostragem reproduzivel
        try (BufferedInputStream entrada = new BufferedInputStream(arquivo.getInputStream())) {
            Charset charset = detectarCharset(entrada);
            try (InputStreamReader leitor = new InputStreamReader(entrada, charset);
                 CSVParser csv = CSVFormat.DEFAULT.builder()
                      .setDelimiter(';').setHeader().setSkipHeaderRecord(true)
                      .setIgnoreSurroundingSpaces(true).setTrim(true)
                      .setIgnoreEmptyLines(true).get().parse(leitor)) {
                Map<String, String> colunas = reconhecerColunas(csv.getHeaderMap().keySet());
                for (CSVRecord linha : csv) {
                    lidas++;
                    try {
                        Voo voo = converter(linha, colunas);
                        voos.preparar(voo);
                        if (!chavesLidas.add(voo.getChaveOrigem())) {
                            duplicadas++;
                            continue;
                        }
                        validas++;
                        if (amostra.size() < limite) {
                            amostra.add(voo);
                        } else {
                            long posicao = sorteio.nextLong(validas);
                            if (posicao < limite) amostra.set((int) posicao, voo);
                        }
                    } catch (IllegalArgumentException | IllegalStateException ex) {
                        rejeitadas++;
                    }
                }
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Erro ao ler CSV (confira arquivo, codificacao e delimitador): " + ex.getMessage());
        }
        if (lidas == 0) throw new IllegalArgumentException("Arquivo sem linhas de dados.");
        if (validas == 0) throw new IllegalArgumentException("Nenhuma linha valida: revise o formato e o conteudo do CSV.");

        List<String> chaves = amostra.stream().map(Voo::getChaveOrigem).toList();
        Set<String> presentes = repository.findAllByChaveOrigemIn(chaves).stream()
            .map(Voo::getChaveOrigem).collect(Collectors.toSet());
        List<Voo> novos = amostra.stream()
            .filter(v -> !presentes.contains(v.getChaveOrigem())).toList();
        repository.saveAll(novos);
        duplicadas += presentes.size();
        long ignoradas = validas - amostra.size();
        String mensagem = "Importacao concluida. " + novos.size() + " registros gravados no Neon.";
        return new ImportacaoResultado(lidas, validas, novos.size(), rejeitadas,
            duplicadas, ignoradas, mensagem,
            "Reservoir sampling com semente fixa; ate " + limite + " linhas validas unicas. " +
            "Descartes por amostragem nao sao registros invalidos.");
    }

    private static Charset detectarCharset(BufferedInputStream entrada) throws IOException {
        entrada.mark(8192);
        byte[] inicio = entrada.readNBytes(4096);
        entrada.reset();
        try {
            StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(inicio));
            return StandardCharsets.UTF_8;
        } catch (CharacterCodingException ex) {
            return Charset.forName("windows-1252");
        }
    }

    private static Map<String, String> reconhecerColunas(Set<String> cabecalhos) {
        Map<String, String> alias = Map.ofEntries(
            Map.entry("sigla icao empresa aerea", "empresa"),
            Map.entry("numero voo", "numero"),
            Map.entry("codigo di", "di"),
            Map.entry("codigo ci", "di"),
            Map.entry("codigo tipo linha", "tipo"),
            Map.entry("sigla icao aeroporto origem", "origem"),
            Map.entry("sigla icao aeroporto destino", "destino"),
            Map.entry("partida prevista", "prevista"),
            Map.entry("partida real", "real"),
            Map.entry("chegada prevista", "chegadaPrevista"),
            Map.entry("chegada real", "chegadaReal"),
            Map.entry("situacao voo", "situacao"),
            Map.entry("situacao do voo", "situacao")
        );
        Map<String, String> identificadas = new HashMap<>();
        for (String nomeOriginal : cabecalhos) {
            String campo = alias.get(normalizarCabecalho(nomeOriginal));
            if (campo != null) identificadas.put(campo, nomeOriginal);
        }
        for (String obrigatoria : List.of("empresa", "numero", "origem", "destino", "situacao")) {
            if (!identificadas.containsKey(obrigatoria))
                throw new IllegalArgumentException("Cabecalho invalido; falta a coluna " + obrigatoria +
                    ". Use o CSV oficial VRA da ANAC separado por ponto e virgula.");
        }
        if (!identificadas.containsKey("prevista") && !identificadas.containsKey("real"))
            throw new IllegalArgumentException("Cabecalho invalido; falta Partida Prevista ou Partida Real.");
        return identificadas;
    }

    private static String normalizarCabecalho(String valor) {
        return Normalizer.normalize(valor.replace("\uFEFF", ""), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", " ").trim().replaceAll("\\s+", " ");
    }

    private static String campo(CSVRecord linha, Map<String, String> indices, String atributo) {
        String chave = indices.get(atributo);
        return chave == null || !linha.isMapped(chave) || !linha.isSet(chave)
            ? "" : linha.get(chave).trim();
    }

    private static Voo converter(CSVRecord linha, Map<String, String> colunas) {
        Voo v = new Voo();
        v.setEmpresaAerea(campo(linha, colunas, "empresa"));
        v.setNumeroVoo(campo(linha, colunas, "numero"));
        v.setCodigoDi(campo(linha, colunas, "di"));
        v.setCodigoTipoLinha(campo(linha, colunas, "tipo"));
        v.setAeroportoOrigem(campo(linha, colunas, "origem"));
        v.setAeroportoDestino(campo(linha, colunas, "destino"));
        v.setPartidaPrevista(data(campo(linha, colunas, "prevista")));
        v.setPartidaReal(data(campo(linha, colunas, "real")));
        v.setChegadaPrevista(data(campo(linha, colunas, "chegadaPrevista")));
        v.setChegadaReal(data(campo(linha, colunas, "chegadaReal")));
        v.setSituacaoVoo(campo(linha, colunas, "situacao"));
        return v;
    }

    private static LocalDateTime data(String valor) {
        if (valor == null || valor.isBlank()) return null;
        for (DateTimeFormatter formato : FORMATOS) {
            try { return LocalDateTime.parse(valor, formato); }
            catch (DateTimeParseException ignorar) { /* proximo formato */ }
        }
        throw new IllegalArgumentException("Data/hora invalida: " + valor);
    }
}
