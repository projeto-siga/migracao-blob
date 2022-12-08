# migracao-blob-hcp
Ferramenta para migração dos campos BLOB das tabelas do SIGA para o storage HCP

## Referências

* Binary Large Object (BLOB)
* [Hitachi Content Platform (HCP)](https://www.hitachivantara.com/en-us/products/data-storage/object-storage/content-platform.html)
* [Sistema de Gestão Administrativa (SIGA)](https://github.com/projeto-siga/siga)

## Escopo das Tabelas e Colunas a serem migradas

| TABELA                                                                                                                                    | COLUNA             |
|-------------------------------------------------------------------------------------------------------------------------------------------|--------------------|
| [SIGA.EX_DOCUMENTO](https://github.com/projeto-siga/siga/blob/develop/siga-ex/src/main/java/br/gov/jfrj/siga/ex/ExDocumento.java)         | CONTEUDO_BLOB_DOC  |
| [SIGA.EX_MODELO](https://github.com/projeto-siga/siga/blob/develop/siga-ex/src/main/java/br/gov/jfrj/siga/ex/ExModelo.java)               | CONTEUDO_BLOB_MOD  |
| [SIGA.EX_MOVIMENTACAO](https://github.com/projeto-siga/siga/blob/develop/siga-ex/src/main/java/br/gov/jfrj/siga/ex/ExMovimentacao.java)   | CONTEUDO_BLOB_MOV  |
| [SIGA.EX_PREENCHIMENTO](https://github.com/projeto-siga/siga/blob/develop/siga-ex/src/main/java/br/gov/jfrj/siga/ex/ExPreenchimento.java) | PREENCHIMENTO_BLOB |

## Configurações necessárias

As configurações necessárias para a comunicação com o banco e storage deverão ser inseridas no arquivo application.properties

### application.properties 

#### HCP

As variáveis de URL, usuário e senha do HCP são as mesmas especificadas nas properties do SIGA.
Dessa forma, a aplicação obtém os valores através das system properties.

<pre>
sempapel.hcp.uri=${siga.armazenamento.arquivo.url}

sempapel.hcp.usuario=${siga.armazenamento.arquivo.usuario}

sempapel.hcp.senha=${siga.armazenamento.arquivo.senha}
</pre>

#### Datasource

No caso da datasource, é necessário especificar a URL e declarar as varíaveis de usuário e senha.

<pre>
spring.datasource.url=jdbc:oracle:thin: ...

spring.datasource.username=${SEMPAPEL_ORACLE_DB_CFG_USUARIO}

spring.datasource.password=${SEMPAPEL_ORACLE_DB_CFG_SENHA}
</pre>

#### Observação

As configurações apresentadas poderão ser atribuídas estaticamente sem o uso de variáveis de ambiente, caso seja mais adequada ao host que executará a migração. 

##### Exemplo

<pre>
sempapel.hcp.uri=https://endereco.hcp.xyz.az

sempapel.hcp.usuario=nome_usuario_hcp

sempapel.hcp.senha=senha_usuario_hcp
</pre>

## Funcionamento

A aplicação funciona via chamadas REST para endpoints específicos de cada tabela a ser migrada.

As chamadas a seguir são exemplos de como a migração pode ser feita para os registros das tabelas.

No caso das tabelas EX_DOCUMENTO e EX_MOVIMENTACAO, será necessário passar o array com os ids dos órgãos a serem migrados.


### EX_PREENCHIMENTO

<pre>
POST {{ _.appUrl }}/api/preenchimento/migrar
</pre>

### EX_MODELO

<pre>
POST {{ _.appUrl }}/api/modelo/migrar
</pre>

### EX_DOCUMENTO

<pre>
POST {{ _.appUrl }}/api/documento/migrar-orgaos
</pre>

### EX_MOVIMENTACAO

<pre>
POST {{ _.appUrl }}/api/movimentacao/migrar-orgaos
</pre>

### Resumo da sequência de operações executadas na migração

Resumidamente, cada enpoint irá fazer uma query no banco para obter o id do registro a ser migrado.

Após isso, será criado uma nova linha na tabela CORPORATIVO.CP_ARQUIVO com o caminho de armazenamento no HCP.

O registro a ser migrado receberá a referência do ID_ARQ criado.

Concluindo, o arquivo BLOB do registro será salvo no HCP.

## Recomendações

### Pré-migração

Recomenda-se que seja feito um backup da base que será migrada.

Recomenda-se que seja executada queries para obter o total de registros de cada tabela a ser migrada, preferencialmente obter os ids desses registros.

#### Exemplo de queries para obter o total de registros a serem migrados

<pre>
SELECT count(doc.ID_DOC) FROM siga.EX_DOCUMENTO doc WHERE doc.CONTEUDO_BLOB_DOC IS NOT NULL AND doc.ID_ARQ IS NULL;

SELECT count(modelo.ID_MOD) FROM siga.EX_MODELO modelo WHERE modelo.CONTEUDO_BLOB_MOD IS NOT NULL AND modelo.ID_ARQ IS NULL;

SELECT count(mov.ID_MOV) FROM siga.EX_MOVIMENTACAO mov WHERE mov.CONTEUDO_BLOB_MOV IS NOT NULL AND mov.ID_ARQ IS NULL;

SELECT count(preenc.ID_PREENCHIMENTO) FROM siga.EX_PREENCHIMENTO preenc WHERE preenc.PREENCHIMENTO_BLOB IS NOT NULL AND preenc.ID_ARQ IS NULL;
</pre>

### Migração

Recomenda-se que as chamadas aos endpoints sejam feitas pontualmente acompanhando o andamento no console da aplicação até a conclusão.

Inicialmente, recomenda-se que a migração seja validada utilizando um único registro e tabela de menor impacto.

#### Migração de um único registro da tabela EX_PREENCHIMENTO 

<pre>
POST {{ _.appUrl }}/api/preenchimento/migrar-preenchimento?id=1316
</pre>

Após a validação da migração de alguns registros da tabela para EX_PREENCHIMENTO, recomenda-se que seja feito o mesmo procedimento para EX_MODELO, EX_DOCUMENTO e EX_MOVIMENTACAO.
