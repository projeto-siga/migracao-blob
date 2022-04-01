package br.gov.sp.sempapel.migracao.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import br.gov.sp.sempapel.migracao.repository.MovimentacaoRepository;

@Service
@Scope("singleton")
public class MovimentacaoService {

	private final static Logger log = Logger.getLogger(MovimentacaoService.class);
	private DecimalFormat df = new DecimalFormat("#.##");
	
	private static final int tamanhoPagina = 100;
	
	private List<String> orgaos;
	
	@Autowired
	private MovimentacaoRepository movimentacaoRepository;
	
	@Autowired
	private MigracaoService migracaoService;
	
	public static Long total= 0l;
	public static Long migrado = 0l;
	public static Boolean cancelar = false;
	public static Boolean migrando = false;
	public static int threadEmExecucao = 0;
	
	private Map<String, Object> m = new HashMap<String, Object>();
	
	public void setOrgaos(List<String> orgaos) {
		this.orgaos = orgaos;
	}
	
	public void cancelar() throws Exception {
		cancelar=true;
	}
	
	@Async
	public void migrar() throws Exception {
		if(migrando)
			return;
		
		m = new HashMap<String, Object>();
		migrando = true;
		cancelar = false;
		
		try {
			ExecutorService executor = Executors.newFixedThreadPool(tamanhoPagina);
			
			Pageable pageable = PageRequest.of(0, tamanhoPagina);
			Page<Long> movimentacoes = findMovimentacaos(pageable);
			total = movimentacoes.getTotalElements();
			log.info("### " + total + " movimentações para migração");
			long progresso = 0;
			long i = new Date().getTime();
			
			Map<Future<Boolean>, Future<Boolean>> mapFuture = new ConcurrentHashMap<Future<Boolean>, Future<Boolean>>();
			
			boolean continuar = total>0?true:false;
			while(continuar && !cancelar) {
				for(Long id : movimentacoes) {
					Future<Boolean> f = executor.submit(() -> migracaoService.migrarMovimentacao(id));
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
				log.info("### " + progresso + " de " + total + " movimentações migradas " + df.format(progresso/t) + " registros / segundo");
				
				if(movimentacoes.hasNext()) {
					movimentacoes = findMovimentacaos(pageable);
				} else 
					continuar = false;
			}
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			log.error(e.getMessage(), e);
		}
		migrando = false;
	}
	
	private Page<Long> findMovimentacaos(Pageable pageable){
		if(orgaos!=null && orgaos.size()>0)
			return movimentacaoRepository.findMovimentacaoModoBlob(pageable, orgaos.stream().map(Long::parseLong).collect(Collectors.toList()));
		else
			return movimentacaoRepository.findMovimentacaoModoBlob(pageable);
	}
	
	public List<Map<String,Object>> listMigrar() {
		return movimentacaoRepository.listMigrar();
	}
	
	public List<Map<String,Object>> statusOrgao(Long idOrgao) {
		return movimentacaoRepository.statusOrgao(idOrgao);
	}
	
	public List<Map<String,Object>> listSemHash() {
		return movimentacaoRepository.listSemHash();
	}
	
	@Async
	public void remover() throws Exception {
		if(migrando)
			return;
		
		m = new HashMap<String, Object>();
		migrando = true;
		cancelar = false;
		
		try {

			ExecutorService executor = Executors.newFixedThreadPool(tamanhoPagina);

			Pageable pageable = PageRequest.of(0, tamanhoPagina);
			Page<Long> movimentacoes = movimentacaoRepository.findMovimentacaoRemover(pageable, orgaos.stream().map(Long::parseLong).collect(Collectors.toList()));
			total = movimentacoes.getTotalElements();
			log.info("### " + total + " movimentações para remoção");
			long progresso = 0;

			Map<Future<Boolean>, Future<Boolean>> mapFuture = new ConcurrentHashMap<Future<Boolean>, Future<Boolean>>();

			boolean continuar = total>0?true:false;
			while(continuar && !cancelar) {
				for(Long id : movimentacoes) {
					Future<Boolean> f = executor.submit(() -> migracaoService.removerMovimentacao(BigDecimal.valueOf(id)));
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

				if(movimentacoes.hasNext()) {
					movimentacoes = movimentacaoRepository.findMovimentacaoRemover(pageable, orgaos.stream().map(Long::parseLong).collect(Collectors.toList()));
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
	public void removerResto(int divisor, int resto, int threadPoolSize, int tamanhoPagina) throws Exception {
		if(migrando)
			return;
		
		m = new HashMap<String, Object>();
		migrando = true;
		cancelar = false;
		migrado = 0L;
		threadEmExecucao = 0;
		
		try {
			ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
			
			List<BigDecimal> listIdMov = new ArrayList<>();
			listIdMov.add(BigDecimal.valueOf(1));
			List<BigDecimal> movimentacoes = movimentacaoRepository.findMovimentacaoRemover(tamanhoPagina, divisor, resto, listIdMov);
			
			Map<Future<Boolean>, Future<Boolean>> mapFuture = new ConcurrentHashMap<Future<Boolean>, Future<Boolean>>();

			while(movimentacoes.size() > 0 && !cancelar) {
				for(BigDecimal id : movimentacoes) {
					Future<Boolean> f = executor.submit(() -> migracaoService.removerMovimentacao(id));
					mapFuture.put(f, f);
					threadEmExecucao++;
				}
				
				while(threadEmExecucao >= tamanhoPagina && !cancelar) {
					Thread.sleep(20000);
					for(Future<Boolean> f : mapFuture.values()) {
						if(f.isDone()) {
							mapFuture.remove(f);
							migrado++;
							threadEmExecucao--;							
						}
					}
				}
				
				movimentacoes = movimentacaoRepository.findMovimentacaoRemover(tamanhoPagina, divisor, resto, movimentacoes);
			}
			
			if(cancelar) 
				executor.shutdownNow();
			
			while(!executor.awaitTermination(10, TimeUnit.SECONDS) && !mapFuture.isEmpty()) {
				for(Future<Boolean> f : mapFuture.values()) {
					if(f.isDone()) {
						mapFuture.remove(f);
						migrado++;
						threadEmExecucao--;							
					}
				}
			}
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			log.error(e.getMessage(), e);
		}
		threadEmExecucao = 0;
		migrando = false;
	}

	public Map<String, Object> status(){
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
