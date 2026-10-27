package br.edu.fatec.simov.service;

import br.edu.fatec.simov.model.Voo;
import br.edu.fatec.simov.repository.VooRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VooServiceTest {
    @Mock VooRepository repository;
    private VooService service;
    private Validator validator;

    @BeforeEach void iniciar() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        service = new VooService(repository, validator);
    }

    private Voo valido() {
        Voo v = new Voo();
        v.setEmpresaAerea(" azu "); v.setNumeroVoo("1234");
        v.setAeroportoOrigem(" sbgr "); v.setAeroportoDestino("sbrj");
        v.setPartidaPrevista(LocalDateTime.of(2026, 9, 10, 8, 0));
        v.setSituacaoVoo("realizado");
        return v;
    }

    @Test void normalizaEPreparaChave() {
        Voo v = service.preparar(valido());
        assertEquals("AZU", v.getEmpresaAerea());
        assertEquals("SBGR", v.getAeroportoOrigem());
        assertEquals("REALIZADO", v.getSituacaoVoo());
        assertEquals(64, v.getChaveOrigem().length());
    }

    @Test void rejeitaVooSemDatas() {
        Voo v = valido(); v.setPartidaPrevista(null);
        assertThrows(IllegalArgumentException.class, () -> service.preparar(v));
    }

    @Test void rejeitaAeroportoInvalido() {
        Voo v = valido(); v.setAeroportoOrigem("GRU");
        assertThrows(IllegalArgumentException.class, () -> service.preparar(v));
    }

    @Test void buscaInexistenteRetornaOptionalVazio() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertTrue(service.buscar(999L).isEmpty());
    }
}
