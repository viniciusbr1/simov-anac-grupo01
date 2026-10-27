package br.edu.fatec.simov.controller;

import br.edu.fatec.simov.dto.ImportacaoResultado;
import br.edu.fatec.simov.model.Voo;
import br.edu.fatec.simov.service.VooImportacaoService;
import br.edu.fatec.simov.service.VooService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/voos")
public class VooController {
    private final VooService servico;
    private final VooImportacaoService importacao;

    public VooController(VooService servico, VooImportacaoService importacao) {
        this.servico = servico;
        this.importacao = importacao;
    }

    private PageRequest pagina(int page, int size) {
        return PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size)),
                Sort.by(Sort.Direction.DESC, "id"));
    }

    @GetMapping
    public ResponseEntity<Page<Voo>> listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(servico.listar(pagina(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Voo> buscar(@PathVariable Long id) {
        return servico.buscar(id).map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Voo> cadastrar(@Valid @RequestBody Voo voo) {
        Voo salvo = servico.cadastrar(voo);
        return ResponseEntity.created(URI.create("/api/voos/" + salvo.getId())).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Voo> atualizar(@PathVariable Long id, @Valid @RequestBody Voo voo) {
        return servico.atualizar(id, voo).map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        return servico.excluir(id) ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }

    @GetMapping("/filtro")
    public ResponseEntity<Page<Voo>> filtrar(@RequestParam(required = false) String empresa,
          @RequestParam(required = false) String origem,
          @RequestParam(required = false) String destino,
          @RequestParam(required = false) String situacao,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(servico.filtrar(empresa, origem, destino, situacao, pagina(page, size)));
    }

    @PostMapping("/importar")
    public ResponseEntity<ImportacaoResultado> importar(@RequestParam("arquivo") MultipartFile arquivo) {
        return ResponseEntity.ok(importacao.importar(arquivo));
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Long>> estatisticas() {
        return ResponseEntity.ok(servico.contarPorSituacao());
    }
}
