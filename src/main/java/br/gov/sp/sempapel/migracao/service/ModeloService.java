package br.gov.sp.sempapel.migracao.service;

import java.text.DecimalFormat;
import java.util.Date;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.gov.sp.sempapel.migracao.model.ExModelo;
import br.gov.sp.sempapel.migracao.repository.ModeloRepository;

@Service
public class ModeloService {

	private final static Logger log = Logger.getLogger(ModeloService.class);
	private DecimalFormat df = new DecimalFormat("#.##");
	
	private static final int tamanhoPagina = 100;
	
	@Autowired
	private ModeloRepository modeloRepository;
	
	@Autowired
	private MigracaoService migracaoService;
	
	public void migrar() throws Exception {
		Pageable pageable = PageRequest.of(0, tamanhoPagina);
		Page<ExModelo> modelos = findModelos(pageable);
		long total = modelos.getTotalElements();
		log.info("### " + total + " modelos para migração");
		double progresso = 0d;
		long i = new Date().getTime();
		
		boolean continuar = true;
		while(continuar) {
			for(ExModelo doc : modelos) {
				migracaoService.migrarModelo(doc);
			}
			
			long t = new Date().getTime() - i;
			t = t/1000;
			progresso += modelos.getSize();
			if(progresso > total)
				progresso = total;
			log.info("### " + progresso + " de " + total + " modelos migrados " + df.format(progresso/t) + " registros / segundo");
			
//			if(modelos.hasNext()) {
//				pageable = modelos.nextPageable();
//				modelos = findModelos(pageable);
//			} else 
				continuar = false;
		}
		
	}
	
	private Page<ExModelo> findModelos(Pageable pageable){
		return modeloRepository.findModeloModoBlob(pageable);
	}
	
}
