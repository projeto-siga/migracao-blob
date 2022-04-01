package br.gov.sp.sempapel.migracao.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import br.gov.sp.sempapel.migracao.model.CpArquivo;

@Service
public class HcpService {

	private final static Logger log = Logger.getLogger(HcpService.class);
	
	private static final String HCP = "HCP ";
	private static final String AUTHORIZATION = "Authorization";
	private static final String ERRO_GRAVAR_ARQUIVO = "Erro ao gravar o arquivo";
	private static final String ERRO_EXCLUIR_ARQUIVO = "Erro ao excluir o arquivo";
	private static final String ERRO_RECUPERAR_ARQUIVO = "Erro ao recuperar o arquivo";
	
	@Value("${sempapel.hcp.uri}")
	private String uri;
	@Value("${sempapel.hcp.usuario}")
	private String usuario;
	@Value("${sempapel.hcp.senha}")
	private String senha;
	
	private CloseableHttpClient client;
	private String token = null;
	
	@PostConstruct
	private void configurar() throws Exception {
		uri = System.getProperty("siga.armazenamento.arquivo.url");
		usuario = System.getProperty("siga.armazenamento.arquivo.usuario");
		senha = System.getProperty("siga.armazenamento.arquivo.senha");
		gerarToken();
		
		TrustStrategy acceptingTrustStrategy = new TrustSelfSignedStrategy();
	    SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
	    SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
	    client = HttpClients.custom().setSSLSocketFactory(csf).build();
	}
	
	private void gerarToken() {
		String usuarioBase64 = Base64.getEncoder().encodeToString(usuario.getBytes());
		String senhaMD5 = DigestUtils.md5Hex(senha.getBytes());
		token = HCP + usuarioBase64 + ":" + senhaMD5;
	}
	
	public void salvar(CpArquivo cpArquivo, byte[] conteudo) throws Exception {
		try {
			HttpPut request = new HttpPut(uri+cpArquivo.getCaminho());
			request.addHeader(AUTHORIZATION, token);
			ByteArrayEntity requestEntity = new ByteArrayEntity(conteudo);
			request.setEntity(requestEntity);
			try(CloseableHttpResponse response = client.execute(request)){
				if(response.getStatusLine().getStatusCode()!=201) {
					log.error(ERRO_GRAVAR_ARQUIVO + " " + cpArquivo.getIdArq() + "TAMANHO: " + conteudo.length);
					throw new Exception(response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
				}
			}
		} catch (Exception e) {
			log.error(ERRO_GRAVAR_ARQUIVO, cpArquivo.getIdArq(), e);
			throw e;
		}
	}
	
	public void apagar(CpArquivo cpArquivo) {
		try {
			HttpDelete request = new HttpDelete(uri+cpArquivo.getCaminho());
			request.addHeader(AUTHORIZATION, token);
			try(CloseableHttpResponse response = client.execute(request)){
				if(!(response.getStatusLine().getStatusCode()==200 || response.getStatusLine().getStatusCode()==404))
					throw new Exception(response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
			}
		} catch (Exception e) {
			log.error(ERRO_EXCLUIR_ARQUIVO, cpArquivo.getIdArq(), e);
		}
	}
	
	public void salvar(byte[] conteudo, int id) throws Exception {
		try {
			HttpPut request = new HttpPut(uri+"teste/"+id+".dat");
			request.addHeader(AUTHORIZATION, token);
			ByteArrayEntity requestEntity = new ByteArrayEntity(conteudo);
			request.setEntity(requestEntity);
			try(CloseableHttpResponse response = client.execute(request)){
				if(response.getStatusLine().getStatusCode()!=201)
					throw new Exception(response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
	public byte[] recuperar(CpArquivo cpArquivo) throws Exception {
		return recuperar(cpArquivo.getIdArq(), cpArquivo.getCaminho());
	}
	
	public byte[] recuperar(Long idArq, String caminho) throws Exception {
		if(idArq == null || caminho == null)
			return null;
		try {
			HttpGet httpGet = new HttpGet(uri+caminho);
			httpGet.addHeader(AUTHORIZATION, token);
			try(CloseableHttpResponse response = client.execute(httpGet)){
				if (response.getStatusLine().getStatusCode() == 200 ) {
					BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
					ByteArrayOutputStream bao = new ByteArrayOutputStream();
					byte[] buff = new byte[8000];
					int bytesRead = 0;
					while((bytesRead = bis.read(buff)) != -1) {
						bao.write(buff, 0, bytesRead);
					}
					return bao.toByteArray();
				} else {
					throw new Exception(response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
				}
			}
		} catch (Exception e) {
			log.error(ERRO_RECUPERAR_ARQUIVO, idArq, e);
			throw e;
		}
	}
	
	@Async
	public Future<Boolean> testeSalvar(byte[] conteudo, int id) throws Exception {
		configurar();
		try {
			HttpPut request = new HttpPut(uri+"teste/"+id+".dat");
			request.addHeader(AUTHORIZATION, token);
			ByteArrayEntity requestEntity = new ByteArrayEntity(conteudo);
			request.setEntity(requestEntity);
			try(CloseableHttpResponse response = client.execute(request)){
				if(response.getStatusLine().getStatusCode()!=201)
					throw new Exception(response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
			}
		} catch (Exception e) {
			throw e;
		}
		return new AsyncResult<Boolean>(true);
	}
	
	public void testeApagar(int id) throws Exception {
		configurar();
		try {
			HttpDelete request = new HttpDelete(uri+"teste/"+id+".dat");
			request.addHeader(AUTHORIZATION, token);
			try(CloseableHttpResponse response = client.execute(request)){
				if(!(response.getStatusLine().getStatusCode()==200 || response.getStatusLine().getStatusCode()==404))
					throw new Exception(response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
	@SuppressWarnings("unused")
	@Async
	public Future<Boolean> testeRecuperar(int id) throws Exception {
		try {
			configurar();
			HttpGet httpGet = new HttpGet(uri+"teste/"+id+".dat");
			httpGet.addHeader(AUTHORIZATION, token);
			try(CloseableHttpResponse response = client.execute(httpGet)){
				if (response.getStatusLine().getStatusCode() == 200 ) {
					BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
					ByteArrayOutputStream bao = new ByteArrayOutputStream();
					byte[] buff = new byte[8000];
					int bytesRead = 0;
					while((bytesRead = bis.read(buff)) != -1) {
						bao.write(buff, 0, bytesRead);
					}
					byte[] b = bao.toByteArray();
				} else {
					throw new Exception(response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return new AsyncResult<Boolean>(true);
	}
}
