package br.gov.sp.sempapel.migracao.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import br.gov.sp.sempapel.migracao.model.CpArquivoExcluir;

@Repository
public interface ArquivoExcluirRepository extends PagingAndSortingRepository<CpArquivoExcluir, String> {

}
