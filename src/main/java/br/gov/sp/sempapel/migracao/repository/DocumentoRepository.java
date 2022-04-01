package br.gov.sp.sempapel.migracao.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.gov.sp.sempapel.migracao.model.ExDocumento;

@Repository
public interface DocumentoRepository extends PagingAndSortingRepository<ExDocumento, Long> {

	@Query("select d.idDoc "
			+ "from ExDocumento d "
			+ "where d.cpArquivo is null "
			+ "and d.conteudoBlobDoc is not null ")
	Page<Long> findDocumentoModoBlob(Pageable pageable);

	@Query("select d.idDoc "
			+ "from ExDocumento d "
			+ "where d.cpArquivo is null "
			+ "and d.idOrgaoUsuario in :orgaos "
			+ "and d.conteudoBlobDoc is not null ")
	Page<Long> findDocumentoModoBlob(Pageable pageable, @Param("orgaos") List<Long> orgaos);
	
	@Query("select count(d.idDoc) "
			+ "from ExDocumento d "
			+ "where d.cpArquivo is null "
			+ "and d.idOrgaoUsuario in :orgaos "
			+ "and d.conteudoBlobDoc is not null ")
	Long countDocumentoModoBlob(@Param("orgaos") List<Long> orgaos);
	
	@Query("select d "
			+ "from ExDocumento d "
			+ "where d.cpArquivo is not null "
			+ "and d.conteudoBlobDoc is not null "
			+ "and d.idOrgaoUsuario in :orgaos")
	Page<ExDocumento> findDocumentoModoHcp(Pageable pageable, @Param("orgaos") List<Long> orgaos);
	
	@Query(value=""
			+ "select doc.* " + 
			"from siga.ex_documento_old doc " + 
			"inner join corporativo.cp_arquivo a on a.id_arq=doc.id_arq " + 
			"where a.tamanho_arq <> length(doc.conteudo_blob_doc) " + 
			"and doc.conteudo_blob_doc is not null ",
			nativeQuery = true)
	List<ExDocumento> findDocumentoDivergente();
	
	@Query(value=""
			+ "select o.id_orgao_usu as id, o.sigla_orgao_usu as sigla, o.nm_orgao_usu as orgao, count(d.id_doc) as total, sum(length(d.conteudo_blob_doc))/1024/1024 as tamanho  "
			+ "from siga.ex_documento_old d "
			+ "left join corporativo.cp_orgao_usuario o on d.id_orgao_usu=o.id_orgao_usu "
			+ "where d.id_arq is null "
			+ "and d.conteudo_blob_doc is not null "
			+ "group by o.id_orgao_usu, o.sigla_orgao_usu, o.nm_orgao_usu ",
			nativeQuery = true)
	List<Map<String, Object>> listMigrar();
	
	@Query("select d.idDoc "
			+ "from ExDocumento d "
			+ "join d.cpArquivo a "
			+ "where d.cpArquivo is not null "
			+ "and a.hash is not null "
			+ "and d.idOrgaoUsuario in :orgaos "
			+ "and d.conteudoBlobDoc is not null "
			+ "and d.apagado is null ")
	Page<Long> findDocumentoRemover(Pageable pageable, @Param("orgaos") List<Long> orgaos);
	
	@Query("select d.idDoc "
			+ "from ExDocumento d "
			+ "join d.cpArquivo a "
			+ "where d.cpArquivo is not null "
			+ "and a.hash is not null "
			+ "and d.conteudoBlobDoc is not null "
			+ "and d.apagado is null ")
	Page<Long> findDocumentoRemover(Pageable pageable);
	
	@Query(value=""
			+ "select o.id_orgao_usu as id, o.sigla_orgao_usu as sigla, o.nm_orgao_usu as orgao, count(d.id_doc) as total "
			+ "from corporativo.cp_arquivo a "
			+ "join siga.ex_documento_old d on d.id_arq=a.id_arq "
			+ "left join corporativo.cp_orgao_usuario o on d.id_orgao_usu=o.id_orgao_usu "
			+ "where a.hash_md5 is null "
			+ "and d.conteudo_blob_doc is not null "
			+ "group by o.id_orgao_usu, o.sigla_orgao_usu, o.nm_orgao_usu ",
			nativeQuery = true)
	List<Map<String, Object>> listSemHash();
	
	@Query(value=""
			+ "select d.idDoc "
			+ "from corporativo.cp_arquivo a "
			+ "join siga.ex_documento_old d on d.id_arq=a.id_arq "
			+ "where a.hash_md5 is null "
			+ "and d.idOrgaoUsuario = :idOrgao "
			+ "and d.conteudo_blob_doc is not null ",
			nativeQuery = true)
	Page<Long>  findDocumentosSemHash(Pageable pageable, @Param("idOrgao") Long idOrgao);
	
}
