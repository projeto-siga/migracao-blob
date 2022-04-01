package br.gov.sp.sempapel.migracao.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import br.gov.sp.sempapel.migracao.model.ExModelo;

@Repository
public interface ModeloRepository extends PagingAndSortingRepository<ExModelo, Long> {

	@Query("select d.idMod "
			+ "from ExModelo d "
			+ "where d.cpArquivo is null "
			+ "and d.conteudoBlobMod is not null ")
	Page<Long> findModeloModoBlob(Pageable pageable);
	
	
	@Query(value=""
			+ "select count(*) as total, sum(length(m.conteudo_blob_mod))/1024/1024 as tamanho "
			+ "from siga.ex_modelo_old m "
			+ "where 1=1 "
			+ "and m.conteudo_blob_mod is not null "
			+ "and m.id_arq is null",
			nativeQuery = true)
	List<Map<String, Object>> listMigrar();
	
	@Query("select d.idMod "
			+ "from ExModelo d "
			+ "join d.cpArquivo a "
			+ "where d.cpArquivo is not null "
			+ "and a.hash is not null "
			+ "and d.conteudoBlobMod is not null ")
	Page<Long> findModeloRemover(Pageable pageable);
	
	@Query(value=""
			+ "select count(m.id_mod) as total "
			+ "from corporativo.cp_arquivo a "
			+ "join siga.ex_modelo_old m on m.id_arq=a.id_arq "
			+ "where a.hash_md5 is null "
			+ "and m.conteudo_blob_mod is not null ",
			nativeQuery = true)
	List<Map<String, Object>> listSemHash();
}
