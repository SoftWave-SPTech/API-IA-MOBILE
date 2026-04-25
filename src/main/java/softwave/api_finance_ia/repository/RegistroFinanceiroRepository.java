package softwave.api_finance_ia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import softwave.api_finance_ia.entity.RegistroFinanceiro;

import java.util.List;

public interface RegistroFinanceiroRepository extends JpaRepository<RegistroFinanceiro, Long> {

    List<RegistroFinanceiro> findByClienteId(Long clienteId);

    @Query("select distinct r.processoId from RegistroFinanceiro r where r.clienteId = :clienteId")
    List<Long> findDistinctProcessoIdByClienteId(@Param("clienteId") Long clienteId);
}
