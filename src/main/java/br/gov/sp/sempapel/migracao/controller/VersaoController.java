package br.gov.sp.sempapel.migracao.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/versao")
public class VersaoController {

	@Value("${versao}")
	private String versao;
	
	@RequestMapping(value = "/versao", method = RequestMethod.GET)
	public Map<String, String> versao(){
		Map<String, String> m = new HashMap<String, String>();
		m.put("versao", versao);
		return m;
	}

}
