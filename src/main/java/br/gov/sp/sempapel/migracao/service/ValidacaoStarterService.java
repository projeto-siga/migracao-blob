package br.gov.sp.sempapel.migracao.service;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
@Scope("singleton")
public class ValidacaoStarterService implements ApplicationListener<ContextRefreshedEvent> {
	private String hostname;
	private int divisor;
	private int resto;
	private int threadPoolSize;
	private int tamanhoPagina;

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	@Autowired
	private MovimentacaoService movimentacaoService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		executor.submit(() -> {
			iniciarStatusHost();

			while (true) {
				try {
					processarStart();
					processarStop();
					atualizarStatus();
				} catch (Exception e) {
					String status = "ERRO: " + e.getMessage();
					status = status.substring(0, status.length() < 50 ? status.length() : 49);
					atualizarStatusHost(status);
				}

				Thread.sleep(10000);
			}
		});
	}

	private void atualizarStatus() throws Exception {
		if (MovimentacaoService.migrando != null && MovimentacaoService.migrando) {
			String status = "EXECUTANDO (" + divisor + ";" + resto + ";" + threadPoolSize + ";" + tamanhoPagina + ")";
			atualizarStatusHost(status);
			eliminarDuplicidade(status);
		} else {
			atualizarStatusHost("DISPONIVEL");
		}
	}

	private void eliminarDuplicidade(String status) throws Exception {
		List<Map<String, Object>> hostList = listaHostAtualizado();

		for (Map<String, Object> curHost : hostList) {
			if (curHost.get("STATUS").equals(status)) {
				if (hostname.compareTo(curHost.get("VALOR") + "") < 0) {
					movimentacaoService.cancelar();
					break;
				}
			}
		}
	}

	private void processarStop() throws Exception {
		Map<String, Object> map = selecionarComando("STOP");

		if (map != null && !map.isEmpty()) {
			movimentacaoService.cancelar();
		}
	}

	private void processarStart() throws Exception {
		Map<String, Object> map = selecionarComando("START");

		if (map != null && !map.isEmpty()) {
			String parm = (String) map.get("VALOR");
			String[] tokens = parm.split(";");

			divisor = Integer.valueOf(tokens[1]);
			resto = Integer.valueOf(tokens[2]);
			threadPoolSize = Integer.valueOf(tokens[3]);
			tamanhoPagina = Integer.valueOf(tokens[4]);

			movimentacaoService.removerResto(divisor, resto, threadPoolSize, tamanhoPagina);
			Thread.sleep(1000);
		}
	}

	private Map<String, Object> selecionarComando(String comando) {
		Map<String, Object> map = null;

		try {
			String sql = "SELECT DISTINCT TIPO_DADO, VALOR, STATUS FROM SIGA.PROCESSAMENTO_HCP WHERE TIPO_DADO = ? AND VALOR LIKE ?";
			map = jdbcTemplate.queryForMap(sql, comando, hostname + "%");

			sql = "DELETE FROM SIGA.PROCESSAMENTO_HCP WHERE TIPO_DADO = ? AND VALOR LIKE ?";
			jdbcTemplate.update(sql, comando, hostname + "%");

		} catch (EmptyResultDataAccessException e) {
			// DO NOTHING
		}
		return map;
	}

	private void iniciarStatusHost() throws UnknownHostException {
		getHostName();

		String status = "DISPONIVEL";
		if (MovimentacaoService.migrando != null && MovimentacaoService.migrando)
			status = "EXECUTANDO (" + divisor + ";" + resto + ";" + threadPoolSize + ";" + tamanhoPagina + ")";

		try {
			String sql = "SELECT 1 FROM SIGA.PROCESSAMENTO_HCP WHERE TIPO_DADO = ? AND VALOR = ?";
			jdbcTemplate.queryForObject(sql, Integer.class, "HOST", hostname);

			atualizarStatusHost(status);
		} catch (EmptyResultDataAccessException e) {
			String sql = "INSERT INTO SIGA.PROCESSAMENTO_HCP VALUES(?,?,?,?,SYSDATE)";
			jdbcTemplate.update(sql, "HOST", hostname, status, "");
		}

	}

	private void getHostName() throws UnknownHostException {
		hostname = InetAddress.getLocalHost().getHostName().split("\\.")[0];

		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		String jvmName = bean.getName();
		long pid = Long.parseLong(jvmName.split("@")[0]);

		hostname = pid + "@" + hostname;
	}

	private void atualizarStatusHost(String status) throws UnknownHostException {
		String sql = "UPDATE SIGA.PROCESSAMENTO_HCP SET STATUS = ?, INFO = ?, DATA_ATUALIZACAO = SYSDATE WHERE TIPO_DADO = ? AND VALOR = ?";

		String info = "fila: " + MovimentacaoService.threadEmExecucao
				+ ", migrado: " + MovimentacaoService.migrado;

		int ret = jdbcTemplate.update(sql, status, info, "HOST", hostname);

		if (ret == 0)
			iniciarStatusHost();
	}

	public void incluirComando(String tipo, String comandoStr) {
		String[] comando = comandoStr.split("\\|");

		for (int i = 0; i < comando.length; i++) {
			String sql = "INSERT INTO SIGA.PROCESSAMENTO_HCP VALUES(?,?,?,?, SYSDATE)";

			if (!comando[i].isEmpty())
				jdbcTemplate.update(sql, tipo, comando[i], "", "");
		}
	}

	public int limparTabelaInfo() {
		String sql = "DELETE FROM SIGA.PROCESSAMENTO_HCP WHERE DATA_ATUALIZACAO < (SYSDATE - 30/60/60/24)";

		return jdbcTemplate.update(sql);
	}

	public void incluirStartAutomatico(int divisor, int threadPoolSize, int tamanhoPagina, String evitarHost, boolean permitirMesmoHost) {
		Executors.newSingleThreadExecutor().submit(() -> {
			try {
				List<Map<String, Object>> hostList = listaHostAtualizado();

				StringBuilder startStr = new StringBuilder();

				for (int resto = 0; resto < divisor; resto++) {
					if (!emExecucao(hostList, divisor, resto)) {
						String host = obterHostDisponivel(hostList, evitarHost);
						if (host == null)
							break;

						bloquearHostParaExecucao(hostList, host, permitirMesmoHost);
						startStr.append(host + ";" + divisor + ";" + resto + ";" + threadPoolSize + ";" + tamanhoPagina + "|");
					}
				}

				incluirComando("START", startStr.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private String obterHostDisponivel(List<Map<String, Object>> hostList, String evitarHost) {
		for (Map<String, Object> map : hostList) {
			String host = (String) map.get("VALOR");
			String status = (String) map.get("STATUS");
			String numHost = host.contains("-") ? host.split("-")[1] : "x";

			if (status.equals("DISPONIVEL") && !evitarHost.contains(numHost)) {
				return host;
			}
		}

		return null;
	}

	private void bloquearHostParaExecucao(List<Map<String, Object>> hostList, String host, boolean permitirMesmoHost) {
		String hostSemPid = host.split("@")[1];

		if (permitirMesmoHost)
			hostSemPid = host;

		for (Map<String, Object> map : hostList) {
			String curHost = (String) map.get("VALOR");

			if (curHost.contains(hostSemPid))
				map.put("STATUS", "EXECUTANDO");
		}
	}

	private boolean emExecucao(List<Map<String, Object>> hostList, int divisor2, int resto2) {
		for (Map<String, Object> map : hostList) {
			if (map.get("STATUS").equals("EXECUTANDO (" + divisor + ";" + resto + ";" + threadPoolSize + ";" + tamanhoPagina + ")"))
				return true;
		}

		return false;
	}

	public void incluirStopAutomatico() {
		try {
			List<Map<String, Object>> hostList = listaHostAtualizado();

			StringBuilder stopStr = new StringBuilder();

			for (Map<String, Object> map : hostList) {
				String host = (String) map.get("VALOR");
				String status = (String) map.get("STATUS");

				if (status.contains("EXECUTANDO")) {
					stopStr.append(host + "|");
				}
			}

			incluirComando("STOP", stopStr.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<Map<String, Object>> listaHostAtualizado() throws InterruptedException {
		limparTabelaInfo();

		String sql = "SELECT * FROM SIGA.PROCESSAMENTO_HCP WHERE TIPO_DADO = 'HOST'";
		return jdbcTemplate.queryForList(sql);
	}

}
