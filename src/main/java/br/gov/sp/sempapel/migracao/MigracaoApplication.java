package br.gov.sp.sempapel.migracao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaRepositories
@EnableAsync
@EntityScan
public class MigracaoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MigracaoApplication.class, args);
	}

}
