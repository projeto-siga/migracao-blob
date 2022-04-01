package br.gov.sp.sempapel.migracao.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.gov.sp.sempapel.migracao.service.DocumentoService;
import br.gov.sp.sempapel.migracao.service.MigracaoService;

@RestController
@RequestMapping("/api/documento")
public class DocumentoController {

	@Autowired
	private DocumentoService documentoService;
	@Autowired
	private MigracaoService migracaoService;
	
	@RequestMapping(value = "/migrar-orgaos", method = RequestMethod.POST)
	public void migrasOrgaos(@RequestBody List<String> orgaos){
		try {
			documentoService.setOrgaos(orgaos);
			documentoService.migrar();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/migrar-orgao", method = RequestMethod.POST)
	public void migrarOrgao(@RequestParam("orgao") String orgao){
		try {
			List<String> orgaos = new ArrayList<String>();
			orgaos.add(orgao);
			documentoService.setOrgaos(orgaos);
			documentoService.migrar();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/desfazer-migracao", method = RequestMethod.POST)
	public void desfazerMigracao(@RequestParam("orgao") String orgao){
		try {
			List<String> orgaos = new ArrayList<String>();
			orgaos.add(orgao);
			documentoService.setOrgaos(orgaos);
//			documentoService.desfazerMigracao();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/migrar-documento", method = RequestMethod.POST)
	public Map<String, Object> migrar(@RequestParam("id") Long id){
		Map<String, Object> m = new HashMap<String, Object>();
		try {
			migracaoService.migrarDocumento(id);
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			m.put("trace",e.getStackTrace());
		}
		return m;
	}
	
	@RequestMapping(value = "/validar", method = RequestMethod.POST)
	public Map<String, Object> validar(@RequestParam("id") Long id){
		Map<String, Object> m = new HashMap<String, Object>();
		try {
			m.put("validado",migracaoService.validar(id));
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			m.put("trace",e.getStackTrace());
		}
		return m;
	}
	
	@RequestMapping(value = "/remover", method = RequestMethod.POST)
	public Map<String, Object> remover(@RequestParam("orgao") String orgao){
		Map<String, Object> m = new HashMap<String, Object>();
		try {
			List<String> orgaos = new ArrayList<String>();
			if(!orgao.equals("X")) {
				orgaos.add(orgao);
			}
			documentoService.setOrgaos(orgaos);
			documentoService.remover();
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			m.put("trace",e.getStackTrace());
		}
		return m;
	}
	
	@RequestMapping(value = "/remover-resto", method = RequestMethod.POST)
	public Map<String, Object> removerResto(@RequestParam("orgao") String orgao,@RequestParam("divisor") Long divisor, @RequestParam("resto") Long resto){
		Map<String, Object> m = new HashMap<String, Object>();
		try {
			List<String> orgaos = new ArrayList<String>();
			orgaos.add(orgao);
			documentoService.setOrgaos(orgaos);
			documentoService.removerResto(divisor, resto);
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			m.put("trace",e.getStackTrace());
		}
		return m;
	}
	
//	@RequestMapping(value = "/migrar-async", method = RequestMethod.GET)
//	public void migrarAsync(@RequestParam("id") Long id){
//		documentoService.migrarAsync(id);
//	}
	
	@RequestMapping(value = "/parar", method = RequestMethod.POST)
	public void parar(){
		try {
			documentoService.cancelar();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/status", method = RequestMethod.GET)
	public Map<String, String> status(){
		try {
			return documentoService.status();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@RequestMapping(value = "/status-orgao", method = RequestMethod.GET)
	public Map<String, String> statusOrgao(@RequestParam("orgao") String orgao){
		try {
			List<String> orgaos = new ArrayList<String>();
			orgaos.add(orgao);
			return documentoService.statusOrgao(orgaos);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@RequestMapping(value = "/list-migrar", method = RequestMethod.GET)
	public List<Map<String,Object>> listMigrar(){
		return documentoService.listMigrar();
	}
	
	@RequestMapping(value = "/list-sem-hash", method = RequestMethod.GET)
	public List<Map<String,Object>> listSemHash(){
		return documentoService.listSemHash();
	}
	
	@RequestMapping(value = "/gerar-hash", method = RequestMethod.GET)
	public void gerarHash(@RequestParam("idOrgao") Long idOrgao){
		documentoService.gerarHash(idOrgao);
	}
	
//	@RequestMapping(value = "/setorgaos", method = RequestMethod.GET)
//	public void setOrgaos(@RequestParam("orgao") String orgao) {
//		List<String> orgaos = new ArrayList<String>();
//		orgaos.add(orgao);
//		documentoService.setOrgaos(orgaos);
//	}
	
	@RequestMapping(value = "/orgaos", method = RequestMethod.GET)
	public List<String> getOrgaos() {
		return documentoService.getOrgaos();
	}
	
}
