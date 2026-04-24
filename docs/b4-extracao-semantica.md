# B4 - Extracao semantica por tipo IBM

## Escopo implementado
A Etapa B4 aplica estrategias de extracao semantica por tipo classificado no B3:
- `MSG_SISTEMA`
- `INTEGRACAO_UC`
- `INTEGRACAO_DI`
- `API_QUARKUS`
- `CANAIS_UC`
- `ESPECIFICACAO_SUPLEMENTAR`

## Componentes
- `br.com.expertdev.gid.service.IBMB4ExtractionService`
- `br.com.expertdev.gid.service.extractor.IBMExtractorFactory`
- `br.com.expertdev.gid.service.extractor.*ExtractorStrategy`
- `br.com.expertdev.gid.service.IBMB4Probe`

## Como validar com probe

```powershell
mvn -f "C:\Users\marcu\workspace\Pessoal\expertDev\pom.xml" -DskipTests exec:java "-Dexec.mainClass=br.com.expertdev.gid.service.IBMB4Probe" "-Dexec.args=C:\caminho\doc1.doc C:\caminho\doc2.doc"
```

## Saida esperada
Para cada arquivo:
- tipo detectado
- confianca da classificacao
- RTC/UC detectados
- contadores semanticos conforme tipo (mensagens, regras, fluxos, parametros, urls)
- avisos de leitura/conversao/fallback

