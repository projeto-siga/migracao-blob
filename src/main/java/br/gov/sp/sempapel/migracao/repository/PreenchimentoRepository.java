package br.gov.sp.sempapel.migracao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import br.gov.sp.sempapel.migracao.model.ExPreenchimento;

@Repository
public interface PreenchimentoRepository extends PagingAndSortingRepository<ExPreenchimento, Long> {

	@Query("select d "
			+ "from ExPreenchimento d "
			+ "where d.cpArquivo is null "
			+ "and d.preenchimentoBlob is not null ")
	Page<ExPreenchimento> findPreenchimentoModoBlob(Pageable pageable);
}
