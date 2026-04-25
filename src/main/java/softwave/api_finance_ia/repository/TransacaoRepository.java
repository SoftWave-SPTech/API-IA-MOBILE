package softwave.api_finance_ia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import softwave.api_finance_ia.entity.Transacao;

import java.util.Collection;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByHonorarioIdIn(Collection<Long> honorarioIds);
}
