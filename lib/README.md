# Fallback de Dependencias (`lib/`)

Coloque aqui os JARs aprovados para fallback em ambiente restrito.

Arquivos esperados pelo profile `corp-lib-fallback` em `pom.xml`:

- `jsoup-1.15.3.jar`
- `poi-ooxml-4.1.2.jar`
- `pdfbox-2.0.27.jar`
- `jackson-databind-2.17.2.jar`

Importante:
- nao commitar JAR sem aprovacao de licenca/seguranca
- registrar checksum e owner no checklist em `docs/checklist-atualizacao-jars.md`

Comando para usar fallback de `lib/`:

```powershell
mvn -Dcorp.offline=true -Dcorp.lib.fallback=true clean test
```

