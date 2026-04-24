# B3 - Classificacao de layout e filtro de ruido IBM

## Escopo implementado
- Classificacao automatica de tipo de artefato IBM por nome de arquivo + sinais de conteudo.
- Filtro de ruido para metadados Word/OLE2/AIP comuns.
- Runner de validacao para lote de arquivos Word.

## Componentes
- `br.com.expertdev.gid.service.IBMRuidoFilter`
- `br.com.expertdev.gid.service.IBMLayoutDetector`
- `br.com.expertdev.gid.service.IBMB3ClassificationService`
- `br.com.expertdev.gid.service.IBMB3Probe`

## Como executar o probe

```powershell
mvn -f "C:\Users\marcu\workspace\Pessoal\expertDev\pom.xml" -DskipTests exec:java "-Dexec.mainClass=br.com.expertdev.gid.service.IBMB3Probe" "-Dexec.args=C:\caminho\arquivo1.doc C:\caminho\arquivo2.docx"
```

## Saida esperada
Para cada arquivo:
- tipo detectado (`MSG_SISTEMA`, `INTEGRACAO_UC`, `INTEGRACAO_DI`, `API_QUARKUS`, `CANAIS_UC`, `ESPECIFICACAO_SUPLEMENTAR`, `DESCONHECIDO`)
- confianca da deteccao
- chars bruto vs. chars limpo
- linhas descartadas por ruido
- sinais de deteccao

