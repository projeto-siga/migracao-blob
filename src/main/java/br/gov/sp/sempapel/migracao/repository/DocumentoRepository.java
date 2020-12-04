package br.gov.sp.sempapel.migracao.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.gov.sp.sempapel.migracao.model.ExDocumento;

@Repository
public interface DocumentoRepository extends PagingAndSortingRepository<ExDocumento, Long> {

	@Query("select d "
			+ "from ExDocumento d "
			+ "where d.cpArquivo is null "
			+ "and d.conteudoBlobDoc is not null ")
	Page<ExDocumento> findDocumentoModoBlob(Pageable pageable);

	@Query("select d "
			+ "from ExDocumento d "
			+ "where d.cpArquivo is null "
			+ "and d.conteudoBlobDoc is not null "
			+ "and d.idOrgaoUsuario in :orgaos")
	Page<ExDocumento> findDocumentoModoBlob(Pageable pageable, @Param("orgaos") List<Long> orgaos);
}
