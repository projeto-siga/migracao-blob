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

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "EX_MODELO_OLD", schema = "SIGA")
@DynamicUpdate
@Getter @Setter @NoArgsConstructor
public class ExModelo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(sequenceName = "EX_MODELO_SEQ", name = "EX_MODELO_SEQ", allocationSize = 1)
	@GeneratedValue(generator = "EX_MODELO_SEQ")
	@Column(name = "ID_MOD", unique = true, nullable = false)
	private java.lang.Long idMod;
	
	/** The value of the simple conteudoTpBlob property. */
	@Column(name = "CONTEUDO_TP_BLOB", length = 128)
	private java.lang.String conteudoTpBlob;
	
	@Lob
	@Column(name = "CONTEUDO_BLOB_MOD")
	@Basic(fetch = FetchType.LAZY)
	private byte[] conteudoBlobMod;

	@ManyToOne(fetch = FetchType.LAZY, optional = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "ID_ARQ")
	private CpArquivo cpArquivo;
	
}