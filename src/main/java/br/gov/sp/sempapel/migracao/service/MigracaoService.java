package br.gov.sp.sempapel.migracao.service;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.gov.sp.sempapel.migracao.model.CpArquivo;
import br.gov.sp.sempapel.migracao.model.CpArquivoTipoArmazenamentoEnum;
import br.gov.sp.sempapel.migracao.model.ExDocumento;
import br.gov.sp.sempapel.migracao.model.ExModelo;
import br.gov.sp.sempapel.migracao.model.ExMovimentacao;
import br.gov.sp.sempapel.migracao.model.ExPreenchimento;
import br.gov.sp.sempapel.migracao.model.TipoConteudo;
import br.gov.sp.sempapel.migracao.repository.ArquivoRepository;
import br.gov.sp.sempapel.migracao.repository.DocumentoRepository;
import br.gov.sp.sempapel.migracao.repository.ModeloRepository;
import br.gov.sp.sempapel.migracao.repository.MovimentacaoRepository;
import br.gov.sp.sempapel.migracao.repository.PreenchimentoRepository;

@Service
@Transactional
public class MigracaoService {

	private final static Logger log = Logger.getLogger(MigracaoService.class);
	
	@Autowired
	private HcpService hcpService;
	@Autowired
	private ArquivoRepository arquivoRepository;
	@Autowired
	private DocumentoRepository documentoRepository;
	@Autowired
	private MovimentacaoRepository movimentacaoRepository;
	@Autowired
	private PreenchimentoRepository preenchimentoRepository;
	@Autowired
	private ModeloRepository modeloRepository;
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public void migrarDocumento(ExDocumento documento) throws Exception {
		CpArquivo cpArquivo = new CpArquivo();
		try {
			cpArquivo.setIdOrgaoUsuario(documento.getIdOrgaoUsuario());
			cpArquivo.setConteudoTpArq(documento.getConteudoTpDoc());
			cpArquivo.setTipoArmazenamento(CpArquivoTipoArmazenamentoEnum.HCP);
			cpArquivo.setTamanho(documento.getConteudoBlobDoc().length);
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
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public void migrarMovimentacao(ExMovimentacao movimentacao) throws Exception {
		CpArquivo cpArquivo = new CpArquivo();
		try {
			cpArquivo.setConteudoTpArq(movimentacao.getConteudoTpMov());
			cpArquivo.setTipoArmazenamento(CpArquivoTipoArmazenamentoEnum.HCP);
			cpArquivo.setTamanho(movimentacao.getConteudoBlobMov().length);
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
	}
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public void migrarPreenchimento(ExPreenchimento preenchimento) throws Exception {
		CpArquivo cpArquivo = new CpArquivo();
		try {
			cpArquivo.setConteudoTpArq(TipoConteudo.TXT.getMimeType());
			cpArquivo.setTipoArmazenamento(CpArquivoTipoArmazenamentoEnum.HCP);
			cpArquivo.setTamanho(preenchimento.getPreenchimentoBlob().length);
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
	}
	
	@Transactional(rollbackOn = Exception.class, value = TxType.REQUIRES_NEW)
	public void migrarModelo(ExModelo modelo) throws Exception {
		CpArquivo cpArquivo = new CpArquivo();
		try {
			cpArquivo.setConteudoTpArq(modelo.getConteudoTpBlob());
			cpArquivo.setTipoArmazenamento(CpArquivoTipoArmazenamentoEnum.HCP);
			cpArquivo.setTamanho(modelo.getConteudoBlobMod().length);
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
	}
}
