package br.gov.sp.sempapel.migracao.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import br.gov.sp.sempapel.migracao.model.ExPreenchimento;

@Repository
public interface PreenchimentoRepository extends PagingAndSortingRepository<ExPreenchimento, Long> {

	@Query("select d.idPreenchimento "
			+ "from ExPreenchimento d "
			+ "where d.cpArquivo is null "
			+ "and d.preenchimentoBlob is not null ")
	Page<Long> findPreenchimentoModoBlob(Pageable pageable);
	
	@Query(value=""
			+ "select count(*) as total, sum(length(p.preenchimento_blob))/1024/1024 as tamanho "
			+ "from siga.ex_preenchimento_old p "
			+ "where 1=1 "
			+ "and p.preenchimento_blob is not null "
			+ "and p.id_arq is null",
			nativeQuery = true)
	List<Map<String, Object>> listMigrar();
	
	@Query("select d.idPreenchimento "
			+ "from ExPreenchimento d "
			+ "join d.cpArquivo a "
			+ "where d.cpArquivo is not null "
			+ "and a.hash is not null "
			+ "and d.preenchimentoBlob is not null ")
	Page<Long> findPreenchimentoRemover(Pageable pageable);
	
	@Query(value=""
			+ "select count(p.id_preenchimento) as total "
			+ "from corporativo.cp_arquivo a "
			+ "join siga.ex_preenchimento_old p on p.id_arq=a.id_arq "
			+ "where a.hash_md5 is null "
			+ "and p.preenchimento_blob is not null ",
			nativeQuery = true)
	List<Map<String, Object>> listSemHash();
}
