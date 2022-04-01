package br.gov.sp.sempapel.migracao.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import br.gov.sp.sempapel.migracao.service.MigracaoService;

@RestController
@RequestMapping("/api/arquivo")
public class ArquivoController {

	@Autowired
	private MigracaoService migracaoService;
	
	@RequestMapping(value = "/excluir", method = RequestMethod.POST)
	public void start(){
		try {
			migracaoService.excluirApagados();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/status", method = RequestMethod.GET)
	public Map<String, Long> status(){
		try {
			return migracaoService.statusOld();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@RequestMapping(value = "/erros", method = RequestMethod.GET)
	public List<String> erros(){
		return migracaoService.getErros();
	}
	
}
