package br.gov.sp.sempapel.migracao.service.test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.gov.sp.sempapel.migracao.service.HcpService;

@Service
public class TesteService {
	
	private final static Logger log = Logger.getLogger(TesteService.class);

	@Autowired
	private HcpService hcpService;
	
	@Value("${hcp.test.halitado}")
	private boolean habilitado;
	@Value("${hcp.test.total.arquivo}")
	private Integer total;
	@Value("${hcp.test.tamanho.minimo}")
	private Integer tamanhoMinimo;
	@Value("${hcp.test.tamanho.maximo}")
	private Integer tamanhoMaximo;
	@Value("${hcp.test.tamanho.incremento}")
	private Integer incrementoTamanho;
	@Value("${hcp.test.thread.minimo}")
	private Integer threadMinimo;
	@Value("${hcp.thread.maximo}")
	private Integer threadMaximo;
	@Value("${hcp.test.thread.incremento}")
	private Integer incrementoThread;
	
	@PostConstruct
	public void init() {
		if(habilitado) {
			log.info("### Teste de carga do HCP");
			Integer tamanho = tamanhoMinimo;
			log.info("-----------------------------------------------");
			while(tamanho<=tamanhoMaximo) {
				Integer threads = threadMinimo;
				while(threads<=threadMaximo) {
					log.info("Configuração do teste "+tamanho+"Mb, "+total+" aquivos, "+threads+" threads");
					apagar(total);
					gravar(total, tamanho, threads);
					recuperar(total, tamanho, threads);
					log.info("-----------------------------------------------");
					threads+=incrementoThread;
				}
				tamanho+=incrementoTamanho;
			}
			apagar(total);
			log.info("### Teste de carga do HCP concluído");
		}
	}
	
	private void apagar(int ultimoId) {
		try {
			long ini = System.currentTimeMillis();
			for(int i = 1; i<=ultimoId; i++)
				hcpService.testeApagar(i);
			long fim = System.currentTimeMillis();
			long t = (fim-ini)/1000;
			log.info("Exclusão: "+t+"s no total");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void gravar(int qntd, int tam, int pool) {
		try {
			Map<Future<Boolean>, Future<Boolean>> map = new ConcurrentHashMap<Future<Boolean>, Future<Boolean>>();
			byte[] a = gerarArquivo(tam);
			long ini = System.currentTimeMillis();
			for(int i = 1; i<=qntd; i++) {
				Future<Boolean> f = hcpService.testeSalvar(a, i);
				map.put(f, f);
				while(verificaPool(map)>=pool) {
//					System.out.println("Aguardando liberação do Pool");
				}
			}
			while(verificaPool(map)>0) {
//				System.out.println("Aguardando conclusão do Pool");
			}
			long fim = System.currentTimeMillis();
			long t = (fim-ini)/1000;
			log.info("Gravação: "+(qntd/t)+" arq/s | "+t+"s no total");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void recuperar(int qntd, int tam, int pool) {
		try {
			Map<Future<Boolean>, Future<Boolean>> map = new ConcurrentHashMap<Future<Boolean>, Future<Boolean>>();
			long ini = System.currentTimeMillis();
			for(int i = 1; i<=qntd; i++) {
				Future<Boolean> f = hcpService.testeRecuperar(i);
				map.put(f, f);
				while(verificaPool(map)>=pool) {
//					System.out.println("Aguardando liberação do Pool");
				}
			}
			while(verificaPool(map)>0) {
//				System.out.println("Aguardando conclusão do Pool");
			}
			long fim = System.currentTimeMillis();
			long t = (fim-ini)/1000;
			log.info("Recuperação: "+(qntd/t)+" arq/s | "+t+"s no total");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int verificaPool(Map<Future<Boolean>, Future<Boolean>> map) {
		Set<Future<Boolean>> s = map.keySet();
		for(Future<Boolean> f : s) {
			if(f.isDone())
				map.remove(f);
		}
		return map.size();
	}

	private byte[] gerarArquivo(int tamMega) {
		byte[] a = new byte[tamMega*1024*1024];
		return a;
	}
	
}
