package br.edu.fatec.simov.service;

import br.edu.fatec.simov.model.Voo;
import br.edu.fatec.simov.repository.VooRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class VooService {
    private final VooRepository repository;
    private final Validator validator;

    public VooService(VooRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public Page<Voo> listar(Pageable pageable) { return repository.findAll(pageable); }
    public Optional<Voo> buscar(Long id) { return repository.findById(id); }

    public Page<Voo> filtrar(String empresa, String origem, String destino, String situacao, Pageable pageable) {
        Specification<Voo> filtro = (raiz, consulta, cb) -> cb.conjunction();
        if (preenchido(empresa)) filtro = filtro.and((r, q, cb) -> cb.equal(r.get("empresaAerea"), limpa(empresa)));
        if (preenchido(origem)) filtro = filtro.and((r, q, cb) -> cb.equal(r.get("aeroportoOrigem"), limpa(origem)));
        if (preenchido(destino)) filtro = filtro.and((r, q, cb) -> cb.equal(r.get("aeroportoDestino"), limpa(destino)));
        if (preenchido(situacao)) filtro = filtro.and((r, q, cb) -> cb.equal(r.get("situacaoVoo"), situacaoNormalizada(situacao)));
        return repository.findAll(filtro, pageable);
    }

    public Voo cadastrar(Voo voo) {
        preparar(voo);
        if (repository.findByChaveOrigem(voo.getChaveOrigem()).isPresent()) {
            throw new IllegalArgumentException("Esta operacao ja foi cadastrada.");
        }
        voo.setId(null);
        return repository.save(voo);
    }

    public Optional<Voo> atualizar(Long id, Voo enviado) {
        preparar(enviado);
        return repository.findById(id).map(atual -> {
            var outro = repository.findByChaveOrigem(enviado.getChaveOrigem());
            if (outro.isPresent() && !outro.get().getId().equals(id)) {
                throw new IllegalArgumentException("Ja existe outro registro com esses dados.");
            }
            atual.setEmpresaAerea(enviado.getEmpresaAerea());
            atual.setNumeroVoo(enviado.getNumeroVoo());
            atual.setCodigoDi(enviado.getCodigoDi());
            atual.setCodigoTipoLinha(enviado.getCodigoTipoLinha());
            atual.setAeroportoOrigem(enviado.getAeroportoOrigem());
            atual.setAeroportoDestino(enviado.getAeroportoDestino());
            atual.setPartidaPrevista(enviado.getPartidaPrevista());
            atual.setPartidaReal(enviado.getPartidaReal());
            atual.setChegadaPrevista(enviado.getChegadaPrevista());
            atual.setChegadaReal(enviado.getChegadaReal());
            atual.setSituacaoVoo(enviado.getSituacaoVoo());
            atual.setChaveOrigem(enviado.getChaveOrigem());
            return repository.save(atual);
        });
    }

    public boolean excluir(Long id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    public Map<String, Long> contarPorSituacao() {
        Map<String, Long> contagem = new LinkedHashMap<>();
        for (Object[] grupo : repository.contarPorSituacao()) {
            contagem.put((String) grupo[0], (Long) grupo[1]);
        }
        return contagem;
    }

    public Voo preparar(Voo voo) {
        voo.setEmpresaAerea(limpa(voo.getEmpresaAerea()));
        voo.setNumeroVoo(limpa(voo.getNumeroVoo()));
        voo.setCodigoDi(limpa(voo.getCodigoDi()));
        voo.setCodigoTipoLinha(limpa(voo.getCodigoTipoLinha()));
        voo.setAeroportoOrigem(limpa(voo.getAeroportoOrigem()));
        voo.setAeroportoDestino(limpa(voo.getAeroportoDestino()));
        voo.setSituacaoVoo(situacaoNormalizada(voo.getSituacaoVoo()));
        String erros = validator.validate(voo).stream().map(this::erro)
            .collect(Collectors.joining("; "));
        if (!erros.isEmpty()) throw new IllegalArgumentException(erros);
        if (voo.getPartidaPrevista() == null && voo.getPartidaReal() == null) {
            throw new IllegalArgumentException("Informe partida prevista ou partida real.");
        }
        voo.setChaveOrigem(gerarChave(voo));
        return voo;
    }

    private String erro(ConstraintViolation<Voo> erro) {
        return erro.getPropertyPath() + ": " + erro.getMessage();
    }
    private static boolean preenchido(String v) { return v != null && !v.isBlank(); }
    private static String limpa(String v) { return v == null ? "" : v.trim().toUpperCase(java.util.Locale.ROOT); }
    public static String situacaoNormalizada(String v) {
        if (v == null) return "";
        return Normalizer.normalize(limpa(v), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").replaceAll("\\s+", " ");
    }

    private static String gerarChave(Voo v) {
        String dados = String.join("|", v.getEmpresaAerea(), v.getNumeroVoo(),
            v.getCodigoDi(), v.getCodigoTipoLinha(), v.getAeroportoOrigem(),
            v.getAeroportoDestino(), String.valueOf(v.getPartidaPrevista()),
            String.valueOf(v.getPartidaReal()));
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(dados.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponivel", ex);
        }
    }
}
