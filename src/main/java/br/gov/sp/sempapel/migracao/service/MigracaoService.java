package br.gov.sp.sempapel.migracao.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import br.gov.sp.sempapel.migracao.model.CpArquivo;
import br.gov.sp.sempapel.migracao.model.CpArquivoExcluir;
import br.gov.sp.sempapel.migracao.model.CpArquivoTipoArmazenamentoEnum;
import br.gov.sp.sempapel.migracao.model.ExDocumento;
import br.gov.sp.sempapel.migracao.model.ExModelo;
import br.gov.sp.sempapel.migracao.model.ExMovimentacao;
import br.gov.sp.sempapel.migracao.model.ExPreenchimento;
import br.gov.sp.sempapel.migracao.model.TipoConteudo;
import br.gov.sp.sempapel.migracao.repository.ArquivoExcluirRepository;
import br.gov.sp.sempapel.migracao.repository.ArquivoRepository;
import br.gov.sp.sempapel.migracao.repository.DocumentoRepository;
import br.gov.sp.sempapel.migracao.repository.ModeloRepository;
import br.gov.sp.sempapel.migracao.repository.MovimentacaoRepository;
import br.gov.sp.sempapel.migracao.repository.PreenchimentoRepository;

@Service
@Transactional
public class MigracaoService {

	private final static Logger log = Logger.getLogger(MigracaoService.class);

	private DecimalFormat df = new DecimalFormat("#.##");
	private static final int tamanhoPagina = 100;
	
	@Autowired
	private HcpService hcpService;
	@Autowired
	private ArquivoRepository arquivoRepository;
	@Autowired
	private ArquivoExcluirRepository arquivoExcluirRepository;
	@Autowired
	private DocumentoRepository documentoRepository;
	@Autowired
	private MovimentacaoRepository movimentacaoRepository;
	@Autowired
	private PreenchimentoRepository preenchimentoRepository;
	@Autowired
	private ModeloRepository modeloRepository;
	
	List<String> listErros = new ArrayList<>();
	
	private static Long total= 0l;
	private static Long progresso = 0l;
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public Boolean migrarDocumento(Long id) throws Exception {
		ExDocumento documento = documentoRepository.findById(id).get();
		if(documento.getConteudoBlobDoc().length>0) {
			CpArquivo cpArquivo = new CpArquivo();
			try {
				cpArquivo.setIdOrgaoUsuario(documento.getIdOrgaoUsuario());
				cpArquivo.setConteudoTpArq(documento.getConteudoTpDoc());
				cpArquivo.setTipoArmazenamento(CpArquivoTipoArmazenamentoEnum.HCP);
				cpArquivo.setTamanho(documento.getConteudoBlobDoc().length);
				cpArquivo.setHash(getHash(documento.getConteudoBlobDoc()));
				cpArquivo.gerarCaminho(documento.getDtDoc());
				cpArquivo = arquivoRepository.save(cpArquivo);
				documento.setCpArquivo(cpArquivo);
				documentoRepository.save(documento);
				hcpService.salvar(cpArquivo, documento.getConteudoBlobDoc());
			} catch (Exception e) {
				log.error(e);
				hcpService.apagar(cpArquivo);
				throw e;
			}
		}
		return true;
	}
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public Boolean migrarMovimentacao(Long id) throws Exception {
		ExMovimentacao movimentacao = movimentacaoRepository.findById(id).get();
		CpArquivo cpArquivo = new CpArquivo();
		try {
			cpArquivo.setConteudoTpArq(movimentacao.getConteudoTpMov());
			cpArquivo.setTipoArmazenamento(CpArquivoTipoArmazenamentoEnum.HCP);
			cpArquivo.setTamanho(movimentacao.getConteudoBlobMov().length);
			cpArquivo.setHash(getHash(movimentacao.getConteudoBlobMov()));
			cpArquivo.gerarCaminho(movimentacao.getDtMov());
			cpArquivo = arquivoRepository.save(cpArquivo);
			movimentacao.setCpArquivo(cpArquivo);
			movimentacaoRepository.save(movimentacao);
			hcpService.salvar(cpArquivo, movimentacao.getConteudoBlobMov());
		} catch (Exception e) {
			log.error(e);
			hcpService.apagar(cpArquivo);
			throw e;
		}
		return true;
	}
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public Boolean migrarPreenchimento(Long id) throws Exception {
		ExPreenchimento preenchimento = preenchimentoRepository.findById(id).get();
		CpArquivo cpArquivo = new CpArquivo();
		try {
			cpArquivo.setConteudoTpArq(TipoConteudo.TXT.getMimeType());
			cpArquivo.setTipoArmazenamento(CpArquivoTipoArmazenamentoEnum.HCP);
			cpArquivo.setTamanho(preenchimento.getPreenchimentoBlob().length);
			cpArquivo.setHash(getHash(preenchimento.getPreenchimentoBlob()));
			cpArquivo.gerarCaminho(null);
			cpArquivo = arquivoRepository.save(cpArquivo);
			preenchimento.setCpArquivo(cpArquivo);
			preenchimentoRepository.save(preenchimento);
			hcpService.salvar(cpArquivo, preenchimento.getPreenchimentoBlob());
		} catch (Exception e) {
			log.error(e);
			hcpService.apagar(cpArquivo);
			throw e;
		}
		return true;
	}
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public Boolean migrarModelo(Long id) throws Exception {
		ExModelo modelo = modeloRepository.findById(id).get();
		CpArquivo cpArquivo = new CpArquivo();
		try {
			cpArquivo.setConteudoTpArq(modelo.getConteudoTpBlob());
			cpArquivo.setTipoArmazenamento(CpArquivoTipoArmazenamentoEnum.HCP);
			cpArquivo.setTamanho(modelo.getConteudoBlobMod().length);
			cpArquivo.setHash(getHash(modelo.getConteudoBlobMod()));
			cpArquivo.gerarCaminho(null);
			cpArquivo = arquivoRepository.save(cpArquivo);
			modelo.setCpArquivo(cpArquivo);
			modeloRepository.save(modelo);
			hcpService.salvar(cpArquivo, modelo.getConteudoBlobMod());
		} catch (Exception e) {
			log.error(e);
			hcpService.apagar(cpArquivo);
			throw e;
		}
		return true;
	}
	
	public Boolean validar(Long id) throws Exception {
		ExDocumento documento = documentoRepository.findById(id).get();
		byte[] arq = hcpService.recuperar(documento.getCpArquivo());
		String md5 = getHash(arq);
		if(md5.equals(documento.getCpArquivo().getHash())) {
			return true;
		} else
			return false;
	}
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public Boolean removerDocumento(Long id) throws Exception {
		ExDocumento documento = documentoRepository.findById(id).get();
		byte[] arq = hcpService.recuperar(documento.getCpArquivo());
		String md5 = getHash(arq);
		if(md5.equals(documento.getCpArquivo().getHash())) {
			documento.setApagado("1");
			documentoRepository.save(documento);
		}else {
			documento.setApagado("0");
			documentoRepository.save(documento);
			addErro("Documento "+id+"com HASH imcompatível");
		}
		return true;
	}
	
	@Transactional
	public Boolean removerMovimentacao(BigDecimal id) throws Exception {
		try {
			Map<String, Object> cpArquivo = movimentacaoRepository.findInformacaoArquivo(id);
			
			Long idArq = ((BigDecimal) cpArquivo.get("ID_ARQ")).longValue();
			String caminho = (String) cpArquivo.get("CAMINHO");
			String hash = (String) cpArquivo.get("HASH_MD5");
			
			byte[] arq = hcpService.recuperar(idArq, caminho);

			String md5 = null;
			if(arq != null)
				md5 = getHash(arq);
			
			String apagado = "0";
			if(md5 != null && hash != null && md5.equals(hash)) 
				apagado = "1";
			
			movimentacaoRepository.atualizarApagado(id, apagado);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public Boolean removerModelo(Long id) throws Exception {
		ExModelo modelo = modeloRepository.findById(id).get();
		byte[] arq = hcpService.recuperar(modelo.getCpArquivo());
		String md5 = getHash(arq);
		if(md5.equals(modelo.getCpArquivo().getHash())) {
			modelo.setConteudoBlobMod(null);
			modeloRepository.save(modelo);
		}else {
			addErro("Modelo "+id+"com HASH imcompatível");
		}
		return true;
	}
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public Boolean removerPreenchimento(Long id) throws Exception {
		ExPreenchimento preenchimento = preenchimentoRepository.findById(id).get();
		byte[] arq = hcpService.recuperar(preenchimento.getCpArquivo());
		String md5 = getHash(arq);
		if(md5.equals(preenchimento.getCpArquivo().getHash())) {
			preenchimento.setPreenchimentoBlob(null);
			preenchimentoRepository.save(preenchimento);
		}else {
			addErro("Preenchimento "+id+"com HASH imcompatível");
		}
		return true;
	}
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public Boolean gerarHashDocumento(Long id) throws Exception {
		ExDocumento documento = documentoRepository.findById(id).get();
		if(documento.getConteudoBlobDoc().length>0) {
			CpArquivo cpArquivo = documento.getCpArquivo();
			try {
				cpArquivo.setHash(getHash(documento.getConteudoBlobDoc()));
				cpArquivo = arquivoRepository.save(cpArquivo);
			} catch (Exception e) {
				log.error(e);
				throw e;
			}
		}
		return true;
	}
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public Boolean gerarHashMovimentacao(Long id) throws Exception {
		ExMovimentacao movimentacao = movimentacaoRepository.findById(id).get();
		CpArquivo cpArquivo = movimentacao.getCpArquivo();
		try {
			cpArquivo.setHash(getHash(movimentacao.getConteudoBlobMov()));
			cpArquivo = arquivoRepository.save(cpArquivo);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
		return true;
	}
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public Boolean gerarHashPreenchimento(Long id) throws Exception {
		ExPreenchimento preenchimento = preenchimentoRepository.findById(id).get();
		CpArquivo cpArquivo = preenchimento.getCpArquivo();
		try {
			cpArquivo.setHash(getHash(preenchimento.getPreenchimentoBlob()));
			cpArquivo = arquivoRepository.save(cpArquivo);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
		return true;
	}
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public Boolean gerarHashModelo(Long id) throws Exception {
		ExModelo modelo = modeloRepository.findById(id).get();
		CpArquivo cpArquivo = modelo.getCpArquivo();
		try {
			cpArquivo.setHash(getHash(modelo.getConteudoBlobMod()));
			cpArquivo = arquivoRepository.save(cpArquivo);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
		return true;
	}
	
	@Async
	public void excluirApagados() {
		Pageable pageable = PageRequest.of(0, tamanhoPagina);
		Page<CpArquivoExcluir> documentos = arquivoExcluirRepository.findAll(pageable);
		total = documentos.getTotalElements();
		log.info("### " + total + " documentos para exclusão");
		progresso = 0l;
		long i = new Date().getTime();
		
		boolean continuar = total>0?true:false;
		while(continuar) {
			for(CpArquivoExcluir doc : documentos) {
				CpArquivo cpArquivo = new CpArquivo();
				cpArquivo.setCaminho(doc.getCaminho());
				hcpService.apagar(cpArquivo);
				arquivoExcluirRepository.delete(doc);
			}
			
			long t = new Date().getTime() - i;
			t = t/1000;
			progresso += documentos.getSize();
			if(progresso > total)
				progresso = total;
			log.info("### " + progresso + " de " + total + " documentos excluídos " + df.format(progresso/t) + " registros / segundo");
			
			if(documentos.hasNext()) {
				documentos = arquivoExcluirRepository.findAll(pageable);
			} else 
				continuar = false;
		}
	}
	
	public Map<String, Long> statusOld(){
		if(total.equals(0l)) {
			Pageable pageable = PageRequest.of(0, tamanhoPagina);
			Page<CpArquivoExcluir> documentos = arquivoExcluirRepository.findAll(pageable);
			total = documentos.getTotalElements();
		}
		Map<String, Long> m = new HashMap<String, Long>();
		m.put("total", total);
		m.put("excluido", progresso);
		return m;
	}
	
	private String getHash(byte[] doc) {
		return DigestUtils.md5Hex(doc);
	}
	
//	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
//	public Boolean desfazerMigracao(Long id) throws Exception {
//		ExDocumento documento = documentoRepository.findById(id).get();
//		if(documento.getConteudoBlobDoc().length>0) {
//			try {
//				CpArquivo cpArquivo = documento.getCpArquivo();
//				documento.setCpArquivo(null);
//				hcpService.apagar(cpArquivo);
//				documentoRepository.save(documento);
//				arquivoRepository.delete(cpArquivo);
//			} catch (Exception e) {
//				log.error(e);
//				throw e;
//			}
//		}
//		return true;
//	}
	
	private void addErro(String msg) {
		listErros.add(Instant.now()+" | "+msg);
		if(listErros.size()>100)
			listErros.remove(0);
	}
	
	public List<String> getErros(){
		return listErros;
	}

}
