package br.gov.sp.sempapel.migracao.service;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import br.gov.sp.sempapel.migracao.model.ExModelo;
import br.gov.sp.sempapel.migracao.repository.ModeloRepository;

@Service
@Scope("singleton")
public class ModeloService {

	private final static Logger log = Logger.getLogger(ModeloService.class);
	private DecimalFormat df = new DecimalFormat("#.##");
	
	private static final int tamanhoPagina = 100;
	
	@Autowired
	private ModeloRepository modeloRepository;
	
	@Autowired
	private MigracaoService migracaoService;
	
	private Long total= 0l;
	private Long migrado = 0l;
	private Boolean cancelar = false;
	private Boolean migrando = false;
	
	private Map<String, String> m = new HashMap<String, String>();
	
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
			Page<Long> modelos = findModelos(pageable);
			total = modelos.getTotalElements();
			log.info("### " + total + " modelos para migração");
			long progresso = 0;
			long i = new Date().getTime();
			
			Map<Future<Boolean>, Future<Boolean>> mapFuture = new ConcurrentHashMap<Future<Boolean>, Future<Boolean>>();
			
			boolean continuar = total>0?true:false;
			while(continuar && !cancelar) {
				for(Long id : modelos) {
					Future<Boolean> f = executor.submit(() -> migracaoService.migrarModelo(id));
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
				log.info("### " + progresso + " de " + total + " modelos migrados " + df.format(progresso/t) + " registros / segundo");
				
				if(modelos.hasNext()) {
					modelos = findModelos(pageable);
				} else 
					continuar = false;
			}
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			log.error(e.getMessage(), e);
		}
		migrando = false;
	}
	
	private Page<Long> findModelos(Pageable pageable){
		return modeloRepository.findModeloModoBlob(pageable);
	}
	
	public List<Map<String,Object>> listMigrar() {
		return modeloRepository.listMigrar();
	}
	
	public List<Map<String,Object>> listSemHash() {
		return modeloRepository.listSemHash();
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
			Page<Long> modelos = modeloRepository.findModeloRemover(pageable);
			total = modelos.getTotalElements();
			log.info("### " + total + " modelos para remoção");
			long progresso = 0;
			long i = new Date().getTime();

			Map<Future<Boolean>, Future<Boolean>> mapFuture = new ConcurrentHashMap<Future<Boolean>, Future<Boolean>>();

			boolean continuar = total>0?true:false;
			while(continuar && !cancelar) {
				for(Long id : modelos) {
					Future<Boolean> f = executor.submit(() -> migracaoService.removerModelo(id));
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
//				log.info("### " + progresso + " de " + total + " modelos removidos " + df.format(progresso/t) + " registros / segundo");

				if(modelos.hasNext()) {
					modelos = modeloRepository.findModeloRemover(pageable);
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
