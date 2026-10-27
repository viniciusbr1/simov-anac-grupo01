package br.edu.fatec.simov.service;

import br.edu.fatec.simov.dto.ImportacaoResultado;
import br.edu.fatec.simov.model.Voo;
import br.edu.fatec.simov.repository.VooRepository;
import jakarta.validation.Validation;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VooImportacaoServiceTest {
    @Mock VooRepository repository;
    private VooImportacaoService service;

    @BeforeEach void iniciar() {
        var vooService = new VooService(repository, Validation.buildDefaultValidatorFactory().getValidator());
        service = new VooImportacaoService(repository, vooService, 500);
    }

    @Test void importaValidaEContaRejeitadaEDuplicada() {
        String cabecalho = "Sigla ICAO Empresa Aérea;Número Voo;Código DI;Código Tipo Linha;" +
            "Sigla ICAO Aeroporto Origem;Sigla ICAO Aeroporto Destino;Partida Prevista;" +
            "Partida Real;Chegada Prevista;Chegada Real;Situação Voo\n";
        String linha = "AZU;1234;0;N;SBGR;SBRJ;10/09/2026 08:00;;;;Realizado\n";
        String invalida = "AZU;12;0;N;GRU;SBRJ;10/09/2026 08:00;;;;Realizado\n";
        MockMultipartFile csv = new MockMultipartFile("arquivo", "exemplo.csv", "text/csv",
            (cabecalho + linha + linha + invalida).getBytes(StandardCharsets.UTF_8));
        when(repository.findAllByChaveOrigemIn(anyCollection())).thenReturn(List.of());
        ImportacaoResultado r = service.importar(csv);
        assertEquals(3, r.linhasLidas());
        assertEquals(1, r.importadas());
        assertEquals(1, r.duplicadas());
        assertEquals(1, r.rejeitadas());
        verify(repository).saveAll(anyList());
    }

    @Test void rejeitaCabecalhoErrado() {
        MockMultipartFile csv = new MockMultipartFile("arquivo", "erro.csv", "text/csv",
            "a;b\n1;2\n".getBytes(StandardCharsets.UTF_8));
        assertThrows(IllegalArgumentException.class, () -> service.importar(csv));
    }
}
