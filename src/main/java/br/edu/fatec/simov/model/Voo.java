package br.edu.fatec.simov.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Entity
@Table(name = "voos", indexes = {
    @Index(name = "idx_voos_empresa", columnList = "empresa_aerea"),
    @Index(name = "idx_voos_origem", columnList = "aeroporto_origem"),
    @Index(name = "idx_voos_situacao", columnList = "situacao_voo")
}, uniqueConstraints = @UniqueConstraint(name = "uk_voos_chave", columnNames = "chave_origem"))
public class Voo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "Use o codigo ICAO da empresa (3 letras)")
    @Column(name = "empresa_aerea", nullable = false, length = 3)
    private String empresaAerea;

    @NotBlank @Pattern(regexp = "[A-Z0-9]{1,10}", message = "Numero do voo invalido")
    @Column(name = "numero_voo", nullable = false, length = 10)
    private String numeroVoo;

    @Column(name = "codigo_di", length = 10)
    private String codigoDi;

    @Column(name = "codigo_tipo_linha", length = 10)
    private String codigoTipoLinha;

    @NotBlank @Pattern(regexp = "[A-Z]{4}", message = "Origem deve ter 4 letras ICAO")
    @Column(name = "aeroporto_origem", nullable = false, length = 4)
    private String aeroportoOrigem;

    @NotBlank @Pattern(regexp = "[A-Z]{4}", message = "Destino deve ter 4 letras ICAO")
    @Column(name = "aeroporto_destino", nullable = false, length = 4)
    private String aeroportoDestino;

    @Column(name = "partida_prevista")
    private LocalDateTime partidaPrevista;

    @Column(name = "partida_real")
    private LocalDateTime partidaReal;

    @Column(name = "chegada_prevista")
    private LocalDateTime chegadaPrevista;

    @Column(name = "chegada_real")
    private LocalDateTime chegadaReal;

    @NotBlank @Pattern(regexp = "REALIZADO|CANCELADO|NAO INFORMADO", message = "Situacao invalida")
    @Column(name = "situacao_voo", nullable = false, length = 20)
    private String situacaoVoo;

    @Column(name = "chave_origem", length = 64, nullable = false, unique = true)
    private String chaveOrigem;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmpresaAerea() { return empresaAerea; }
    public void setEmpresaAerea(String v) { this.empresaAerea = v; }
    public String getNumeroVoo() { return numeroVoo; }
    public void setNumeroVoo(String v) { this.numeroVoo = v; }
    public String getCodigoDi() { return codigoDi; }
    public void setCodigoDi(String v) { this.codigoDi = v; }
    public String getCodigoTipoLinha() { return codigoTipoLinha; }
    public void setCodigoTipoLinha(String v) { this.codigoTipoLinha = v; }
    public String getAeroportoOrigem() { return aeroportoOrigem; }
    public void setAeroportoOrigem(String v) { this.aeroportoOrigem = v; }
    public String getAeroportoDestino() { return aeroportoDestino; }
    public void setAeroportoDestino(String v) { this.aeroportoDestino = v; }
    public LocalDateTime getPartidaPrevista() { return partidaPrevista; }
    public void setPartidaPrevista(LocalDateTime v) { this.partidaPrevista = v; }
    public LocalDateTime getPartidaReal() { return partidaReal; }
    public void setPartidaReal(LocalDateTime v) { this.partidaReal = v; }
    public LocalDateTime getChegadaPrevista() { return chegadaPrevista; }
    public void setChegadaPrevista(LocalDateTime v) { this.chegadaPrevista = v; }
    public LocalDateTime getChegadaReal() { return chegadaReal; }
    public void setChegadaReal(LocalDateTime v) { this.chegadaReal = v; }
    public String getSituacaoVoo() { return situacaoVoo; }
    public void setSituacaoVoo(String v) { this.situacaoVoo = v; }
    public String getChaveOrigem() { return chaveOrigem; }
    public void setChaveOrigem(String v) { this.chaveOrigem = v; }
}
