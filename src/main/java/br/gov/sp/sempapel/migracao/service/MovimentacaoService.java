package br.gov.sp.sempapel.migracao.service;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.gov.sp.sempapel.migracao.model.ExMovimentacao;
import br.gov.sp.sempapel.migracao.repository.MovimentacaoRepository;

@Service
public class MovimentacaoService {

	private final static Logger log = Logger.getLogger(MovimentacaoService.class);
	private DecimalFormat df = new DecimalFormat("#.##");
	
	private static final int tamanhoPagina = 100;
	
	@Value("#{'${sempapel.orgaos}'.split(',')}")
	private List<String> orgaos;
	
	@Autowired
	private MovimentacaoRepository movimentacaoRepository;
	
	@Autowired
	private MigracaoService migracaoService;
	
	public void migrar() throws Exception {
		Pageable pageable = PageRequest.of(0, tamanhoPagina);
		Page<ExMovimentacao> movimentacaos = findMovimentacaos(pageable);
		long total = movimentacaos.getTotalElements();
		log.info("### " + total + " movimentações para migração");
		double progresso = 0d;
		long i = new Date().getTime();
		
		boolean continuar = true;
		while(continuar) {
			for(ExMovimentacao doc : movimentacaos) {
				migracaoService.migrarMovimentacao(doc);
			}
			
			long t = new Date().getTime() - i;
			t = t/1000;
			progresso += movimentacaos.getSize();
			if(progresso > total)
				progresso = total;
			log.info("### " + progresso + " de " + total + " movimentações migradas " + df.format(progresso/t) + " registros / segundo");
			
			if(movimentacaos.hasNext()) {
				pageable = movimentacaos.nextPageable();
				movimentacaos = findMovimentacaos(pageable);
			} else 
				continuar = false;
		}
		
	}
	
	private Page<ExMovimentacao> findMovimentacaos(Pageable pageable){
		if(orgaos!=null && orgaos.size()>0)
			return movimentacaoRepository.findMovimentacaoModoBlob(pageable, orgaos.stream().map(Long::parseLong).collect(Collectors.toList()));
		else
			return movimentacaoRepository.findMovimentacaoModoBlob(pageable);
	}
	
}
