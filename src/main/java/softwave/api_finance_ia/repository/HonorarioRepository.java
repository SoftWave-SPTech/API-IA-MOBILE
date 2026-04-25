package softwave.api_finance_ia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import softwave.api_finance_ia.entity.Honorario;

import java.util.Collection;
import java.util.List;

public interface HonorarioRepository extends JpaRepository<Honorario, Long> {

    List<Honorario> findByProcessoIdIn(Collection<Long> processoIds);
}
