package br.edu.fatec.simov.repository;

import br.edu.fatec.simov.model.Voo;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface VooRepository extends JpaRepository<Voo, Long>, JpaSpecificationExecutor<Voo> {
    Optional<Voo> findByChaveOrigem(String chave);
    List<Voo> findAllByChaveOrigemIn(Collection<String> chaves);

    @Query("SELECT v.situacaoVoo, COUNT(v) FROM Voo v GROUP BY v.situacaoVoo ORDER BY v.situacaoVoo")
    List<Object[]> contarPorSituacao();
}
