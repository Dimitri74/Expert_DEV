# Especificacao Tecnica - Autenticacao ExpertDev 2.4.0-BETA

## Objetivo
Implementar controle de acesso local no ExpertDev com:
- login por usuario ou email + senha;
- senha protegida por hash (PBKDF2 + salt) em SQLite embarcado;
- recuperacao de acesso por codigo temporario local;
- modo Trial de 15 dias sem credenciais;
- bloqueio de uso apos expirar o trial sem credencial valida;
- indicadores visuais de usuario e status Premium/Trial na GUI.

## Escopo implementado
1. Tela de login com:
   - entrar com usuario/email e senha;
   - cadastro de credencial;
   - recuperar acesso;
   - continuar em trial.
2. Persistencia local em `expertdev.db` com tabelas de autenticacao.
3. Recuperacao de senha com codigo de 8 caracteres e expiracao de 15 minutos.
4. Trial local de 15 dias baseado na data do primeiro uso.
5. Indicadores no cabecalho da UI:
   - usuario (extremidade direita);
   - badge `PREMIUM` ou `TRIAL Xd`.

## Arquitetura
### Pacotes e classes novas
- `br.com.expertdev.model.LicenseStatus`
- `br.com.expertdev.model.AuthSession`
- `br.com.expertdev.service.PasswordHasher`
- `br.com.expertdev.service.AuthService`
- `br.com.expertdev.ui.LoginDialog`
- `br.com.expertdev.ui.RecoveryDialog`

### Integracao na inicializacao
- `ExpertDevGUI.lancar()` agora:
  1. carrega `ExpertDevConfig`;
  2. se `auth.enabled=false`, inicia GUI direto;
  3. se `auth.enabled=true`, abre `LoginDialog`;
  4. sem sessao valida, encerra o app.

## Modelo de dados (SQLite)
### `auth_users`
- `id` PK
- `username` UNIQUE
- `email` UNIQUE
- `password_hash`
- `password_salt`
- `is_premium`
- `created_at`, `updated_at`

### `auth_password_reset`
- `id` PK
- `user_id` FK `auth_users`
- `reset_code_hash`
- `expires_at`
- `used_at`
- `created_at`

### `auth_license_state`
- `id` fixo `1`
- `first_run_at`
- `trial_days` (default 15)
- `last_check_at`

## Seguranca
- Senhas nao sao armazenadas em texto puro.
- Hash com `PBKDF2WithHmacSHA256` + salt aleatorio de 16 bytes.
- Recuperacao usa hash SHA-256 do codigo temporario.
- Codigo de reset expira em 15 minutos e e marcado como usado.

## Regras funcionais
1. Login aceita `username` ou `email`.
2. Usuario autenticado recebe sessao premium por padrao.
3. Sem login, usuario pode seguir em trial.
4. Trial expira apos 15 dias corridos desde o primeiro uso.
5. Trial expirado sem credencial valida bloqueia abertura da GUI principal.

## Configuracao
Arquivo: `expertdev.properties`
- chave nova: `auth.enabled=true|false`

## Riscos conhecidos e proximos incrementos
- Codigo de reset e local (nao envia email); para ambiente corporativo, integrar SMTP no futuro.
- Nao ha lockout por tentativas em 2.2.1; implementado em 2.4.0-BETA com 5 tentativas e bloqueio de 5 minutos.
- Nao ha criptografia de banco inteiro; apenas hash de credenciais.

