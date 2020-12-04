package br.gov.sp.sempapel.migracao.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.gov.sp.sempapel.migracao.model.ExMovimentacao;

@Repository
public interface MovimentacaoRepository extends PagingAndSortingRepository<ExMovimentacao, Long> {

	@Query("select d "
			+ "from ExMovimentacao d "
			+ "where d.cpArquivo is null "
			+ "and d.conteudoBlobMov is not null ")
	Page<ExMovimentacao> findMovimentacaoModoBlob(Pageable pageable);
	
	@Query("select d "
			+ "from ExMovimentacao d "
			+ "where d.cpArquivo is null "
			+ "and d.conteudoBlobMov is not null "
			+ "and d.exMobil.exDocumento.idOrgaoUsuario in :orgaos")
	Page<ExMovimentacao> findMovimentacaoModoBlob(Pageable pageable, @Param("orgaos") List<Long> orgaos);
}
