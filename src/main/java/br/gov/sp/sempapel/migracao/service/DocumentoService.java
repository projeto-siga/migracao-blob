package br.gov.sp.sempapel.migracao.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import br.gov.sp.sempapel.migracao.model.ExDocumento;
import br.gov.sp.sempapel.migracao.repository.DocumentoRepository;

@Service
@Scope("singleton")
public class DocumentoService {

	private final static Logger log = Logger.getLogger(DocumentoService.class);
	private DecimalFormat df = new DecimalFormat("#.##");
	
	private static final int tamanhoPagina = 100;
	
	private List<String> orgaos;
	
	@Autowired
	private DocumentoRepository documentoRepository;
	
	@Autowired
	private MigracaoService migracaoService;
	
	@Autowired
	private HcpService hcpService;
	
	private Long total= 0l;
	private Long migrado = 0l;
	private Boolean cancelar = false;
	private Boolean migrando = false;
	
	private Map<String, String> m = new HashMap<String, String>();
	
	public void setOrgaos(List<String> orgaos) {
		this.orgaos = orgaos;
	}
	
	public List<String> getOrgaos() {
		return orgaos;
	}
	
	public Map<String, String> statusOrgao(List<String> orgaos){
		Map<String, String> m = new HashMap<String, String>();
		try {
			Long total = documentoRepository.countDocumentoModoBlob(orgaos.stream().map(Long::parseLong).collect(Collectors.toList()));
			m.put("total", total.toString());
		} catch (Exception e) {
			m.put("erro-status", e.getMessage());
		}
		return m;
	}
	
	public void cancelar() throws Exception {
		cancelar=true;
		migrando=false;
	}
	
	@Async
	public void migrar() throws Exception {
		if(migrando)
			return;
		
		m = new HashMap<String, String>();
		migrando = true;
		cancelar = false;
		
		try {
			ExecutorService executor = Executors.newFixedThreadPool(tamanhoPagina);
			
			Pageable pageable = PageRequest.of(0, tamanhoPagina);
			Page<Long> documentos = findDocumentos(pageable);
			total = documentos.getTotalElements();
			log.info("### " + total + " documentos para migração");
			long progresso = 0;
			long i = new Date().getTime();
			
			Map<Future<Boolean>, Future<Boolean>> mapFuture = new ConcurrentHashMap<Future<Boolean>, Future<Boolean>>();
			
			boolean continuar = total>0?true:false;
			while(continuar && !cancelar) {
				for(Long id : documentos) {
					Future<Boolean> f = executor.submit(() -> migracaoService.migrarDocumento(id));
					mapFuture.put(f, f);
				}
				
				while(mapFuture.size()>0) {
					for(Future<Boolean> f : mapFuture.values()) {
						if(f.isDone()) {
							mapFuture.remove(f);
							progresso++;
							migrado = progresso;
						}
					}
				}
				
				long t = new Date().getTime() - i;
				t = t/1000;
				log.info("### " + progresso + " de " + total + " documentos migrados " + df.format(progresso/t) + " registros / segundo");
				
				if(documentos.hasNext()) {
					documentos = findDocumentos(pageable);
				} else 
					continuar = false;
			}
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			log.error(e.getMessage(), e);
		}
		migrando = false;
	}
	
	private Page<Long> findDocumentos(Pageable pageable){
		if(orgaos!=null && orgaos.size()>0)
			return documentoRepository.findDocumentoModoBlob(pageable, orgaos.stream().map(Long::parseLong).collect(Collectors.toList()));
		else
			return documentoRepository.findDocumentoModoBlob(pageable);
	}
	
	private Page<Long> findDocumentosMigrados(Pageable pageable){
		if(orgaos!=null && orgaos.size()>0)
			return documentoRepository.findDocumentoModoBlob(pageable, orgaos.stream().map(Long::parseLong).collect(Collectors.toList()));
		else
			return documentoRepository.findDocumentoModoBlob(pageable);
	}
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public void conferirTamanho() throws Exception {
		List<ExDocumento> divergentes = documentoRepository.findDocumentoDivergente();
		for(ExDocumento d : divergentes) {
			byte[] hcpArq = hcpService.recuperar(d.getCpArquivo());
			if(d.getConteudoBlobDoc().length == hcpArq.length) {
//				d.getCpArquivo().setTamanho(d.getConteudoBlobDoc().length);
//				arquivoRepository.save(d.getCpArquivo());
			} else {
				save("doc-" + d.getIdDoc() + "blob.zip", d.getConteudoBlobDoc());
				save("doc-" + d.getIdDoc() + "hcp.zip", hcpArq);
			}
		}
	}
	
	private void save(String name, byte[] file) throws IOException {
		  Path path = Paths.get("\\pdfs\\"+name);
		  Files.write(path, file);
	}

	public void desfazerMigracao() {
		m = new HashMap<String, String>();
		
		try {

			ExecutorService executor = Executors.newFixedThreadPool(tamanhoPagina);

			Pageable pageable = PageRequest.of(0, tamanhoPagina);
			Page<Long> documentos = findDocumentosMigrados(pageable);
			total = documentos.getTotalElements();
			log.info("### " + total + " documentos para migração");
			long progresso = 0;
			long i = new Date().getTime();

			Map<Future<Boolean>, Future<Boolean>> mapFuture = new ConcurrentHashMap<Future<Boolean>, Future<Boolean>>();

			boolean continuar = total>0?true:false;
			while(continuar && !cancelar) {
				for(Long id : documentos) {
					Future<Boolean> f = executor.submit(() -> migracaoService.migrarDocumento(id));
					mapFuture.put(f, f);
				}

				while(mapFuture.size()>0) {
					for(Future<Boolean> f : mapFuture.values()) {
						if(f.isDone()) {
							mapFuture.remove(f);
							progresso++;
							migrado = progresso;
						}
					}
				}

				long t = new Date().getTime() - i;
				t = t/1000;
				log.info("### " + progresso + " de " + total + " documentos migrados " + df.format(progresso/t) + " registros / segundo");

				if(documentos.hasNext()) {
					documentos = findDocumentos(pageable);
				} else 
					continuar = false;
			}
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			log.error(e.getMessage(), e);
		}
		migrando = false;
	}
	
	public List<Map<String,Object>> listMigrar() {
		return documentoRepository.listMigrar();
	}
	
	public List<Map<String,Object>> listSemHash() {
		return documentoRepository.listSemHash();
	}
	
	@Async
	public void gerarHash(Long idOrgao) {
		if(migrando)
			return;
		
		m = new HashMap<String, String>();
		migrando = true;
		cancelar = false;
		
		try {
			ExecutorService executor = Executors.newFixedThreadPool(tamanhoPagina);
			
			Pageable pageable = PageRequest.of(0, tamanhoPagina);
			Page<Long> documentos = documentoRepository.findDocumentosSemHash(pageable, idOrgao);
			total = documentos.getTotalElements();
			log.info("### " + total + " documentos para geração de hash");
			long progresso = 0;
			long i = new Date().getTime();
			
			Map<Future<Boolean>, Future<Boolean>> mapFuture = new ConcurrentHashMap<Future<Boolean>, Future<Boolean>>();
			
			boolean continuar = total>0?true:false;
			while(continuar && !cancelar) {
				for(Long id : documentos) {
					Future<Boolean> f = executor.submit(() -> migracaoService.gerarHashDocumento(id));
					mapFuture.put(f, f);
				}
				
				while(mapFuture.size()>0) {
					for(Future<Boolean> f : mapFuture.values()) {
						if(f.isDone()) {
							mapFuture.remove(f);
							progresso++;
							migrado = progresso;
						}
					}
				}
				
				long t = new Date().getTime() - i;
				t = t/1000;
				log.info("### " + progresso + " de " + total + " documentos migrados " + df.format(progresso/t) + " registros / segundo");
				
				if(documentos.hasNext()) {
					documentos = documentoRepository.findDocumentosSemHash(pageable, idOrgao);
				} else 
					continuar = false;
			}
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			log.error(e.getMessage(), e);
		}
		migrando = false;
	}
	
	@Async
	public void remover() throws Exception {
		if(migrando)
			return;
		
		m = new HashMap<String, String>();
		migrando = true;
		cancelar = false;
		
		try {

			ExecutorService executor = Executors.newFixedThreadPool(tamanhoPagina);

			Pageable pageable = PageRequest.of(0, tamanhoPagina);
			Page<Long> documentos = null;
			if(orgaos!=null && orgaos.size()>0)
				documentos = documentoRepository.findDocumentoRemover(pageable, orgaos.stream().map(Long::parseLong).collect(Collectors.toList()));
			else
				documentos = documentoRepository.findDocumentoRemover(pageable);
			total = documentos.getTotalElements();
			log.info("### " + total + " documentos para remoção");
			long progresso = 0;
			long i = new Date().getTime();
			
			Map<Future<Boolean>, Future<Boolean>> mapFuture = new ConcurrentHashMap<Future<Boolean>, Future<Boolean>>();
			
			boolean continuar = total>0?true:false;
			while(continuar && !cancelar) {
				for(Long id : documentos) {
					Future<Boolean> f = executor.submit(() -> migracaoService.removerDocumento(id));
					mapFuture.put(f, f);
				}

				while(mapFuture.size()>0) {
					for(Future<Boolean> f : mapFuture.values()) {
						if(f.isDone()) {
							mapFuture.remove(f);
							progresso++;
							migrado = progresso;
						}
					}
				}

//				long t = new Date().getTime() - i;
//				t = t/1000;
//				log.info("### " + progresso + " de " + total + " documentos removidos " + df.format(progresso/t) + " registros / segundo");

				if(documentos.hasNext()) {
					if(orgaos!=null && orgaos.size()>0)
						documentos = documentoRepository.findDocumentoRemover(pageable, orgaos.stream().map(Long::parseLong).collect(Collectors.toList()));
					else
						documentos = documentoRepository.findDocumentoRemover(pageable);
				} else 
					continuar = false;
			}
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			log.error(e.getMessage(), e);
		}
		migrando = false;
	}
	
	@Async
	public void removerResto(Long divisor, Long resto) throws Exception {
		if(migrando)
			return;
		
		m = new HashMap<String, String>();
		migrando = true;
		cancelar = false;
		
		try {

			ExecutorService executor = Executors.newFixedThreadPool(tamanhoPagina);

			Pageable pageable = PageRequest.of(0, tamanhoPagina);
			Page<Long> documentos = documentoRepository.findDocumentoRemover(pageable, orgaos.stream().map(Long::parseLong).collect(Collectors.toList()));
			total = documentos.getTotalElements();
			log.info("### " + total + " documentos para remoção");
			long progresso = 0;
			long i = new Date().getTime();
			
			Map<Future<Boolean>, Future<Boolean>> mapFuture = new ConcurrentHashMap<Future<Boolean>, Future<Boolean>>();
			
			boolean continuar = total>0?true:false;
			while(continuar && !cancelar) {
				for(Long id : documentos) {
					Long r = id % divisor;
					if(resto.equals(r)) {
						Future<Boolean> f = executor.submit(() -> migracaoService.removerDocumento(id));
						mapFuture.put(f, f);
					}
				}

				while(mapFuture.size()>0) {
					for(Future<Boolean> f : mapFuture.values()) {
						if(f.isDone()) {
							mapFuture.remove(f);
							progresso++;
							migrado = progresso;
						}
					}
				}

//				long t = new Date().getTime() - i;
//				t = t/1000;
//				log.info("### " + progresso + " de " + total + " documentos removidos " + df.format(progresso/t) + " registros / segundo");

				if(documentos.hasNext()) {
					documentos = documentoRepository.findDocumentoRemover(pageable, orgaos.stream().map(Long::parseLong).collect(Collectors.toList()));
				} else 
					continuar = false;
			}
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			log.error(e.getMessage(), e);
		}
		migrando = false;
	}
	
	
	public Map<String, String> status(){
		try {
			m.put("total", total.toString());
			m.put("migrado", migrado.toString());
			m.put("migrando", migrando.toString());
		} catch (Exception e) {
			m.put("erro-status", e.getMessage());
		}
		return m;
	}
}
