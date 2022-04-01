package br.gov.sp.sempapel.migracao.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

	@GetMapping
	public Map<String, String> getMessages(){
		Map<String, String> m = new HashMap<String, String>();
		m.put("chave", "valor");
		return m;
	}
	
}
