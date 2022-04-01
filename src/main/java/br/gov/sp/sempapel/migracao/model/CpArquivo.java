/*******************************************************************************
 * Copyright (c) 2006 - 2011 SJRJ.
 * 
 *     This file is part of SIGA.
 * 
 *     SIGA is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     SIGA is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with SIGA.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package br.gov.sp.sempapel.migracao.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A class that represents a row in the CP_ARQUIVO table. You can customize the
 * behavior of this class by editing the class, {@link CpArquivo()}.
 */
@Entity
@Table(name = "CP_ARQUIVO", schema = "CORPORATIVO")
@Getter @Setter @NoArgsConstructor
public class CpArquivo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(sequenceName = "CORPORATIVO.CP_ARQUIVO_SEQ", name = "CP_ARQUIVO_SEQ", allocationSize = 1)
	@GeneratedValue(generator = "CP_ARQUIVO_SEQ")
	@Column(name = "ID_ARQ")
	private java.lang.Long idArq;

	@Column(name = "ID_ORGAO_USU")
	private Long idOrgaoUsuario;

	@Column(name = "CONTEUDO_TP_ARQ", length = 128)
	private java.lang.String conteudoTpArq;

	@Enumerated(EnumType.STRING)
	@Column(name = "TP_ARMAZENAMENTO")
	private CpArquivoTipoArmazenamentoEnum tipoArmazenamento;
	
	@Column(name = "CAMINHO")
	private String caminho;
	
	@Column(name = "HASH_MD5")
	private String hash;
	
	@Column(name = "TAMANHO_ARQ")
	private Integer tamanho = 0;
	
	public void gerarCaminho(Date data) {
		String extensao;
		
		TipoConteudo t = TipoConteudo.getByMimeType(getConteudoTpArq());
		if(t!=null)
			extensao = t.getExtensao();
		else
			extensao = TipoConteudo.ZIP.getExtensao();
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.AM_PM, Calendar.PM);
		if(data!=null)
			c.setTime(data);
		this.caminho = c.get(Calendar.YEAR)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.DATE)+"/"+c.get(Calendar.HOUR_OF_DAY)+"/"+c.get(Calendar.MINUTE)+"/"+UUID.randomUUID().toString()+"."+extensao;
	}

}