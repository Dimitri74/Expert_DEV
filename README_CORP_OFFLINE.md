# Operacao Corporativa Restrita (Maven)

Este guia define o fluxo oficial para ambientes corporativos com acesso restrito.

## Objetivo

- Repositorio interno como fonte primaria de dependencias
- `lib/` como fallback controlado
- Governanca de atualizacao com owner e checksum

## Como usar

### Build padrao (rede liberada para repositorio interno)

```powershell
mvn clean test
```

### Build em modo corporativo restrito (sem fallback local)

```powershell
mvn -Dcorp.offline=true clean test
```

### Build em modo corporativo restrito com fallback de `lib/`

```powershell
mvn -Dcorp.offline=true -Dcorp.lib.fallback=true clean test
```

## O que cada profile faz

- `corp-offline`:
  - ativa o repositorio configurado em `corp.repo.url` no `pom.xml`
  - nao exige arquivos em `lib/`
- `corp-lib-fallback`:
  - executa `maven-install-plugin` na fase `initialize`
  - instala JARs de `lib/` no repositorio local Maven (`.m2`)
  - deve ser usado apenas quando os JARs de fallback estiverem presentes

## Pre-requisitos operacionais

1. Para usar fallback local, preencher `lib/` com os JARs esperados no `pom.xml`:
   - `jsoup-1.15.3.jar`
   - `poi-ooxml-4.1.2.jar`
   - `pdfbox-2.0.27.jar`
   - `jackson-databind-2.17.2.jar`
2. Definir URL real do repositiorio interno em `corp.repo.url`.
3. Usar `settings.xml` corporativo com mirror para bloquear repositorios externos (recomendado).

## Governanca

- Processo de atualizacao: `docs/checklist-atualizacao-jars.md`
- Toda mudanca em `lib/` deve registrar owner tecnico, checksum e justificativa.

