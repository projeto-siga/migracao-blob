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

import br.gov.sp.sempapel.migracao.model.ExDocumento;
import br.gov.sp.sempapel.migracao.repository.DocumentoRepository;

@Service
public class DocumentoService {

	private final static Logger log = Logger.getLogger(DocumentoService.class);
	private DecimalFormat df = new DecimalFormat("#.##");
	
	private static final int tamanhoPagina = 100;
	
	@Value("#{'${sempapel.orgaos}'.split(',')}")
	private List<String> orgaos;
	
	@Autowired
	private DocumentoRepository documentoRepository;
	
	@Autowired
	private MigracaoService migracaoService;
	
	public void migrar() throws Exception {
		Pageable pageable = PageRequest.of(0, tamanhoPagina);
		Page<ExDocumento> documentos = findDocumentos(pageable);
		long total = documentos.getTotalElements();
		log.info("### " + total + " documentos para migração");
		double progresso = 0d;
		long i = new Date().getTime();
		
		boolean continuar = true;
		while(continuar) {
			for(ExDocumento doc : documentos) {
				migracaoService.migrarDocumento(doc);
			}
			
			long t = new Date().getTime() - i;
			t = t/1000;
			progresso += documentos.getSize();
			if(progresso > total)
				progresso = total;
			log.info("### " + progresso + " de " + total + " documentos migrados " + df.format(progresso/t) + " registros / segundo");
			
			if(documentos.hasNext()) {
				pageable = documentos.nextPageable();
				documentos = findDocumentos(pageable);
			} else 
				continuar = false;
		}
		
	}
	
	private Page<ExDocumento> findDocumentos(Pageable pageable){
		if(orgaos!=null && orgaos.size()>0)
			return documentoRepository.findDocumentoModoBlob(pageable, orgaos.stream().map(Long::parseLong).collect(Collectors.toList()));
		else
			return documentoRepository.findDocumentoModoBlob(pageable);
	}
	
}
