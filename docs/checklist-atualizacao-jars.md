# Checklist de Atualizacao de JARs (Fallback `lib/`)

Use este checklist sempre que incluir ou atualizar JARs em `lib/`.

## Checklist obrigatorio

- [ ] Existe ticket/aprovacao da mudanca
- [ ] Owner tecnico definido
- [ ] Nome do arquivo em padrao `artifactId-versao.jar`
- [ ] Versao atualizada no `pom.xml` (dependencia + execution no `maven-install-plugin`)
- [ ] SHA-256 calculado e registrado
- [ ] Licenca validada
- [ ] Scan de vulnerabilidades realizado
- [ ] Build com fallback validado (`-Dcorp.offline=true -Dcorp.lib.fallback=true`)
- [ ] Evidencia anexada ao PR

## Comando para checksum (PowerShell)

```powershell
Get-FileHash .\lib\SEU_ARQUIVO.jar -Algorithm SHA256
```

## Tabela de controle

| Jar | Versao | GroupId | ArtifactId | Owner | SHA-256 | Origem | Data |
|---|---|---|---|---|---|---|---|
| exemplo.jar | 1.0.0 | exemplo.group | exemplo-artifact | equipe-x | preencher | repositiorio interno/fornecedor | AAAA-MM-DD |

