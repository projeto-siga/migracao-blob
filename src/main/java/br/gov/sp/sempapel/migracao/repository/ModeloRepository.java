package br.gov.sp.sempapel.migracao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import br.gov.sp.sempapel.migracao.model.ExModelo;

@Repository
public interface ModeloRepository extends PagingAndSortingRepository<ExModelo, Long> {

	@Query("select d "
			+ "from ExModelo d "
			+ "where d.cpArquivo is null "
			+ "and d.conteudoBlobMod is not null ")
	Page<ExModelo> findModeloModoBlob(Pageable pageable);
}
