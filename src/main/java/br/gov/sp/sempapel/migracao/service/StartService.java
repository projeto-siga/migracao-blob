package br.gov.sp.sempapel.migracao.service;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class StartService {
	
	private final static Logger log = Logger.getLogger(StartService.class);

	@Value("${sempapel.migrar.modelo}")
	private Boolean migrarModelo;
	@Value("${sempapel.migrar.preenchimento}")
	private Boolean migrarPreenchimento;
	@Value("${sempapel.migrar.movimentacao}")
	private Boolean migrarMovimentacao;
	@Value("${sempapel.migrar.documento}")
	private Boolean migrarDocumento;
	
	@Autowired
	private DocumentoService documentoService;
	@Autowired
	private MovimentacaoService movimentacaoService;
	@Autowired
	private ModeloService modeloService;
	@Autowired
	private PreenchimentoService preenchimentoService;
	@Autowired
	private MigracaoService migracaoService;
	
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
//    	start();
    }
//	@PostConstruct
	public void start() {
		try {
			
//			migracaoService.excluirApagados();
			
			log.info("### Verificando divergência de tamanho");
//			documentoService.conferirTamanho();

			log.info("### Migração iniciada");
//			if(migrarModelo) {
//				log.info("### Migrando modelos");
//				modeloService.migrar();
//			}
//			if(migrarPreenchimento) {
//				log.info("### Migrando preenchimento");
//				preenchimentoService.migrar();
//			}
//			if(migrarDocumento) {
//				log.info("### Migrando documentos");
//				documentoService.migrar();
//			}
//			if(migrarMovimentacao) {
//				log.info("### Migrando movimentações");
//				movimentacaoService.migrar();
//			}
			
			
			log.info("### Migração Finalizada");

		} catch (Exception e) {
			log.info("### Falha na migração " + e.getMessage());
			log.error(e.getMessage(), e);
		}
	}
}
