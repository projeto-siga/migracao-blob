package br.gov.sp.sempapel.migracao.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.gov.sp.sempapel.migracao.model.ExMovimentacao;

@Repository
public interface MovimentacaoRepository extends PagingAndSortingRepository<ExMovimentacao, Long> {

	@Query("select d.idMov "
			+ "from ExMovimentacao d "
			+ "where d.cpArquivo is null "
			+ "and d.conteudoBlobMov is not null ")
	Page<Long> findMovimentacaoModoBlob(Pageable pageable);
	
	@Query("select d.idMov "
			+ "from ExMovimentacao d "
			+ "where d.cpArquivo is null "
			+ "and d.conteudoBlobMov is not null "
			+ "and d.exMobil.exDocumento.idOrgaoUsuario in :orgaos")
	Page<Long> findMovimentacaoModoBlob(Pageable pageable, @Param("orgaos") List<Long> orgaos);
	
	@Query(value=""
			+ "select o.id_orgao_usu as id, o.SIGLA_ORGAO_USU as sigla, o.NM_ORGAO_USU as orgao, count(*) as total, sum(length(mv.conteudo_blob_mov))/1024/1024 as tamanho "
			+ "from siga.ex_movimentacao_old mv "
			+ "left join siga.ex_mobil mb on mb.id_mobil=mv.id_mobil "
			+ "left join siga.ex_documento_old d on d.id_doc=mb.id_doc "
			+ "left join corporativo.CP_ORGAO_USUARIO o on o.id_orgao_usu=d.id_orgao_usu "
			+ "where 1=1 "
			+ "and mv.conteudo_blob_mov is not null "
			+ "and mv.id_arq is null "
			+ "group by o.id_orgao_usu, o.SIGLA_ORGAO_USU, o.NM_ORGAO_USU ",
			nativeQuery = true)
	List<Map<String, Object>> listMigrar();
	
	@Query(value=""
			+ "select o.id_orgao_usu as id, o.SIGLA_ORGAO_USU as sigla, o.NM_ORGAO_USU as orgao, count(*) as total, sum(length(mv.conteudo_blob_mov))/1024/1024 as tamanho "
			+ "from siga.ex_movimentacao_old mv "
			+ "left join siga.ex_mobil mb on mb.id_mobil=mv.id_mobil "
			+ "left join siga.ex_documento_old d on d.id_doc=mb.id_doc "
			+ "left join corporativo.CP_ORGAO_USUARIO o on o.id_orgao_usu=d.id_orgao_usu "
			+ "where 1=1 "
			+ "and d.id_orgao_usu = :idOrgao "
			+ "and mv.conteudo_blob_mov is not null "
			+ "and mv.id_arq is null "
			+ "group by o.id_orgao_usu, o.SIGLA_ORGAO_USU, o.NM_ORGAO_USU ",
			nativeQuery = true)
	List<Map<String, Object>> statusOrgao(@Param("idOrgao") Long idOrgao);
	
	@Query("select d.idMov "
			+ "from ExMovimentacao d "
			+ "join d.cpArquivo a "
			+ "where d.cpArquivo is not null "
			+ "and a.hash is not null "
			+ "and d.conteudoBlobMov is not null "
			+ "and d.apagado is null "
			+ "and d.exMobil.exDocumento.idOrgaoUsuario in :orgaos ")
	Page<Long> findMovimentacaoRemover(Pageable pageable, @Param("orgaos") List<Long> orgaos);
	
	@Query("select d.idMov "
			+ "from ExMovimentacao d "
			+ "join d.cpArquivo a "
			+ "where d.cpArquivo is not null "
			+ "and a.hash is not null "
			+ "and d.conteudoBlobMov is not null "
			+ "and d.apagado is null ")
	Page<Long> findMovimentacaoRemover(Pageable pageable);
	
	@Query(value = "SELECT X.ID_MOV \n" + 
			"FROM SIGA.EX_MOVIMENTACAO_OLD X,\n" + 
			"	CORPORATIVO.CP_ARQUIVO Y\n" + 
			"WHERE\n" + 
			"	X.ID_ARQ = Y.ID_ARQ\n" + 
			"	AND Y.HASH_MD5 IS NOT NULL\n" + 
			"	AND X.CONTEUDO_BLOB_MOV IS NOT NULL\n" + 
			"	AND X.FL_BLOB_OK IS NULL\n" + 
			"	AND MOD(X.ID_MOV, :divisor) = :resto\n" + 
			" 	AND X.ID_MOV NOT IN (:listaIdMov) \n"+
			" 	AND ROWNUM <= :pagina ",
			nativeQuery = true )
	List<BigDecimal> findMovimentacaoRemover(@Param("pagina") int pagina, @Param("divisor") int divisor, @Param("resto") int resto, @Param("listaIdMov") List<BigDecimal> listIdMov);
	
	@Query(value=""
			+ "select o.id_orgao_usu as id, o.sigla_orgao_usu as sigla, o.nm_orgao_usu as orgao, count(mv.id_mov) as total "
			+ "from corporativo.cp_arquivo a "
			+ "join siga.ex_movimentacao_old mv on mv.id_arq=a.id_arq "
			+ "left join siga.ex_mobil mb on mb.id_mobil=mv.id_mobil "
			+ "left join siga.ex_documento_old d on d.id_doc=mb.id_doc "
			+ "left join corporativo.cp_orgao_usuario o on d.id_orgao_usu=o.id_orgao_usu  "
			+ "where a.hash_md5 is null "
			+ "and mv.conteudo_blob_mov is not null "
			+ "group by o.id_orgao_usu, o.sigla_orgao_usu, o.nm_orgao_usu ",
			nativeQuery = true)
	List<Map<String, Object>> listSemHash();

	@Query(value=""
			+ "SELECT\n" + 
			"	X.ID_ARQ,\n" + 
			"	Y.CAMINHO,\n" + 
			"	Y.HASH_MD5\n" + 
			"FROM\n" + 
			"	SIGA.EX_MOVIMENTACAO_OLD X,\n" + 
			"	CORPORATIVO.CP_ARQUIVO Y\n" + 
			"WHERE\n" + 
			"	X.ID_MOV = :idMov\n" + 
			"	AND Y.ID_ARQ = X.ID_ARQ",
			nativeQuery = true)
	Map<String, Object> findInformacaoArquivo(@Param("idMov") BigDecimal idMov);

	@Modifying
	@Query(value="UPDATE SIGA.EX_MOVIMENTACAO_OLD X SET X.FL_BLOB_OK = :apagado WHERE X.ID_MOV  = :idMov", nativeQuery = true)
	void atualizarApagado(@Param("idMov") BigDecimal idMov, @Param("apagado") String apagado);
}
