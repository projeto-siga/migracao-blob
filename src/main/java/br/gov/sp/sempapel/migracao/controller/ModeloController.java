package br.gov.sp.sempapel.migracao.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.gov.sp.sempapel.migracao.service.ModeloService;

@RestController
@RequestMapping("/api/modelo")
public class ModeloController {

	@Autowired
	private ModeloService modeloService;
	
	@RequestMapping(value = "/migrar", method = RequestMethod.POST)
	public void migrarOrgao(){
		try {
			modeloService.migrar();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/list-migrar", method = RequestMethod.GET)
	public List<Map<String,Object>> listMigrar(){
		return modeloService.listMigrar();
	}
	
	@RequestMapping(value = "/list-sem-hash", method = RequestMethod.GET)
	public List<Map<String,Object>> listSemHash(){
		return modeloService.listSemHash();
	}
	
	@RequestMapping(value = "/remover", method = RequestMethod.POST)
	public Map<String, Object> remover(){
		Map<String, Object> m = new HashMap<String, Object>();
		try {
			modeloService.remover();
		} catch (Exception e) {
			m.put("erro",e.getMessage());
			m.put("trace",e.getStackTrace());
		}
		return m;
	}
	
	@RequestMapping(value = "/status", method = RequestMethod.GET)
	public Map<String, String> status(){
		Map<String, String> m = new HashMap<String, String>();
		try {
			return modeloService.status();
		} catch (Exception e) {
			m.put("erro",e.getMessage());
		}
		return m;
	}
	
}
