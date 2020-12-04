package br.gov.sp.sempapel.migracao.service;

import java.text.DecimalFormat;
import java.util.Date;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.gov.sp.sempapel.migracao.model.ExPreenchimento;
import br.gov.sp.sempapel.migracao.repository.PreenchimentoRepository;

@Service
public class PreenchimentoService {

	private final static Logger log = Logger.getLogger(PreenchimentoService.class);
	private DecimalFormat df = new DecimalFormat("#.##");
	
	private static final int tamanhoPagina = 100;
	
	@Autowired
	private PreenchimentoRepository preenchimentoRepository;
	
	@Autowired
	private MigracaoService migracaoService;
	
	public void migrar() throws Exception {
		Pageable pageable = PageRequest.of(0, tamanhoPagina);
		Page<ExPreenchimento> preenchimentos = findPreenchimentos(pageable);
		long total = preenchimentos.getTotalElements();
		log.info("### " + total + " preenchimentos para migração");
		double progresso = 0d;
		long i = new Date().getTime();
		
		boolean continuar = true;
		while(continuar) {
			for(ExPreenchimento doc : preenchimentos) {
				migracaoService.migrarPreenchimento(doc);
			}
			
			long t = new Date().getTime() - i;
			t = t/1000;
			progresso += preenchimentos.getSize();
			if(progresso > total)
				progresso = total;
			log.info("### " + progresso + " de " + total + " preenchimentos migrados " + df.format(progresso/t) + " registros / segundo");
			
			if(preenchimentos.hasNext()) {
				pageable = preenchimentos.nextPageable();
				preenchimentos = findPreenchimentos(pageable);
			} else 
				continuar = false;
		}
		
	}
	
	private Page<ExPreenchimento> findPreenchimentos(Pageable pageable){
		return preenchimentoRepository.findPreenchimentoModoBlob(pageable);
	}
	
}
