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

import br.gov.sp.sempapel.migracao.service.MovimentacaoService;
import br.gov.sp.sempapel.migracao.service.ValidacaoStarterService;
import br.gov.sp.sempapel.migracao.service.MigracaoService;

@RestController
@RequestMapping("/api/movimentacao")
public class MovimentacaoController {

	@Autowired
	private MovimentacaoService movimentacaoService;
	@Autowired
	private ValidacaoStarterService validacaoStarterService;
	@Autowired
	private MigracaoService migracaoService;
	
	@RequestMapping(value = "/migrar-orgaos", method = RequestMethod.POST)
	public void migrasOrgaos(@RequestBody List<String> orgaos){
		try {
			movimentacaoService.setOrgaos(orgaos);
			movimentacaoService.migrar();
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
			movimentacaoService.setOrgaos(orgaos);
			movimentacaoService.migrar();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/migrar-movimentacao", method = RequestMethod.POST)
	public Map<String, Object> migrar(@RequestParam("id") Long id){
		Map<String, Object> m = new HashMap<String, Object>();
		try {
			migracaoService.migrarMovimentacao(id);
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			m.put("trace",e.getStackTrace());
		}
		return m;
	}
	
	@RequestMapping(value = "/list-migrar", method = RequestMethod.GET)
	public List<Map<String,Object>> listMigrar(){
		return movimentacaoService.listMigrar();
	}
	
	@RequestMapping(value = "/status-orgao", method = RequestMethod.GET)
	public List<Map<String,Object>> statusOrgao(@RequestParam("idOrgao") Long idOrgao){
		return movimentacaoService.statusOrgao(idOrgao);
	}
	
	@RequestMapping(value = "/list-sem-hash", method = RequestMethod.GET)
	public List<Map<String,Object>> listSemHash(){
		return movimentacaoService.listSemHash();
	}
	
	@RequestMapping(value = "/remover", method = RequestMethod.POST)
	public Map<String, Object> remover(@RequestParam("orgao") String orgao){
		Map<String, Object> m = new HashMap<String, Object>();
		try {
			List<String> orgaos = new ArrayList<String>();
			orgaos.add(orgao);
			movimentacaoService.setOrgaos(orgaos);
			m = movimentacaoService.status();
			movimentacaoService.remover();
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			m.put("trace",e.getStackTrace());
		}
		return m;
	}
	
	@RequestMapping(value = "/remover-resto-bd-auto", method = RequestMethod.POST)
	public Map<String, Object> removerRestoBDAutomatico(
			@RequestParam("divisor") int divisor, 
			@RequestParam("threadPoolSize") int threadPoolSize, 
			@RequestParam("evitarHost") String evitarHost,
			@RequestParam("tamanhoPagina") int tamanhoPagina,
			@RequestParam("permitirMesmoHost") boolean permitirMesmoHost){
		Map<String, Object> m = new HashMap<String, Object>();
		try {
			validacaoStarterService.incluirStartAutomatico(divisor, threadPoolSize, tamanhoPagina, evitarHost, permitirMesmoHost);
			m.put("msg","Sucesso");
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			m.put("trace",e.getStackTrace());
		}
		return m;
	}
	
	@RequestMapping(value = "/parar-execucao-bd", method = RequestMethod.POST)
	public Map<String, Object> pararExecucao(){
		Map<String, Object> m = new HashMap<String, Object>();
		try {
			validacaoStarterService.incluirStopAutomatico();
			m.put("msg","Sucesso");
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			m.put("trace",e.getStackTrace());
		}
		return m;
	}
	
	

	@RequestMapping(value = "/limpartabelainfo", method = RequestMethod.POST)
	public Map<String, Object> limparTabelaInfo(){
		Map<String, Object> m = new HashMap<String, Object>();
		try {
			validacaoStarterService.limparTabelaInfo();
			m.put("msg","Sucesso");
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			m.put("trace",e.getStackTrace());
		}
		return m;
	}
	
	
	@RequestMapping(value = "/status", method = RequestMethod.GET)
	public Map<String, Object> status(){
		Map<String, Object> m = new HashMap<String, Object>();
		try {
			return movimentacaoService.status();
		} catch (Exception e) {
			m.put("erro",e.getMessage());
		}
		return m;
	}
	
}
