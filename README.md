# API-FINANCE-IA

Microservico Spring Boot para geracao de insights financeiros e de gestao com IA.

## Endpoints iniciais

- `POST /insights/gerar`
- `GET /insights`
- `GET /insights/{id}`
- `GET /insights/ultimo`
- `GET /insights/kpis/resumo`
- `GET /health`

## Variaveis de ambiente

- `OPENROUTER_API_KEY`
- `OPENROUTER_BASE_URL`
- `OPENROUTER_MODEL`
- `OPENROUTER_TIMEOUT_MS`
- `OPENROUTER_MAX_OUTPUT_TOKENS`
- `OPENROUTER_TEMPERATURE`
- `OPENROUTER_APP_NAME`
- `OPENROUTER_APP_URL`
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `CORS_ALLOWED_ORIGINS`
- `SERVICES_TRANSACOES_URL`, `SERVICES_COBRANCAS_URL`, `SERVICES_CLIENTES_URL`
- `USAR_METRICAS_MOCK` (padrao `true` — desligue quando o back expuser os endpoints)
- `FALLBACK_METRICAS_MOCK_EM_FALHA` (padrao `true`)

## Contrato com o back financeiro

Ver `docs/CONTRATO_INTEGRACAO_BACK_FINANCEIRO.md` (rotas internas, JSON e checklist).

## Execucao local

```bash
mvn spring-boot:run
```
